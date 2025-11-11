package uk.gov.di.ipv.cri.drivingpermit.event.endpoints;

import com.fasterxml.jackson.core.exc.InputCoercionException;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.request.ChangePasswordPayload;
import uk.gov.di.ipv.cri.drivingpermit.event.service.ChangePasswordHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_LATENCY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_STATUS_CODE_ALERT_METRIC;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    private static final String TEST_END_POINT = "http://127.0.0.1";
    private static final String TEST_USER_NAME = "TEST";
    private static final String TEST_PASSWORD = "PASSWORD";

    private ChangePasswordService changePasswordService;
    private ObjectMapper realObjectMapper;
    @Mock private DvlaConfiguration mockDvlaConfiguration;
    @Mock private HttpRetryer mockHttpRetryer;
    @Mock private RequestConfig mockRequestConfig;
    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockDvlaConfiguration.getChangePasswordEndpoint()).thenReturn(TEST_END_POINT);
        when(mockDvlaConfiguration.getUsername()).thenReturn(TEST_USER_NAME);
        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

        changePasswordService =
                new ChangePasswordService(
                        mockDvlaConfiguration,
                        mockHttpRetryer,
                        mockRequestConfig,
                        realObjectMapper,
                        mockEventProbe);
    }

    @Test
    void shouldReturn200WhenChangePasswordServiceCalled() throws IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse changePasswordResponse =
                HttpResponseFixtures.createHttpResponse(200, null, null, false);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(ChangePasswordHttpRetryStatusConfig.class)))
                .thenReturn(changePasswordResponse);

        assertDoesNotThrow(
                () ->
                        changePasswordService.sendPasswordChangeRequest(
                                "NEWPASSWORD", Strategy.NO_CHANGE));

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        eq(DVLA_CHANGE_PASSWORD_RESPONSE_LATENCY.withEndpointPrefix()),
                        anyDouble());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS
                                .withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertChangePasswordHeaders(httpRequestCaptor);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenChangePasswordEndpointDoesNotRespond()
            throws IOException {

        Exception exceptionCaught = new IOException("Change Password Endpoint Timed out");

        doThrow(exceptionCaught)
                .when(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangePasswordHttpRetryStatusConfig.class));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                changePasswordService.sendPasswordChangeRequest(
                                        "NEW_PASSWORD", Strategy.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Change Password
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangePasswordHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        eq(DVLA_CHANGE_PASSWORD_RESPONSE_LATENCY.withEndpointPrefix()),
                        anyDouble());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_CHANGE_PASSWORD_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(
                                exceptionCaught));
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenChangePasswordEndpointResponseStatusCodeNot200()
            throws IOException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // change password response but status not 200
        CloseableHttpResponse changePasswordResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "Server Error", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(ChangePasswordHttpRetryStatusConfig.class)))
                .thenReturn(changePasswordResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse
                                .ERROR_CHANGE_PASSWORD_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                changePasswordService.sendPasswordChangeRequest(
                                        "NEW_PASSWORD", Strategy.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Change Password
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(ChangePasswordHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        eq(DVLA_CHANGE_PASSWORD_RESPONSE_LATENCY.withEndpointPrefix()),
                        anyDouble());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS
                                .withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_CHANGE_PASSWORD_RESPONSE_STATUS_CODE_ALERT_METRIC
                                .withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenFailingToCreateChangePasswordRequestBody()
            throws IOException {

        ObjectMapper spyObjectMapper = Mockito.spy(new ObjectMapper());

        // Just for this test so we can inject use a spy to inject exceptions
        ChangePasswordService thisTestOnlyChangePasswordService =
                changePasswordService =
                        new ChangePasswordService(
                                mockDvlaConfiguration,
                                mockHttpRetryer,
                                mockRequestConfig,
                                spyObjectMapper,
                                mockEventProbe);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PREPARE_CHANGE_PASSWORD_REQUEST_PAYLOAD);

        // The above form data is validated and mapped into another object,
        // preventing the JsonProcessingException from occurring.
        // This triggers the exception directly to ensure it is handled should the processing change
        when(spyObjectMapper.writeValueAsString(any(ChangePasswordPayload.class)))
                .thenThrow(
                        new InputCoercionException(
                                null, "Problem during json mapping", null, null));

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                thisTestOnlyChangePasswordService.sendPasswordChangeRequest(
                                        "NEW_PASSWORD", Strategy.NO_CHANGE),
                        "JsonProcessingException creating request body");

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturn200WhenChangePasswordServiceCalledWithLiveTestStrategy() throws IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse changePasswordResponse =
                HttpResponseFixtures.createHttpResponse(200, null, null, false);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(ChangePasswordHttpRetryStatusConfig.class)))
                .thenReturn(changePasswordResponse);

        assertDoesNotThrow(
                () ->
                        changePasswordService.sendPasswordChangeRequest(
                                "NEWPASSWORD", Strategy.LIVE));

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_CHANGE_PASSWORD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        eq(DVLA_CHANGE_PASSWORD_RESPONSE_LATENCY.withEndpointPrefix()),
                        anyDouble());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_CHANGE_PASSWORD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS
                                .withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertChangePasswordHeaders(httpRequestCaptor);
    }

    private void assertChangePasswordHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));
    }
}
