package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.dynamo.TokenItem;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.TokenResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.DVLATokenExpiryWindowException;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HTTPReply;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HTTPReplyHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.RequestHeaderKeys.HEADER_AUTHORIZATION;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.RequestHeaderKeys.HEADER_CONTENT_TYPE;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_VALID;

public class TokenRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "token endpoint";

    private final String tokenTableName;
    private final DynamoDbTable<TokenItem> tokenTable;

    private final URI requestURI;
    private final String authValue;

    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    // Token item shared between concurrent lambdas (if scaling)
    public static final String TOKEN_ITEM_ID = "TokenKey";
    public static final Key TOKEN_ITEM_KEY = Key.builder().partitionValue(TOKEN_ITEM_ID).build();

    // DynamoDB auto ttl deletion is the best effort (upto 48hrs later...)
    // Token Item ttl expiration enforced CRI side (vs dynamo filter expression)
    // as there will be only ever be one token.
    private static final long MAX_ALLOWED_TOKEN_LIFETIME_SECONDS = 3600L;
    private static final long TOKEN_EXPIRATION_WINDOW_SECONDS = 300L;
    private static final long TOKEN_ITEM_TTL_SECS =
            MAX_ALLOWED_TOKEN_LIFETIME_SECONDS - TOKEN_EXPIRATION_WINDOW_SECONDS;

    public static final String INVALID_EXPIRY_WINDOW_ERROR_MESSAGE =
            "Token expiry window not valid";

    public TokenRequestService(
            DvlaConfiguration dvlaConfiguration,
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        // Token Table
        tokenTableName = dvlaConfiguration.getTokenTableName();
        tokenTable =
                dynamoDbEnhancedClient.table(tokenTableName, TableSchema.fromBean(TokenItem.class));

        this.requestURI = URI.create(dvlaConfiguration.getTokenEndpoint());

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        // Basic Auth = "username:password" in base64
        String usernameAndPassword =
                String.format(
                        "%s:%s", dvlaConfiguration.getUsername(), dvlaConfiguration.getPassword());
        String usernameAndPasswordHash =
                Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
        authValue = String.format("Basic %s", usernameAndPasswordHash);
    }

    public String requestAccessToken(boolean alwaysRequestNewToken)
            throws OAuthErrorResponseException {

        LOGGER.info("Checking Table {} for existing cached token", tokenTableName);

        TokenItem tokenItem = getTokenItemFromTable();

        boolean existingCachedToken = tokenItem != null;
        boolean tokenTtlHasExpired =
                existingCachedToken
                        && isTokenNearExpiration(tokenItem, TOKEN_EXPIRATION_WINDOW_SECONDS);

        LOGGER.info(
                "Existing cached token - {} - ttl expired {}",
                existingCachedToken,
                tokenTtlHasExpired);

        if (alwaysRequestNewToken) {
            LOGGER.info("Override enabled - always requesting a new token");
        }

        boolean newTokenRequest =
                alwaysRequestNewToken || !existingCachedToken || tokenTtlHasExpired;

        // Request an Access Token
        if (newTokenRequest) {

            TokenResponse newTokenResponse = performNewTokenRequest();

            LOGGER.info("Saving Token {}", newTokenResponse.getIdToken());

            tokenItem = new TokenItem(newTokenResponse.getIdToken());

            saveTokenItem(tokenItem);
        } else {
            long ttl = tokenItem.getTtl();

            LOGGER.info(
                    "Re-using cached Token - expires {} UTC",
                    Instant.ofEpochSecond(ttl).atZone(ZoneId.systemDefault()).toLocalDateTime());

            eventProbe.counterMetric(DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN.withEndpointPrefix());
        }

        return tokenItem.getTokenValue();
    }

    private TokenResponse performNewTokenRequest() throws OAuthErrorResponseException {

        final String requestId = UUID.randomUUID().toString();
        LOGGER.info("{} Request Id {}", ENDPOINT_NAME, requestId);

        // Token Request is posted as if via a form
        final HttpPost request = new HttpPost();
        request.setURI(requestURI);
        request.addHeader(
                HEADER_CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        request.addHeader(HEADER_AUTHORIZATION, authValue);

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        eventProbe.counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("Token request endpoint is {}", requestURIString);
        LOGGER.info("Submitting token request to third party...");
        try (CloseableHttpResponse response = httpRetryer.sendHTTPRequestRetryIfAllowed(request)) {

            eventProbe.counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());

            // throws OAuthErrorResponseException on error
            httpReply =
                    HTTPReplyHelper.retrieveStatusCodeAndBodyFromResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {

            LOGGER.error("IOException executing token request - {}", e.getMessage());

            eventProbe.counterMetric(
                    DVLA_TOKEN_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT);
        }

        if (httpReply.statusCode == 200) {
            LOGGER.info("Token status code {}", httpReply.statusCode);

            eventProbe.counterMetric(
                    DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

            try {
                LOGGER.debug("Token ResponseBody - {}", httpReply.responseBody);

                TokenResponse response =
                        objectMapper.readValue(httpReply.responseBody, TokenResponse.class);

                eventProbe.counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());

                return response;
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException mapping Token response");
                LOGGER.debug(e.getMessage());

                eventProbe.counterMetric(DVLA_TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY);
            }
        } else {
            // The token request responded but with an unexpected status code
            LOGGER.error(
                    "Token response status code {} content - {}",
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(
                    DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    private TokenItem getTokenItemFromTable() {
        return tokenTable.getItem(TOKEN_ITEM_KEY);
    }

    private void saveTokenItem(TokenItem tokenItem) {
        // id=TOKEN_ITEM_ID as same TokenItem is always used
        tokenItem.setId(TOKEN_ITEM_ID);

        long ttlSeconds = Instant.now().plusSeconds(TOKEN_ITEM_TTL_SECS).getEpochSecond();

        tokenItem.setTtl(ttlSeconds);
        tokenTable.putItem(tokenItem);

        LOGGER.info(
                "Token cached - expires {} UTC",
                Instant.ofEpochSecond(ttlSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime());
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
