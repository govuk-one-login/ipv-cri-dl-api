package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.dynamo.TokenItem;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.TokenRequestPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.TokenResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLATokenExpiryWindowException;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReply;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReplyHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.util.StopWatch;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.BAD_REQUEST;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.UNAUTHORISED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_LATENCY;

public class TokenRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "token endpoint";
    private static final String REQUEST_NAME = "Token";

    private final String tokenTableName;
    private DataStore<TokenItem> dataStore;

    private URI requestURI;
    private final String username;
    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final DvlaConfiguration dvlaConfiguration;

    private final HttpRetryStatusConfig httpRetryStatusConfig;

    // Alerts will be fired for these status code responses
    // The CRI must also never retry request with these codes
    private final List<Integer> alertStatusCodes = List.of(BAD_REQUEST, UNAUTHORISED);

    // Token item shared between concurrent lambdas (if scaling)
    public static final String TOKEN_ITEM_ID = "TokenKey";

    // DynamoDB auto ttl deletion is the best effort (upto 48hrs later...)
    // Token Item ttl expiration enforced CRI side (vs dynamo filter expression)
    // as there will be only ever be one token.
    private static final long MAX_ALLOWED_TOKEN_LIFETIME_SECONDS = 3600L;
    private static final long TOKEN_EXPIRATION_WINDOW_SECONDS = 300L;
    private static final long TOKEN_ITEM_TTL_SECS =
            MAX_ALLOWED_TOKEN_LIFETIME_SECONDS - TOKEN_EXPIRATION_WINDOW_SECONDS;

    public static final String INVALID_EXPIRY_WINDOW_ERROR_MESSAGE =
            "Token expiry window not valid";

    private final StopWatch stopWatch;

    public TokenRequestService(
            DvlaConfiguration dvlaConfiguration,
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        // Token Table
        this.tokenTableName = dvlaConfiguration.getTokenTableName();
        this.dataStore = new DataStore<>(tokenTableName, TokenItem.class, dynamoDbEnhancedClient);

        this.requestURI = URI.create(dvlaConfiguration.getTokenEndpoint());
        this.username = dvlaConfiguration.getUsername();

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new TokenHttpRetryStatusConfig();
        this.dvlaConfiguration = dvlaConfiguration;

        this.stopWatch = new StopWatch();
    }

    public String requestToken(boolean alwaysRequestNewToken, Strategy strategy)
            throws OAuthErrorResponseException {

        LOGGER.info("Checking Table {} for existing cached token", tokenTableName);

        TokenItem tokenItem = getTokenItemFromTable(strategy);

        boolean existingCachedToken = tokenItem != null;
        boolean tokenTtlHasExpired =
                existingCachedToken
                        && isTokenNearExpiration(tokenItem, TOKEN_EXPIRATION_WINDOW_SECONDS);

        LOGGER.info(
                "Existing cached token - {} - ttl expired {}",
                existingCachedToken,
                tokenTtlHasExpired);

        if (alwaysRequestNewToken) {
            LOGGER.info("Override enabled - requesting a new token");
        }

        boolean newTokenRequest =
                alwaysRequestNewToken || !existingCachedToken || tokenTtlHasExpired;

        // Request an Access Token
        if (newTokenRequest) {

            TokenResponse newTokenResponse =
                    performNewTokenRequest(dvlaConfiguration.getPassword(), strategy);

            LOGGER.info("Saving Token {}", newTokenResponse.getIdToken());

            tokenItem = new TokenItem(newTokenResponse.getIdToken());

            saveTokenItem(tokenItem, strategy);
        } else {
            long ttl = tokenItem.getTtl();

            LOGGER.info(
                    "Re-using cached Token - expires {} UTC",
                    Instant.ofEpochSecond(ttl).atZone(ZoneId.systemDefault()).toLocalDateTime());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN
                            .withEndpointPrefix());
        }

        return tokenItem.getTokenValue();
    }

    public TokenResponse performNewTokenRequest(String passwordParam, Strategy strategy)
            throws OAuthErrorResponseException {

        final String requestId = UUID.randomUUID().toString();
        LOGGER.info("{} Request Id {}", REQUEST_NAME, requestId);

        // Token Request is posted as if via a form
        final HttpPost request = new HttpPost();

        // TestStrategy Logic
        if (strategy == Strategy.NO_CHANGE) {
            request.setURI(requestURI);
        } else {
            final String endpointUri = dvlaConfiguration.getEndpointURLs().get(strategy.name());
            final String tokenEndpoint =
                    String.format("%s%s", endpointUri, dvlaConfiguration.getTokenPath());
            this.requestURI = URI.create(tokenEndpoint);
            request.setURI(requestURI);
        }

        request.addHeader(
                RequestHeaderKeys.HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        String requestBody;
        try {
            TokenRequestPayload tokenRequestPayload =
                    TokenRequestPayload.builder()
                            .userName(username)
                            .password(passwordParam)
                            .build();

            requestBody = objectMapper.writeValueAsString(tokenRequestPayload);
            LOGGER.debug("response body: {}", requestBody);
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException creating request body");
            LOGGER.debug(e.getMessage());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_TOKEN_REQUEST_PAYLOAD);
        }

        LOGGER.debug(
                "{} request headers : {}",
                ENDPOINT_NAME,
                LOGGER.isDebugEnabled() ? (Arrays.toString(request.getAllHeaders())) : "");
        LOGGER.debug("{} request body : {}", REQUEST_NAME, requestBody);

        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        eventProbe.counterMetric(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("Submitting {} request to third party...", REQUEST_NAME);
        stopWatch.start();
        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {
            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());

            // throws OAuthErrorResponseException on error
            httpReply = HTTPReplyHelper.retrieveResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {
            // No Response Latency
            eventProbe.counterMetric(
                    DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix(), stopWatch.stop());

            LOGGER.error("IOException executing {} request - {}", REQUEST_NAME, e.getMessage());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR
                            .withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT);
        }

        // Response Latency
        eventProbe.counterMetric(
                DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix(), stopWatch.stop());

        if (httpReply.statusCode == 200) {
            LOGGER.info("{} status code {}", REQUEST_NAME, httpReply.statusCode);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            try {
                LOGGER.debug("{} headers {}", REQUEST_NAME, httpReply.responseHeaders);
                LOGGER.debug("{} response {}", REQUEST_NAME, httpReply.responseBody);

                TokenResponse response =
                        objectMapper.readValue(httpReply.responseBody, TokenResponse.class);

                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_VALID
                                .withEndpointPrefix());

                return response;
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_INVALID
                                .withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY);
            }
        } else {
            // The token request responded but with an unexpected status code
            LOGGER.error(
                    "{} response status code {} content - {}",
                    REQUEST_NAME,
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            if (alertStatusCodes.contains(httpReply.statusCode)) {
                LOGGER.warn("Status code {}, triggered alert metric", httpReply.statusCode);

                // Alarm Firing
                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_STATUS_CODE_ALERT_METRIC
                                .withEndpointPrefix());
            }

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    private TokenItem getTokenItemFromTable(Strategy strategy) {
        return dataStore.getItem(strategy.name() + TOKEN_ITEM_ID);
    }

    private void saveTokenItem(TokenItem tokenItem, Strategy strategy) {
        // id=<Strategy>TOKEN_ITEM_ID as tokenItem used is dependant on third party routing

        tokenItem.setId(strategy.name() + TOKEN_ITEM_ID);

        long ttlSeconds = Instant.now().plusSeconds(TOKEN_ITEM_TTL_SECS).getEpochSecond();

        tokenItem.setTtl(ttlSeconds);
        // Create calls put which overwrites any existing token
        dataStore.create(tokenItem);

        LOGGER.info(
                "Token cached - expires {} UTC",
                Instant.ofEpochSecond(ttlSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public void removeTokenItem(Strategy strategy) {
        String tokenId = strategy.name() + TOKEN_ITEM_ID;
        dataStore.delete(tokenId);
        LOGGER.info("Token removed from table {}", tokenTableName);
    }

    public boolean isTokenNearExpiration(TokenItem tokenItem, long expiryWindow) {

        if (expiryWindow <= 0 || expiryWindow >= TOKEN_ITEM_TTL_SECS) {
            throw new DVLATokenExpiryWindowException(INVALID_EXPIRY_WINDOW_ERROR_MESSAGE);
        }

        long expiresTime = tokenItem.getTtl();

        long now = Instant.now().getEpochSecond();

        long windowStart =
                Instant.ofEpochSecond(expiresTime).minusSeconds(expiryWindow).getEpochSecond();

        return now >= windowStart;
    }
}
