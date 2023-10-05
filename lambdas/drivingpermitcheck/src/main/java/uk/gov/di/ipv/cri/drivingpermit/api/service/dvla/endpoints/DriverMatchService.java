package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints;

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
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.DvlaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.DriverMatchAPIResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.DriverMatchErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.errorresponse.fields.Errors;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.dvla.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HTTPReply;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HTTPReplyHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.RequestHeaderKeys.HEADER_API_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.RequestHeaderKeys.HEADER_AUTHORIZATION;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request.RequestHeaderKeys.HEADER_CONTENT_TYPE;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.NOT_FOUND;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_VALID;

public class DriverMatchService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "driver match endpoint";

    private static final String REQUEST_NAME = "Driver match";

    private static final String DVLA_RESPONSE_HEADER_REQUEST_ID_KEY = "X-DVLA-Request-Id";

    private static final String HTTP_404_EXPECTED_ERROR_CODE = "ENQ018";

    private final URI requestURI;
    private final String apiKey;

    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;

    private final EventProbe eventProbe;

    private final HttpRetryStatusConfig httpRetryStatusConfig;

    public DriverMatchService(
            DvlaConfiguration dvlaConfiguration,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        this.requestURI = URI.create(dvlaConfiguration.getMatchEndpoint());
        this.apiKey = dvlaConfiguration.getApiKey();

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new DriverMatchHttpRetryStatusConfig();
    }

    public DriverMatchServiceResult performMatch(
            DrivingPermitForm drivingPermitForm, String tokenValue)
            throws OAuthErrorResponseException {

        // Request is posted as if JSON
        final HttpPost request = new HttpPost();
        request.setURI(requestURI);

        request.addHeader(HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(HEADER_AUTHORIZATION, tokenValue);
        request.addHeader(HEADER_API_KEY, apiKey);

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        // Body Params
        String requestBody;
        try {
            DvlaPayload dvlaPayload =
                    DvlaPayload.builder()
                            .drivingLicenceNumber(drivingPermitForm.getDrivingLicenceNumber())
                            .lastName(drivingPermitForm.getSurname())
                            .issueNumber(drivingPermitForm.getIssueNumber())
                            .validFrom(drivingPermitForm.getIssueDate().toString())
                            .validTo(drivingPermitForm.getExpiryDate().toString())
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

        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        eventProbe.counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("Submitting {} request to third party...", REQUEST_NAME);
        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {

            eventProbe.counterMetric(DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());

            // throws OAuthErrorResponseException on error
            httpReply = HTTPReplyHelper.retrieveResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {
            LOGGER.error("IOException executing {} request - {}", REQUEST_NAME, e.getMessage());

            eventProbe.counterMetric(
                    DVLA_MATCH_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT);
        }

        // There are two API response types possible depending on userdata
        if (httpReply.statusCode == SUCCESS || httpReply.statusCode == NOT_FOUND) {

            LOGGER.info("{} status code {}", REQUEST_NAME, httpReply.statusCode);

            eventProbe.counterMetric(
                    DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

            LOGGER.debug("{} headers {}", REQUEST_NAME, httpReply.responseHeaders);
            LOGGER.debug("{} response {}", REQUEST_NAME, httpReply.responseBody);

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

                eventProbe.counterMetric(DVLA_MATCH_RESPONSE_TYPE_VALID.withEndpointPrefix());

                return DriverMatchServiceResult.builder()
                        .validity(validity)
                        .requestId(requestId)
                        .build();
            } catch (JsonProcessingException e) {

                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                // Invalid due to json mapping fail
                eventProbe.counterMetric(DVLA_MATCH_RESPONSE_TYPE_INVALID.withEndpointPrefix());

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
                    DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_MATCH_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    // 404 response validation is disabled - reply is being trusted to be correct
    //    private Errors verifyAndRetrieve404Error(
    //            DriverMatchErrorResponse driverMatchErrorResponse, HTTPReply httpReply)
    //            throws OAuthErrorResponseException {
    //
    //        if (driverMatchErrorResponse.getErrors() == null
    //                || driverMatchErrorResponse.getErrors().size() != 1) {
    //
    //            String errorsListSize =
    //                    (driverMatchErrorResponse.getErrors() == null)
    //                            ? null
    //                            : String.valueOf(driverMatchErrorResponse.getErrors().size());
    //
    //            LOGGER.error(
    //                    "One error expected in {} 404 response, errors list size was {}",
    //                    REQUEST_NAME,
    //                    errorsListSize);
    //            LOGGER.debug(httpReply.responseBody);
    //
    //            // Invalid due to mismatch in the number of errors
    //            eventProbe.counterMetric(DVLA_MATCH_RESPONSE_TYPE_INVALID.withEndpointPrefix());
    //
    //            throw new OAuthErrorResponseException(
    //                    HttpStatusCode.INTERNAL_SERVER_ERROR,
    //                    ErrorResponse.MATCH_ENDPOINT_404_RESPONSE_EXPECTED_ERROR_SIZE_NOT_ONE);
    //        }
    //
    //        // There is one error but is it the expected error code
    //        Errors errors = driverMatchErrorResponse.getErrors().get(0);
    //
    //        // Check the 404 error is a licence number not found
    //        // error code, and not a different code
    //        if (!errors.getCode().equals(HTTP_404_EXPECTED_ERROR_CODE)) {
    //            LOGGER.error(
    //                    "404 Status, error code mismatch expected code {} but found code {}",
    //                    HTTP_404_EXPECTED_ERROR_CODE,
    //                    errors.getCode());
    //            LOGGER.debug(httpReply.responseBody);
    //
    //            // Invalid due to a code mismatch
    //            eventProbe.counterMetric(DVLA_MATCH_RESPONSE_TYPE_INVALID.withEndpointPrefix());
    //
    //            throw new OAuthErrorResponseException(
    //                    HttpStatusCode.INTERNAL_SERVER_ERROR,
    //
    // ErrorResponse.MATCH_ENDPOINT_404_RESPONSE_EXPECTED_ERROR_CODE_NOT_CORRECT);
    //        }
    //
    //        return errors;
    //    }
}
