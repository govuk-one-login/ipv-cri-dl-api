package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.dynamo.TokenItem;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.TokenResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLATokenExpiryWindowException;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService.TOKEN_ITEM_ID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_TOKEN_RESPONSE_LATENCY;
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
    private static final Key TOKEN_ITEM_KEY =
            Key.builder().partitionValue(Strategy.NO_CHANGE.name() + TOKEN_ITEM_ID).build();

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockDvlaConfiguration.getTokenEndpoint()).thenReturn(TEST_END_POINT);
        when(mockDvlaConfiguration.getTokenTableName()).thenReturn(TEST_TOKEN_TABLE_NAME);
        when(mockDvlaConfiguration.getUsername()).thenReturn(TEST_USER_NAME);

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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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

        String tokenValue = tokenRequestService.requestToken(false, Strategy.NO_CHANGE);

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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
                        () -> tokenRequestService.requestToken(true, Strategy.STUB),
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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
                        () -> tokenRequestService.requestToken(true, Strategy.NO_CHANGE),
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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
                        () -> tokenRequestService.requestToken(true, Strategy.NO_CHANGE),
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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
                        () -> tokenRequestService.requestToken(true, Strategy.NO_CHANGE),
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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
        String tokenResponseOne = tokenRequestService.requestToken(false, Strategy.NO_CHANGE);
        assertEquals(TEST_TOKEN_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();
        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo = tokenRequestService.requestToken(false, Strategy.NO_CHANGE);

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
                .verify(mockEventProbe)
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

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
        String tokenResponseOne = tokenRequestService.requestToken(false, Strategy.NO_CHANGE);
        assertEquals(TEST_TOKEN_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();

        // Overriding the TokenItem TTL so it is expired and we should then make a new request
        testTokenFromDynamo.setTtl(Instant.now().getEpochSecond());

        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo = tokenRequestService.requestToken(false, Strategy.NO_CHANGE);

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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
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
                .counterMetric(eq(DVLA_TOKEN_RESPONSE_LATENCY.withEndpointPrefix()), anyDouble());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(DVLA_TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
    }

    @Test
    void shouldThrowDVLATokenExpiryWindowExceptionWhenExpiryWindowIsZero() {
        TokenItem tokenItem = new TokenItem(TEST_TOKEN_VALUE);
        tokenItem.setTtl(Instant.now().plusSeconds(3000).getEpochSecond());

        DVLATokenExpiryWindowException thrownException =
                assertThrows(
                        DVLATokenExpiryWindowException.class,
                        () -> tokenRequestService.isTokenNearExpiration(tokenItem, 0),
                        "Expected DVLATokenExpiryWindowException");

        assertEquals(
                TokenRequestService.INVALID_EXPIRY_WINDOW_ERROR_MESSAGE,
                thrownException.getMessage());
    }

    @Test
    void shouldThrowDVLATokenExpiryWindowExceptionWhenExpiryWindowIsNegative() {
        TokenItem tokenItem = new TokenItem(TEST_TOKEN_VALUE);
        tokenItem.setTtl(Instant.now().plusSeconds(3000).getEpochSecond());

        DVLATokenExpiryWindowException thrownException =
                assertThrows(
                        DVLATokenExpiryWindowException.class,
                        () -> tokenRequestService.isTokenNearExpiration(tokenItem, -1),
                        "Expected DVLATokenExpiryWindowException");

        assertEquals(
                TokenRequestService.INVALID_EXPIRY_WINDOW_ERROR_MESSAGE,
                thrownException.getMessage());
    }

    @Test
    void shouldThrowDVLATokenExpiryWindowExceptionWhenExpiryWindowExceedsTtl() {
        TokenItem tokenItem = new TokenItem(TEST_TOKEN_VALUE);
        tokenItem.setTtl(Instant.now().plusSeconds(3000).getEpochSecond());

        // TOKEN_ITEM_TTL_SECS = 3600 - 300 = 3300
        DVLATokenExpiryWindowException thrownException =
                assertThrows(
                        DVLATokenExpiryWindowException.class,
                        () -> tokenRequestService.isTokenNearExpiration(tokenItem, 3300),
                        "Expected DVLATokenExpiryWindowException");

        assertEquals(
                TokenRequestService.INVALID_EXPIRY_WINDOW_ERROR_MESSAGE,
                thrownException.getMessage());
    }

    @Test
    void shouldReturnFalseWhenTokenIsNotNearExpiration() {
        TokenItem tokenItem = new TokenItem(TEST_TOKEN_VALUE);
        tokenItem.setTtl(Instant.now().plusSeconds(3000).getEpochSecond());

        assertFalse(tokenRequestService.isTokenNearExpiration(tokenItem, 300));
    }

    @Test
    void shouldReturnTrueWhenTokenIsNearExpiration() {
        TokenItem tokenItem = new TokenItem(TEST_TOKEN_VALUE);
        tokenItem.setTtl(Instant.now().getEpochSecond());

        assertTrue(tokenRequestService.isTokenNearExpiration(tokenItem, 300));
    }

    @Test
    void shouldRemoveTokenItemFromTable() {
        tokenRequestService.removeTokenItem(Strategy.NO_CHANGE);

        Key expectedKey =
                Key.builder().partitionValue(Strategy.NO_CHANGE.name() + TOKEN_ITEM_ID).build();
        InOrder inOrderMockTokenTable = inOrder(mockTokenTable);
        inOrderMockTokenTable.verify(mockTokenTable).deleteItem(expectedKey);
        verifyNoMoreInteractions(mockTokenTable);
    }

    @Test
    void shouldRequestNewTokenWithStrategyBasedEndpoint()
            throws OAuthErrorResponseException, IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        String stubEndpoint = "http://stub-endpoint";
        String tokenPath = "/token";
        when(mockDvlaConfiguration.getEndpointURLs())
                .thenReturn(Map.of(Strategy.STUB.name(), stubEndpoint));
        when(mockDvlaConfiguration.getTokenPath()).thenReturn(tokenPath);

        TokenResponse testTokenResponse = TokenResponse.builder().idToken(TEST_TOKEN_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, testTokenResponseString, false);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);

        TokenResponse result =
                tokenRequestService.performNewTokenRequest(TEST_PASSWORD, Strategy.STUB);

        assertNotNull(result);
        assertEquals(TEST_TOKEN_VALUE, result.getIdToken());
        assertEquals(stubEndpoint + tokenPath, httpRequestCaptor.getValue().getURI().toString());
    }

    @Test
    void shouldRequestNewTokenWhenAlwaysRequestNewTokenOverrideEnabledWithCachedToken()
            throws OAuthErrorResponseException, IOException {

        when(mockDvlaConfiguration.getPassword()).thenReturn(TEST_PASSWORD);

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        ArgumentCaptor<TokenItem> dynamoPutItemTokenItemCaptor =
                ArgumentCaptor.forClass(TokenItem.class);

        TokenResponse testTokenResponse = TokenResponse.builder().idToken(TEST_TOKEN_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);
        CloseableHttpResponse tokenResponse =
                HttpResponseFixtures.createHttpResponse(200, null, testTokenResponseString, false);

        // Cached token exists and is not expired
        TokenItem cachedToken = new TokenItem("old-cached-token");
        cachedToken.setId(Strategy.NO_CHANGE.name() + TOKEN_ITEM_ID);
        cachedToken.setTtl(Instant.now().plusSeconds(3000).getEpochSecond());
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(cachedToken);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(TokenHttpRetryStatusConfig.class)))
                .thenReturn(tokenResponse);
        doNothing().when(mockTokenTable).putItem(dynamoPutItemTokenItemCaptor.capture());

        // alwaysRequestNewToken = true should override cached token
        String tokenValue = tokenRequestService.requestToken(true, Strategy.NO_CHANGE);

        assertEquals(TEST_TOKEN_VALUE, tokenValue);

        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenFailingToSerialiseTokenRequestPayload()
            throws IOException {

        ObjectMapper mockObjectMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialisation error") {});

        // Rebuild service with mock ObjectMapper
        when(mockDvlaConfiguration.getTokenEndpoint()).thenReturn(TEST_END_POINT);
        when(mockDvlaConfiguration.getTokenTableName()).thenReturn(TEST_TOKEN_TABLE_NAME);
        when(mockDvlaConfiguration.getUsername()).thenReturn(TEST_USER_NAME);

        TokenRequestService serviceWithMockMapper =
                new TokenRequestService(
                        mockDvlaConfiguration,
                        mockDynamoDbEnhancedClient,
                        mockHttpRetryer,
                        mockRequestConfig,
                        mockObjectMapper,
                        mockEventProbe);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PREPARE_TOKEN_REQUEST_PAYLOAD);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                serviceWithMockMapper.performNewTokenRequest(
                                        TEST_PASSWORD, Strategy.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
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
