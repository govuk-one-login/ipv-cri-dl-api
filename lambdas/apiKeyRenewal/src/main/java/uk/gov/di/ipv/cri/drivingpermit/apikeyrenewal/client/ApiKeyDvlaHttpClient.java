package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.account.ipv.cri.lime.limeade.util.http.HTTPReply;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.dvla.ChangeApiKeyResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReplyHelper;

import java.io.IOException;
import java.net.URI;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys.HEADER_API_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys.HEADER_AUTHORIZATION;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.RequestHeaderKeys.HEADER_CONTENT_TYPE;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_RESPONSE_STATUS_CODE_ALERT_METRIC;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_API_KEY_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;

public class ApiKeyDvlaHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyDvlaHttpClient.class);
    private static final String ENDPOINT_NAME = "dvla-api-key";

    private final DvlaConfiguration dvlaConfiguration;
    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final HttpRetryStatusConfig httpRetryStatusConfig =
            new ApiKeyDvlaHttpRetryStatusConfig();

    public ApiKeyDvlaHttpClient(
            DvlaConfiguration dvlaConfiguration,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {
        this.dvlaConfiguration = dvlaConfiguration;
        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
    }

    public String requestNewApiKey(String authorization)
            throws OAuthErrorResponseException, JsonProcessingException {
        HTTPReply httpReply = send(buildRequest(authorization));

        if (httpReply.statusCode() != SUCCESS) {
            LOGGER.atError()
                    .setMessage("DVLA rejected the change API key request")
                    .addKeyValue("status_code", httpReply.statusCode())
                    .addKeyValue("response_body", httpReply.responseBody())
                    .log();
            emit(DVLA_CHANGE_API_KEY_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS);
            emit(DVLA_CHANGE_API_KEY_RESPONSE_STATUS_CODE_ALERT_METRIC);
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_API_KEY_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
        emit(DVLA_CHANGE_API_KEY_RESPONSE_TYPE_EXPECTED_HTTP_STATUS);
        var newApiKey =
                objectMapper
                        .readValue(httpReply.responseBody(), ChangeApiKeyResponse.class)
                        .newApiKey();
        LOGGER.info("DVLA returned a new API key");
        return newApiKey;
    }

    private HttpPost buildRequest(String authorization) {
        HttpPost request = new HttpPost(URI.create(dvlaConfiguration.getChangeApiKeyEndpoint()));
        request.addHeader(HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(HEADER_API_KEY, dvlaConfiguration.getApiKey());
        request.addHeader(HEADER_AUTHORIZATION, authorization);
        request.setConfig(requestConfig);
        request.setEntity(new StringEntity("", ContentType.APPLICATION_JSON));
        return request;
    }

    private HTTPReply send(HttpPost request) throws OAuthErrorResponseException {
        emit(DVLA_CHANGE_API_KEY_REQUEST_CREATED);
        LOGGER.atInfo()
                .setMessage("Sending change API key request to DVLA")
                .addKeyValue("uri", request.getURI())
                .log();

        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {
            emit(DVLA_CHANGE_API_KEY_REQUEST_SEND_OK);
            return HTTPReplyHelper.retrieveResponse(response, ENDPOINT_NAME);
        } catch (IOException e) {
            LOGGER.error("Failed to send change API key request to DVLA", e);
            emit(DVLA_CHANGE_API_KEY_REQUEST_SEND_ERROR, e);
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT);
        }
    }

    private void emit(ThirdPartyAPIEndpointMetric metric) {
        eventProbe.counterMetric(metric.withEndpointPrefix());
    }

    private void emit(ThirdPartyAPIEndpointMetric metric, IOException e) {
        eventProbe.counterMetric(metric.withEndpointPrefixAndExceptionName(e));
    }
}
