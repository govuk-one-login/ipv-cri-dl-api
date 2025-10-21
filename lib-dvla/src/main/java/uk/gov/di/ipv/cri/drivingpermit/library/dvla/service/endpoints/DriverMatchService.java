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
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DvlaFormFields;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.DvlaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.DriverMatchAPIResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.DriverMatchErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.errorresponse.fields.Errors;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
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
import java.util.Arrays;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.NOT_FOUND;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.UNAUTHORISED;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_MATCH_ENDPOINT_REJECTED_TOKEN_OR_API_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_LATENCY;

public class DriverMatchService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "driver match endpoint";

    private static final String REQUEST_NAME = "Driver match";

    private static final String DVLA_RESPONSE_HEADER_REQUEST_ID_KEY = "X-DVLA-Request-Id";

    private URI requestURI;
    private final String apiKey;

    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;

    private final EventProbe eventProbe;

    private final HttpRetryStatusConfig httpRetryStatusConfig;

    private final DvlaConfiguration dvlaConfiguration;

    private final StopWatch stopWatch;

    public DriverMatchService(
            DvlaConfiguration dvlaConfiguration,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        this.dvlaConfiguration = dvlaConfiguration;
        this.requestURI = URI.create(dvlaConfiguration.getMatchEndpoint());
        this.apiKey = dvlaConfiguration.getApiKey();

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new DriverMatchHttpRetryStatusConfig();
        this.stopWatch = new StopWatch();
    }

    @java.lang.SuppressWarnings("java:S3776")
    public DriverMatchServiceResult performMatch(
            DvlaFormFields dvlaFormFields, String tokenValue, String apiKey, Strategy strategy)
            throws OAuthErrorResponseException {

        // Request is posted as if JSON
        final HttpPost request = new HttpPost();

        // TestStrategy Logic
        if (strategy == Strategy.NO_CHANGE) {
            request.setURI(requestURI);
        } else {
            final String endpointUri = dvlaConfiguration.getEndpointURLs().get(strategy.name());
            this.requestURI =
                    URI.create(
                            String.format("%s%s", endpointUri, dvlaConfiguration.getMatchPath()));
            request.setURI(requestURI);
        }

        request.addHeader(
                RequestHeaderKeys.HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(RequestHeaderKeys.HEADER_AUTHORIZATION, tokenValue);
        request.addHeader(RequestHeaderKeys.HEADER_API_KEY, apiKey);

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        // Body Params
        String requestBody;
        try {
            DvlaPayload dvlaPayload =
                    DvlaPayload.builder()
                            .drivingLicenceNumber(dvlaFormFields.getDrivingLicenceNumber())
                            .lastName(dvlaFormFields.getSurname())
                            .issueNumber(dvlaFormFields.getIssueNumber())
                            .validFrom(dvlaFormFields.getIssueDate().toString())
                            .validTo(dvlaFormFields.getExpiryDate().toString())
                            .build();

            requestBody = objectMapper.writeValueAsString(dvlaPayload);
        } catch (JsonProcessingException e) {
            // PII in variables
            LOGGER.error("JsonProcessingException creating request body");
            LOGGER.debug(e.getMessage());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_MATCH_REQUEST_PAYLOAD);
        }

        LOGGER.debug(
                "{} request headers : {}",
                ENDPOINT_NAME,
                LOGGER.isDebugEnabled() ? (Arrays.toString(request.getAllHeaders())) : "");
        LOGGER.debug("{} request body : {}", ENDPOINT_NAME, requestBody);
        LOGGER.info(
                "{} request headers : {}",
                ENDPOINT_NAME,
                LOGGER.isInfoEnabled() ? (Arrays.toString(request.getAllHeaders())) : "");
        LOGGER.info("{} request body : {}", ENDPOINT_NAME, requestBody);

        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        eventProbe.counterMetric(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("Submitting {} request to third party...", REQUEST_NAME);
        stopWatch.start();
        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());

            // throws OAuthErrorResponseException on error
            httpReply = HTTPReplyHelper.retrieveResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {
            // No Response Latency
            eventProbe.counterMetric(
                    DVLA_MATCH_RESPONSE_LATENCY.withEndpointPrefix(), stopWatch.stop());

            LOGGER.error("IOException executing {} request - {}", REQUEST_NAME, e.getMessage());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_ERROR
                            .withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT);
        }

        // Response Latency
        eventProbe.counterMetric(
                DVLA_MATCH_RESPONSE_LATENCY.withEndpointPrefix(), stopWatch.stop());

        // There are two API response types possible depending on userdata
        if (httpReply.statusCode == SUCCESS || httpReply.statusCode == NOT_FOUND) {

            LOGGER.info("{} status code {}", REQUEST_NAME, httpReply.statusCode);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            LOGGER.debug("{} headers {}", REQUEST_NAME, httpReply.responseHeaders);
            LOGGER.debug("{} response {}", REQUEST_NAME, httpReply.responseBody);
            LOGGER.info("{} headers {}", REQUEST_NAME, httpReply.responseHeaders);
            LOGGER.info("{} response {}", REQUEST_NAME, httpReply.responseBody);

            try {
                Validity validity;

                if (httpReply.statusCode == SUCCESS) {
                    DriverMatchAPIResponse driverMatchAPIResponse =
                            objectMapper.readValue(
                                    httpReply.responseBody, DriverMatchAPIResponse.class);

                    // Invalid = licence number found but details mismatch
                    validity =
                            driverMatchAPIResponse.isValidDocument()
                                    ? Validity.VALID
                                    : Validity.INVALID;
                } else {
                    // 404 response - with message in body
                    DriverMatchErrorResponse driverMatchErrorResponse =
                            objectMapper.readValue(
                                    httpReply.responseBody, DriverMatchErrorResponse.class);

                    // For monitoring
                    int numberOfErrors = driverMatchErrorResponse.getErrors().size();
                    if (numberOfErrors > 1) {
                        LOGGER.warn(
                                "404 response contains {} errors in errors array, only expected 1",
                                numberOfErrors);
                    }

                    Errors errors = driverMatchErrorResponse.getErrors().get(0);
                    LOGGER.info(
                            "{} got valid 404 response, Code {}, Detail {}",
                            REQUEST_NAME,
                            errors.getCode(),
                            errors.getDetail());

                    // licence number was not found
                    validity = Validity.NOT_FOUND;
                }

                LOGGER.info("{} validity status {}", REQUEST_NAME, validity);

                // requestId is in the response header
                String requestId =
                        httpReply.responseHeaders.get(DVLA_RESPONSE_HEADER_REQUEST_ID_KEY);
                LOGGER.info("{} response request Id {}", REQUEST_NAME, requestId);

                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_VALID
                                .withEndpointPrefix());

                return DriverMatchServiceResult.builder()
                        .validity(validity)
                        .requestId(requestId)
                        .build();
            } catch (JsonProcessingException e) {

                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                // Invalid due to json mapping fail
                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_INVALID
                                .withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_MATCH_ENDPOINT_RESPONSE_BODY);
            }
        } else {

            // Endpoint responded but with an unexpected status code
            LOGGER.error(
                    "{} response status code {} content - {}",
                    REQUEST_NAME,
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            // Note 401 is for Token or API Key
            // Throw exception to allow recovering cases of token expiry
            if (httpReply.statusCode == UNAUTHORISED) {

                LOGGER.warn(ERROR_MATCH_ENDPOINT_REJECTED_TOKEN_OR_API_KEY);

                throw new DVLAMatchUnauthorizedException(
                        ERROR_MATCH_ENDPOINT_REJECTED_TOKEN_OR_API_KEY.getMessage());
            }

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_MATCH_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }
}
