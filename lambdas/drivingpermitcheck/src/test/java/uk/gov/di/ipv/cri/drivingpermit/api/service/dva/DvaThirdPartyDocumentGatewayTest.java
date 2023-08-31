package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HashFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.util.MyJwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.HttpResponseUtils.createHttpResponse;

@ExtendWith(MockitoExtension.class)
class DvaThirdPartyDocumentGatewayTest {

    private static class DVAGatewayConstructorArgs {
        private final ObjectMapper objectMapper;
        private final DvaCryptographyService dvaCryptographyService;
        private final RequestHashValidator requestHashValidator;
        private final ConfigurationService configurationService;
        private final HttpRetryer httpRetryer;
        private final EventProbe eventProbe;

        private DVAGatewayConstructorArgs(
                ObjectMapper objectMapper,
                DvaCryptographyService dvaCryptographyService,
                RequestHashValidator requestHashValidator,
                ConfigurationService configurationService,
                HttpRetryer httpRetryer,
                EventProbe eventProbe) {

            this.objectMapper = objectMapper;
            this.dvaCryptographyService = dvaCryptographyService;
            this.requestHashValidator = requestHashValidator;
            this.httpRetryer = httpRetryer;
            this.configurationService = configurationService;
            this.eventProbe = eventProbe;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final int MOCK_HTTP_STATUS_CODE = -1;
    private DvaThirdPartyDocumentGateway dvaThirdPartyDocumentGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService configurationService;
    @Mock private HttpRetryer httpRetryer;
    @Mock private DvaCryptographyService dvaCryptographyService;
    @Mock private RequestHashValidator requestHashValidator;
    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        lenient()
                .when(configurationService.getDvaEndpointUri())
                .thenReturn("https://test-endpoint.co.uk");
        this.dvaThirdPartyDocumentGateway =
                new DvaThirdPartyDocumentGateway(
                        mockObjectMapper,
                        dvaCryptographyService,
                        requestHashValidator,
                        configurationService,
                        httpRetryer,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeThirdPartyAPI()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dvaCryptographyService.unwrapDvaResponse(anyString()))
                .thenReturn(createSuccessDvaResponse());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);
        when(this.requestHashValidator.valid(any(DvaPayload.class), anyString(), anyBoolean())).thenReturn(true);

        DocumentCheckResult actualDocumentCheckResult =
                dvaThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHashValidation() throws NoSuchAlgorithmException {
        HashFactory hashFactory = new HashFactory();
        this.requestHashValidator = new RequestHashValidator(hashFactory);

        DvaPayload dvaPayload = new DvaPayload();
        dvaPayload.setSurname("Sur");
        dvaPayload.setForenames(Arrays.asList("Fore"));

        DvaResponse dvaResponse = new DvaResponse();
        dvaResponse.setRequestHash(hashFactory.getHash(dvaPayload, true) + "0");

        boolean isValidHash =
                this.requestHashValidator.valid(dvaPayload, dvaResponse.getRequestHash(), true);

        // Request Hash  = ad8[...]a5f
        // Response Hash = ad8[...]a5f0
        assertFalse(isValidHash);
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(300);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DVA_ERROR_HTTP_30X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP400Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(400);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DVA_ERROR_HTTP_400.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP500Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(500);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DVA_ERROR_HTTP_50X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnUnhandledHTTPResponse()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(-1);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DVA_ERROR_HTTP_X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @ParameterizedTest
    @MethodSource("getRetryStatusCodes") // Retry status codes
    void retryThirdPartyApiHTTPResponseForStatusCode(int initialStatusCodeResponse)
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);
        when(this.requestHashValidator.valid(any(DvaPayload.class), anyString(), anyBoolean())).thenReturn(true);

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dvaCryptographyService.unwrapDvaResponse(anyString()))
                .thenReturn(createSuccessDvaResponse());

        DocumentCheckResult actualFraudCheckResult =
                dvaThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertNotNull(actualFraudCheckResult);
        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, DVAGatewayConstructorArgs> testCases =
                Map.of(
                        "objectMapper must not be null",
                        new DVAGatewayConstructorArgs(null, null, null, null, null, null),
                        "crossCoreApiConfig must not be null",
                        new DVAGatewayConstructorArgs(
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(DvaCryptographyService.class),
                                null,
                                null,
                                null,
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) ->
                        assertThrows(
                                NullPointerException.class,
                                () ->
                                        new DvaThirdPartyDocumentGateway(
                                                constructorArgs.objectMapper,
                                                constructorArgs.dvaCryptographyService,
                                                constructorArgs.requestHashValidator,
                                                constructorArgs.configurationService,
                                                constructorArgs.httpRetryer,
                                                constructorArgs.eventProbe),
                                errorMessage));
    }

    private static Stream<Integer> getRetryStatusCodes() {
        Stream<Integer> retryStatusCodes = Stream.of(429);
        Stream<Integer> serverErrorRetryStatusCodes = IntStream.range(500, 599).boxed();
        return Stream.concat(retryStatusCodes, serverErrorRetryStatusCodes);
    }

    private static DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        // below is request hash for kenneth test user
        dvaResponse.setRequestHash(
                "5687a54991970a188bc670ab6e8f867c42216811d0a7d705b02d8e6e10d7ef84");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }
}
