package uk.gov.di.ipv.cri.drivingpermit.api.service.dcs;

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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.request.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DcsConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.util.MyJwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DcsThirdPartyDocumentGatewayTest {

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final int MOCK_HTTP_STATUS_CODE = -1;
    private DcsThirdPartyDocumentGateway dcsThirdPartyDocumentGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService mockConfigurationService;
    @Mock private DcsConfiguration mockDcsConfiguration;

    @Mock private HttpRetryer httpRetryer;
    @Mock private DcsCryptographyService dcsCryptographyService;

    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {

        when(mockConfigurationService.getDcsConfiguration()).thenReturn(mockDcsConfiguration);
        when(mockDcsConfiguration.getEndpointUri()).thenReturn(TEST_ENDPOINT_URL);

        this.dcsThirdPartyDocumentGateway =
                new DcsThirdPartyDocumentGateway(
                        mockObjectMapper,
                        dcsCryptographyService,
                        mockConfigurationService,
                        httpRetryer,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeThirdPartyAPI()
            throws IOException, CertificateException, ParseException, JOSEException,
                    OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(200, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        DocumentCheckResult actualDocumentCheckResult =
                dcsThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

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
            throws IOException, CertificateException, JOSEException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(300, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR =
                ErrorResponse.ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();
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
            throws IOException, CertificateException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(400, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR =
                ErrorResponse.ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();
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
            throws IOException, CertificateException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR =
                ErrorResponse.ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();
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
            throws IOException, CertificateException, JOSEException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(-1, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR =
                ErrorResponse.ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();
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
            throws IOException, CertificateException, ParseException, JOSEException,
                    OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse =
                HttpResponseFixtures.createHttpResponse(200, null, "", false);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DcsHttpRetryStatusConfig.class)))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());

        DocumentCheckResult actualFraudCheckResult =
                dcsThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertNotNull(actualFraudCheckResult);
        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
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
}
