package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.TestDataCreator;

import javax.net.ssl.SSLSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.TestDataCreator.createSuccessDcsResponse;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDocumentGatewayTest {

    private static class ExperianGatewayConstructorArgs {
        private final ObjectMapper objectMapper;
        private final DcsCryptographyService dcsCryptographyService;
        private final ConfigurationService configurationService;
        private final HttpRetryer httpRetryer;

        private ExperianGatewayConstructorArgs(
                ObjectMapper objectMapper,
                DcsCryptographyService dcsCryptographyService,
                ConfigurationService configurationService,
                HttpRetryer httpRetryer) {

            this.objectMapper = objectMapper;
            this.dcsCryptographyService = dcsCryptographyService;
            this.httpRetryer = httpRetryer;
            this.configurationService = configurationService;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final int MOCK_HTTP_STATUS_CODE = -1;
    private ThirdPartyDocumentGateway thirdPartyDocumentGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService configurationService;
    @Mock private HttpRetryer httpRetryer;
    @Mock private DcsCryptographyService dcsCryptographyService;

    @BeforeEach
    void setUp() {
        lenient()
                .when(configurationService.getDcsEndpointUri())
                .thenReturn("https://test-endpoint.co.uk");
        this.thirdPartyDocumentGateway =
                new ThirdPartyDocumentGateway(
                        mockObjectMapper,
                        dcsCryptographyService,
                        configurationService,
                        httpRetryer);
    }

    @Test
    void shouldInvokeExperianApi()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());

        DocumentCheckResult actualDocumentCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(300);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        final String EXPECTED_ERROR = ThirdPartyDocumentGateway.HTTP_300_REDIRECT_MESSAGE + 300;

        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP400Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(400);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        final String EXPECTED_ERROR = ThirdPartyDocumentGateway.HTTP_400_CLIENT_REQUEST_ERROR + 400;

        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP500Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(500);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        final String EXPECTED_ERROR = ThirdPartyDocumentGateway.HTTP_500_SERVER_ERROR + 500;

        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnUnhandledHTTPResponse()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(-1);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        final String EXPECTED_ERROR =
                ThirdPartyDocumentGateway.HTTP_UNHANDLED_ERROR + MOCK_HTTP_STATUS_CODE;

        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @ParameterizedTest
    @MethodSource("getRetryStatusCodes") // Retry status codes
    void retryThirdPartyApiHTTPResponseForStatusCode(int initialStatusCodeResponse)
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm =
                TestDataCreator.createTestDrivingPermitForm(AddressType.CURRENT);

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload("")));
        when(this.mockObjectMapper.writeValueAsString(any(JWSObject.class))).thenReturn("");

        HttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertNotNull(actualFraudCheckResult);
        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/jose", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, ExperianGatewayConstructorArgs> testCases =
                Map.of(
                        "objectMapper must not be null",
                        new ExperianGatewayConstructorArgs(null, null, null, null),
                        "crossCoreApiConfig must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(DcsCryptographyService.class),
                                null,
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) ->
                        assertThrows(
                                NullPointerException.class,
                                () ->
                                        new ThirdPartyDocumentGateway(
                                                constructorArgs.objectMapper,
                                                constructorArgs.dcsCryptographyService,
                                                constructorArgs.configurationService,
                                                constructorArgs.httpRetryer),
                                errorMessage));
    }

    private HttpResponse<String> createMockApiResponse(int statusCode) {

        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return TEST_API_RESPONSE_BODY;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

    private static Stream<Integer> getRetryStatusCodes() {
        Stream<Integer> retryStatusCodes = Stream.of(429);
        Stream<Integer> serverErrorRetryStatusCodes = IntStream.range(500, 599).boxed();
        return Stream.concat(retryStatusCodes, serverErrorRetryStatusCodes);
    }

    private HttpResponse createHttpResponse(int statusCode) {
        return new HttpResponse() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public Object body() {
                return "";
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}
