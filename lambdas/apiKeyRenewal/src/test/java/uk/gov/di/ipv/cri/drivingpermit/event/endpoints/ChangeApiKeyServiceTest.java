package uk.gov.di.ipv.cri.drivingpermit.event.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.service.ChangeApiKeyHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_RESPONSE_STATUS_CODE_ALERT_METRIC;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_API_KEY_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;

@ExtendWith(MockitoExtension.class)
class ChangeApiKeyServiceTest {

    private static final String TEST_END_POINT = "http://127.0.0.1";
    private static final String TEST_API_KEY = "TESTAPIKEY";

    @Mock private DvlaConfiguration mockDvlaConfiguration;
    @Mock private HttpRetryer mockHttpRetryer;
    @Mock private RequestConfig mockRequestConfig;
    @Mock private EventProbe mockEventProbe;

    private ChangeApiKeyService changeApiKeyService;
    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockDvlaConfiguration.getChangeApiKeyEndpoint())
                .thenReturn(TEST_END_POINT + "/apiKey");

        changeApiKeyService =
                new ChangeApiKeyService(
                        mockDvlaConfiguration,
                        mockHttpRetryer,
                        mockRequestConfig,
                        realObjectMapper,
                        mockEventProbe);
    }

    @Test
    void shouldReturnApiKeyWhenRequestedFromThirdParty()
            throws IOException, OAuthErrorResponseException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse changeApiKeyResponse =
                HttpResponseFixtures.createHttpResponse(
                        200, null, "{\"newApiKey\": \"test_api_key_response\"}", false);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(ChangeApiKeyHttpRetryStatusConfig.class)))
                .thenReturn(changeApiKeyResponse);

        String newApiKey =
                changeApiKeyService.sendApiKeyChangeRequest(
                        "existing_api_key", "authorizationToken");

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_API_KEY_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertChangeApiKeyHeaders(httpRequestCaptor);
        assertEquals(newApiKey, "test_api_key_response");
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenChangeApiKeyEndpointDoesNotRespond()
            throws IOException {

        Exception exceptionCaught = new IOException("Token Endpoint Timed out");

        doThrow(exceptionCaught)
                .when(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangeApiKeyHttpRetryStatusConfig.class));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                changeApiKeyService.sendApiKeyChangeRequest(
                                        "existing_api_key", "authorizationToken"),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangeApiKeyHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_API_KEY_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(
                                exceptionCaught));
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenChangeApiKeyEndpointResponseStatusCodeNot200()
            throws IOException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Bearer access token but status not 200
        CloseableHttpResponse changeApiKeyResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "Server Error", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(ChangeApiKeyHttpRetryStatusConfig.class)))
                .thenReturn(changeApiKeyResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_API_KEY_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                changeApiKeyService.sendApiKeyChangeRequest(
                                        "existing_api_key", "authorizationToken"),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangeApiKeyHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_API_KEY_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_API_KEY_RESPONSE_STATUS_CODE_ALERT_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    private void assertChangeApiKeyHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));
    }
}
