package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDocumentGatewayTest {

    private static class ExperianGatewayConstructorArgs {
        private final ObjectMapper objectMapper;
        private final DcsCryptographyService dcsCryptographyService;
        private final ConfigurationService configurationService;
        private final HttpRetryer httpRetryer;
        private final EventProbe eventProbe;

        private ExperianGatewayConstructorArgs(
                ObjectMapper objectMapper,
                DcsCryptographyService dcsCryptographyService,
                ConfigurationService configurationService,
                HttpRetryer httpRetryer,
                EventProbe eventProbe) {

            this.objectMapper = objectMapper;
            this.dcsCryptographyService = dcsCryptographyService;
            this.httpRetryer = httpRetryer;
            this.configurationService = configurationService;
            this.eventProbe = eventProbe;
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

    @Mock private EventProbe mockEventProbe;

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
                        httpRetryer,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeThirdPartyAPI()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        DocumentCheckResult actualDocumentCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(300);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    thirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_30x.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
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

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(400);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    thirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_40x.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
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

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(500);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    thirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_50x.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
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

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(-1);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    thirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
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

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJWSSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());

        DocumentCheckResult actualFraudCheckResult =
                thirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertNotNull(actualFraudCheckResult);
        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, ExperianGatewayConstructorArgs> testCases =
                Map.of(
                        "objectMapper must not be null",
                        new ExperianGatewayConstructorArgs(null, null, null, null, null),
                        "crossCoreApiConfig must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(DcsCryptographyService.class),
                                null,
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
                                                constructorArgs.httpRetryer,
                                                constructorArgs.eventProbe),
                                errorMessage));
    }

    private static Stream<Integer> getRetryStatusCodes() {
        Stream<Integer> retryStatusCodes = Stream.of(429);
        Stream<Integer> serverErrorRetryStatusCodes = IntStream.range(500, 599).boxed();
        return Stream.concat(retryStatusCodes, serverErrorRetryStatusCodes);
    }

    private static DcsResponse createSuccessDcsResponse() {
        DcsResponse dcsResponse = new DcsResponse();
        dcsResponse.setCorrelationId("1234");
        dcsResponse.setRequestId("4321");
        dcsResponse.setValid(true);
        return dcsResponse;
    }

    private CloseableHttpResponse createHttpResponse(int statusCode) {
        return new CloseableHttpResponse() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String name) {
                return false;
            }

            @Override
            public Header[] getHeaders(String name) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String name) {
                if ("Accept".equals(name)) {
                    return new BasicHeader(name, "application/jose");
                } else {
                    return new BasicHeader(name, "application/jose");
                }
            }

            @Override
            public Header getLastHeader(String name) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {}

            @Override
            public void addHeader(String name, String value) {}

            @Override
            public void setHeader(Header header) {}

            @Override
            public void setHeader(String name, String value) {}

            @Override
            public void setHeaders(Header[] headers) {}

            @Override
            public void removeHeader(Header header) {}

            @Override
            public void removeHeaders(String name) {}

            @Override
            public HeaderIterator headerIterator() {
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String name) {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams params) {}

            @Override
            public StatusLine getStatusLine() {
                return new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return statusCode;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return null;
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusline) {}

            @Override
            public void setStatusLine(ProtocolVersion ver, int code) {}

            @Override
            public void setStatusLine(ProtocolVersion ver, int code, String reason) {}

            @Override
            public void setStatusCode(int code) throws IllegalStateException {}

            @Override
            public void setReasonPhrase(String reason) throws IllegalStateException {}

            @Override
            public HttpEntity getEntity() {
                return new HttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        return false;
                    }

                    @Override
                    public boolean isChunked() {
                        return false;
                    }

                    @Override
                    public long getContentLength() {
                        return 0;
                    }

                    @Override
                    public Header getContentType() {
                        return null;
                    }

                    @Override
                    public Header getContentEncoding() {
                        return null;
                    }

                    @Override
                    public InputStream getContent()
                            throws IOException, UnsupportedOperationException {
                        String initialString = "";
                        InputStream targetStream =
                                new ByteArrayInputStream(initialString.getBytes());
                        return targetStream;
                    }

                    @Override
                    public void writeTo(OutputStream outStream) throws IOException {}

                    @Override
                    public boolean isStreaming() {
                        return false;
                    }

                    @Override
                    public void consumeContent() throws IOException {}
                };
            }

            @Override
            public void setEntity(HttpEntity entity) {}

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale loc) {}

            @Override
            public void close() throws IOException {}
        };
    }

    private static class MyJWSSigner implements JWSSigner {
        @Override
        public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
            return new Base64URL("base64Url");
        }

        @Override
        public Set<JWSAlgorithm> supportedJWSAlgorithms() {
            HashSet<JWSAlgorithm> hashSet = new HashSet<>();
            hashSet.add(JWSAlgorithm.EdDSA);
            return hashSet;
        }

        @Override
        public JCAContext getJCAContext() {
            return new JCAContext();
        }
    }
}
