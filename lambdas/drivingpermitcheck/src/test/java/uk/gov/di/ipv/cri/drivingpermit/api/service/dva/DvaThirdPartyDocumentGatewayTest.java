package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.MyJwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DVACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DvaThirdPartyDocumentGatewayTest {
    public static final String SELF_SIGNED_ROOT_CERT = System.getenv("SELF_SIGNED_ROOT_CERT");
    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final int MOCK_HTTP_STATUS_CODE = -1;
    public static final String ACM_ROOT_CERT = System.getenv("ACM_ROOT_CERT");
    public static final String SAMPLE_PAYLOAD = System.getenv("SAMPLE_PAYLOAD");
    public static final String SIGNING_KEY_ID = System.getenv("SIGNING_KEY_ID");
    public static final String ENC_KEY_ID = System.getenv("ENC_KEY_ID");

    private DvaThirdPartyDocumentGateway dvaThirdPartyDocumentGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private DrivingPermitConfigurationService mockDrivingPermitConfigurationService;
    @Mock private DvaConfiguration mockDvaConfiguration;

    @Mock private HttpRetryer httpRetryer;
    @Mock private DvaCryptographyService dvaCryptographyService;
    @Mock private RequestHashValidator requestHashValidator;
    @Mock private EventProbe mockEventProbe;

    // To be removed
    @RegisterExtension
    static WireMockExtension wm1 =
            WireMockExtension.newInstance()
                    .options(
                            wireMockConfig()
                                    .httpsPort(8443)
                                    .httpDisabled(true)
                                    .keystorePath("utils/v2/certs/wiremock-DVA-cert.jks")
                                    .keystorePassword("password")
                                    .keyManagerPassword("password")
                                    .keystoreType("JKS")
                                    .needClientAuth(true)
                                    // Only works with absolute file path
                                    // .trustStorePath("utils/v2/certs/myKeystore.jks")
                                    .trustStorePassword("password")
                                    .trustStoreType("JKS")
                                    .stubRequestLoggingDisabled(false))
                    .build();

    @BeforeEach
    void setUp() {

        when(mockDrivingPermitConfigurationService.getDvaConfiguration())
                .thenReturn(mockDvaConfiguration);
        when(mockDvaConfiguration.getEndpointUri()).thenReturn(TEST_ENDPOINT_URL);

        this.dvaThirdPartyDocumentGateway =
                new DvaThirdPartyDocumentGateway(
                        mockObjectMapper,
                        dvaCryptographyService,
                        requestHashValidator,
                        mockDrivingPermitConfigurationService,
                        httpRetryer,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeThirdPartyAPI()
            throws IOException, InterruptedException, GeneralSecurityException, ParseException,
                    JOSEException, OAuthErrorResponseException, java.text.ParseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(200, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);
        when(this.dvaCryptographyService.unwrapDvaResponse(anyString()))
                .thenReturn(createSuccessDvaResponse());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);
        when(this.requestHashValidator.valid(any(DvaPayload.class), anyString(), anyBoolean()))
                .thenReturn(true);

        DocumentCheckResult actualDocumentCheckResult =
                dvaThirdPartyDocumentGateway.performDocumentCheck(
                        drivingPermitForm, Strategy.NO_CHANGE);

        assertEquals(
                TEST_ENDPOINT_URL + "/api/ukverify",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response()
            throws IOException, GeneralSecurityException, JOSEException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(300, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm, Strategy.NO_CHANGE);
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
            throws IOException, InterruptedException, GeneralSecurityException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(400, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm, Strategy.NO_CHANGE);
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
            throws IOException, InterruptedException, GeneralSecurityException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm, Strategy.NO_CHANGE);
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
            throws IOException, InterruptedException, GeneralSecurityException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(-1, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dvaThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm, Strategy.NO_CHANGE);
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
            throws IOException, InterruptedException, GeneralSecurityException, ParseException,
                    JOSEException, OAuthErrorResponseException, java.text.ParseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dvaCryptographyService.preparePayload(any(DvaPayload.class)))
                .thenReturn(jwsObject);
        when(this.requestHashValidator.valid(any(DvaPayload.class), anyString(), anyBoolean()))
                .thenReturn(true);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(200, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DvaHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);
        when(this.dvaCryptographyService.unwrapDvaResponse(anyString()))
                .thenReturn(createSuccessDvaResponse());

        DocumentCheckResult actualFraudCheckResult =
                dvaThirdPartyDocumentGateway.performDocumentCheck(
                        drivingPermitForm, Strategy.NO_CHANGE);

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
    @Tag("FullE2ECertTest")
    void shouldInvokeRealThirdPartyAPIWithAws() throws Exception {

        wm1.stubFor(
                post("/api/ukverify")
                        .willReturn(
                                aResponse()
                                        .withResponseBody(
                                                Body.ofBinaryOrText(
                                                        SAMPLE_PAYLOAD.getBytes(),
                                                        new ContentTypeHeader(
                                                                "application/jwt")))));

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generateDva();

        ServiceFactory serviceFactory = new ServiceFactory();
        Map<String, String> tlsMap = new HashMap<>();
        tlsMap.put("tlsRootCertificate-2023-11-13", SELF_SIGNED_ROOT_CERT);
        tlsMap.put("tlsIntermediateCertificate-2023-11-13", SELF_SIGNED_ROOT_CERT);

        ParameterStoreService parameterStoreService =
                Mockito.spy(
                        new ParameterStoreService(
                                serviceFactory.getClientProviderFactory().getSSMProvider()));
        when(parameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE, "DVA/HttpClient"))
                .thenReturn(tlsMap);
        when(parameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE, ParameterStoreParameters.DVA_ENDPOINT))
                .thenReturn("https://localhost:8443");

        DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration =
                new DvaCryptographyServiceConfiguration(parameterStoreService);

        String signingCertificatePath = "signingCert-acm.cer";
        X509Certificate signingCertificate = CryptoUtils.loadCertificate(signingCertificatePath);

        dvaCryptographyServiceConfiguration.setSigningCert(signingCertificate);
        KmsSigner kmsSigner =
                new KmsSigner(
                        SIGNING_KEY_ID,
                        signingCertificate,
                        serviceFactory.getClientProviderFactory().getKMSClient());
        JweKmsDecrypter jweKmsDecrypter =
                new JweKmsDecrypter(
                        ENC_KEY_ID, serviceFactory.getClientProviderFactory().getKMSClient());
        DVACloseableHttpClientFactory dvaCloseableHttpClientFactory =
                new DVACloseableHttpClientFactory();
        EventProbe eventProbe = new EventProbe();
        DocumentCheckResult actualDocumentCheckResult =
                new DvaThirdPartyDocumentGateway(
                                new ObjectMapper(),
                                new DvaCryptographyService(
                                        dvaCryptographyServiceConfiguration,
                                        kmsSigner,
                                        jweKmsDecrypter),
                                new RequestHashValidator(),
                                new DrivingPermitConfigurationService(
                                        parameterStoreService,
                                        new SecretsManagerService(
                                                serviceFactory
                                                        .getClientProviderFactory()
                                                        .getSecretsManagerClient())),
                                new HttpRetryer(
                                        dvaCloseableHttpClientFactory.getClient(
                                                parameterStoreService,
                                                serviceFactory.getApacheHTTPClientFactoryService(),
                                                new AcmCertificateService(
                                                        serviceFactory.getAcmClient()),
                                                true),
                                        eventProbe,
                                        1),
                                eventProbe)
                        .performDocumentCheck(drivingPermitForm, Strategy.NO_CHANGE);

        assertEquals(true, actualDocumentCheckResult.isValid());
    }

    @Test
    @Tag("FullE2ECertTest")
    void shouldCreateTrustStoreFromAcm() throws Exception {
        Certificate tlsRootCert = KeyCertHelper.getDecodedX509Certificate(ACM_ROOT_CERT);

        // Utility used to make cert into JKS for wiremock
        try (FileOutputStream fos =
                new FileOutputStream("src/test/resources/utils/v2/certs/mykeystore.jks")) {
            KeyStore trustStore = createTrustStore(new Certificate[] {tlsRootCert});

            trustStore.store(fos, "password".toCharArray());
        }
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

    // To be removed
    private KeyStore createTrustStore(Certificate[] certificates)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        int k = 0;
        for (Certificate cert : certificates) {
            k++;
            keyStore.setCertificateEntry("my-ca-" + k, cert);
        }
        return keyStore;
    }
}
