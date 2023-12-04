package uk.gov.di.ipv.cri.drivingpermit.event.endpoints;

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
import uk.gov.di.ipv.cri.drivingpermit.event.request.ChangePasswordPayload;
import uk.gov.di.ipv.cri.drivingpermit.event.service.ChangePasswordHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.UnauthorisedException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReply;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReplyHelper;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys.HEADER_CONTENT_TYPE;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_STATUS_CODE_ALERT_METRIC;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;

public class ChangePasswordService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "change password endpoint";
    private static final String REQUEST_NAME = "Change Password";

    private final URI requestURI;
    private final String username;

    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final HttpRetryStatusConfig httpRetryStatusConfig;

    private final DvlaConfiguration dvlaConfiguration;

    public ChangePasswordService(
            DvlaConfiguration dvlaConfiguration,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        this.requestURI = URI.create(dvlaConfiguration.getChangePasswordEndpoint());
        this.username = dvlaConfiguration.getUsername();

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new ChangePasswordHttpRetryStatusConfig();
        this.dvlaConfiguration = dvlaConfiguration;
    }

    public void sendPasswordChangeRequest(String newPassword)
            throws OAuthErrorResponseException, UnauthorisedException {
        sendPasswordChangeRequest(newPassword, dvlaConfiguration.getPassword());
    }

    public void sendPasswordChangeRequest(String newPassword, String exisitingPassword)
            throws OAuthErrorResponseException, UnauthorisedException {

        final String requestId = UUID.randomUUID().toString();
        LOGGER.info("{} Request Id {}", REQUEST_NAME, requestId);

        // Change Password Request is posted as if via a form
        final HttpPost request = new HttpPost();
        request.setURI(requestURI);
        request.addHeader(HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        String requestBody = createRequestBody(newPassword, exisitingPassword);

        LOGGER.debug("{} request body : {}", REQUEST_NAME, requestBody);

        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        eventProbe.counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("Submitting {} request to third party...", REQUEST_NAME);
        // This will also need tidied up in LIME-906
        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {
            eventProbe.counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK.withEndpointPrefix());

            // throws OAuthErrorResponseException on error
            httpReply = HTTPReplyHelper.retrieveResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {

            LOGGER.error("IOException executing {} request - {}", REQUEST_NAME, e.getMessage());

            eventProbe.counterMetric(
                    DVLA_CHANGE_PASSWORD_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT);
        }

        if (httpReply.statusCode == 200) {
            LOGGER.info("{} status code {}", REQUEST_NAME, httpReply.statusCode);

            eventProbe.counterMetric(
                    DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        } else {
            // The change password request responded but with an unexpected status code
            LOGGER.error(
                    "{} response status code {} content - {}",
                    REQUEST_NAME,
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(
                    DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            LOGGER.warn("Status code {}, triggered alert metric", httpReply.statusCode);

            // Alarm Firing
            eventProbe.counterMetric(
                    DVLA_CHANGE_PASSWORD_RESPONSE_STATUS_CODE_ALERT_METRIC.withEndpointPrefix());

            if (httpReply.statusCode == HttpStatusCode.UNAUTHORIZED) {
                throw new UnauthorisedException(
                        HttpStatusCode.UNAUTHORIZED,
                        ErrorResponse
                                .ERROR_CHANGE_PASSWORD_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
            }

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse
                            .ERROR_CHANGE_PASSWORD_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    private String createRequestBody(String newPassword, String existingPassword)
            throws OAuthErrorResponseException {
        String requestBody;
        try {

            ChangePasswordPayload changePasswordPayload =
                    ChangePasswordPayload.builder()
                            .userName(username)
                            .password(existingPassword)
                            .newPassword(newPassword)
                            .build();

            requestBody = objectMapper.writeValueAsString(changePasswordPayload);
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException creating request body");
            LOGGER.debug(e.getMessage());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_CHANGE_PASSWORD_REQUEST_PAYLOAD);
        }
        return requestBody;
    }
}
