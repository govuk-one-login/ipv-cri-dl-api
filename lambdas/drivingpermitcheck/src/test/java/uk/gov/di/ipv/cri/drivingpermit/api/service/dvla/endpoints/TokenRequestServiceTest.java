package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.dynamo.TokenItem;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.TokenResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.TokenRequestService.TOKEN_ITEM_ID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_STATUS_CODE_ALERT_METRIC;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_TYPE_VALID;

@ExtendWith(MockitoExtension.class)
class TokenRequestServiceTest {

    private static final String TEST_END_POINT = "http://127.0.0.1";
    private static final String TEST_TOKEN_TABLE_NAME = "test_token_table_name";

    private static final String TEST_TOKEN_VALUE = "unit-test-token-value";

    private static final String TEST_USER_NAME = "TEST";
    private static final String TEST_PASSWORD = "PASSWORD";

    @Mock DvlaConfiguration mockDvlaConfiguration;
    @Mock DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock HttpRetryer mockHttpRetryer;
    @Mock private RequestConfig mockRequestConfig;
    private ObjectMapper realObjectMapper;
    @Mock private EventProbe mockEventProbe;

    private TokenRequestService tokenRequestService;

    @Mock DynamoDbTable<TokenItem> mockTokenTable; // To mock the internals of Datastore
    private final Key TOKEN_ITEM_KEY = Key.builder().partitionValue(TOKEN_ITEM_ID).build();

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockDvlaConfiguration.getTokenEndpoint()).thenReturn(TEST_END_POINT);
        when(mockDvlaConfiguration.getTokenTableName()).thenReturn(TEST_TOKEN_TABLE_NAME);
        when(mockDvlaConfiguration.getUsername()).thenReturn(TEST_USER_NAME);
        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

        // Datastore is wrapper around DynamoDbEnhancedClient
        when(mockDynamoDbEnhancedClient.table(eq(TEST_TOKEN_TABLE_NAME), any(TableSchema.class)))
                .thenReturn(mockTokenTable);

        tokenRequestService =
                new TokenRequestService(
                        mockDvlaConfiguration,
                        mockDynamoDbEnhancedClient,
                        mockHttpRetryer,
                        mockRequestConfig,
                        realObjectMapper,
                        mockEventProbe);
    }

    @Test
    void shouldReturnTokenValueWhenTokenEndpointRespondsWithToken()
            throws OAuthErrorResponseException, IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        TokenResponse testTokenResponse = TokenResponse.builder().idToken(TEST_TOKEN_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // Bearer access token
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, testTokenResponseString, false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);

        String tokenValue = tokenRequestService.requestToken(false);

        // (POST) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(tokenValue);
        assertEquals(TEST_TOKEN_VALUE, tokenValue);
        // Check Headers
        assertTokenHeaders(httpRequestCaptor);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenTokenEndpointDoesNotRespond()
            throws IOException {

        Exception exceptionCaught = new IOException("Token Endpoint Timed out");

        doThrow(exceptionCaught)
                .when(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> tokenRequestService.requestToken(true),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_TOKEN_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(
                                exceptionCaught));
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenTokenEndpointResponseStatusCodeNot200()
            throws IOException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Bearer access token but status not 200
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(501, null, "Server Error", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> tokenRequestService.requestToken(true),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @ParameterizedTest
    @CsvSource({
        "400", "401", // Status Codes where the alert metric is expected to be captured
    })
    void shouldCaptureTokenResponseStatusCodeAlertMetricWhenStatusCodeIs(
            int tokenResponseStatusCode) throws IOException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(
                        tokenResponseStatusCode, null, "Server Error", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> tokenRequestService.requestToken(true),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        // Token Status Code Alert
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_RESPONSE_STATUS_CODE_ALERT_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenFailingToMapTokenEndpointResponse()
            throws IOException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Invalid Response Body
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, "not-json", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> tokenRequestService.requestToken(true),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnCachedAccessTokenIfTokenNotExpired()
            throws IOException, OAuthErrorResponseException {

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Captor used as TTL is set and expiry enforced by the CRI
        ArgumentCaptor<TokenItem> dynamoPutItemTokenItemCaptor =
                ArgumentCaptor.forClass(TokenItem.class);

        // Bearer access token
        TokenResponse testTokenResponse = TokenResponse.builder().idToken(TEST_TOKEN_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, testTokenResponseString, false);

        // Request one
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(null);
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);
        // Token put capture
        doNothing().when(mockTokenTable).putItem(dynamoPutItemTokenItemCaptor.capture());
        String tokenResponseOne = tokenRequestService.requestToken(false);
        assertEquals(TEST_TOKEN_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();
        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo = tokenRequestService.requestToken(false);

        assertEquals(tokenResponseOne, tokenResponseTwo);

        // (Post) Token - Only one send invocation intended
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        // Times 1 here is important - token is cached
        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
    }

    @Test
    void shouldRequestNewAccessTokenIfCachedTokenIsExpired()
            throws IOException, OAuthErrorResponseException {

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Captor used as TTL is set and expiry enforced by the CRI
        ArgumentCaptor<TokenItem> dynamoPutItemTokenItemCaptor =
                ArgumentCaptor.forClass(TokenItem.class);

        // Bearer access token
        TokenResponse testTokenResponse = TokenResponse.builder().idToken(TEST_TOKEN_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, testTokenResponseString, false);

        // Request one
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(null);
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);
        // Token put capture
        doNothing().when(mockTokenTable).putItem(dynamoPutItemTokenItemCaptor.capture());
        String tokenResponseOne = tokenRequestService.requestToken(false);
        assertEquals(TEST_TOKEN_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();

        // Overriding the TokenItem TTL so it is expired and we should then make a new request
        testTokenFromDynamo.setTtl(Instant.now().getEpochSecond());

        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo = tokenRequestService.requestToken(false);

        assertEquals(tokenResponseOne, tokenResponseTwo);

        // (Post) Token - Two send invocations intended
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(2))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        // Times 1 here is important - token is cached
        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        // Request one
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        // Request Two
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
    }

    private void assertTokenHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));
    }
}
