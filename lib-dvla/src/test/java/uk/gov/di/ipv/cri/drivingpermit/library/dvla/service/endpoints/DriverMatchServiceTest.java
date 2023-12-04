package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.DvlaFormFields;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request.DvlaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.DriverMatchAPIResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.DriverMatchErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.errorresponse.fields.Errors;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.util.HttpResponseFixtures;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_RESPONSE_TYPE_VALID;

@ExtendWith(MockitoExtension.class)
class DriverMatchServiceTest {

    private static final String TEST_END_POINT = "http://127.0.0.1";

    // Real token will be a base64 encoded JWT
    private static final String TEST_TOKEN_VALUE =
            Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()).toString();
    private static final String TEST_API_KEY = UUID.randomUUID().toString();

    @Mock DvlaConfiguration mockDvlaConfiguration;
    @Mock HttpRetryer mockHttpRetryer;
    @Mock private RequestConfig mockRequestConfig;
    private ObjectMapper realObjectMapper;
    @Mock private EventProbe mockEventProbe;

    private DriverMatchService driverMatchService;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockDvlaConfiguration.getMatchEndpoint()).thenReturn(TEST_END_POINT);
        when(mockDvlaConfiguration.getApiKey()).thenReturn(TEST_API_KEY);

        driverMatchService =
                new DriverMatchService(
                        mockDvlaConfiguration,
                        mockHttpRetryer,
                        mockRequestConfig,
                        realObjectMapper,
                        mockEventProbe);
    }

    @ParameterizedTest
    @CsvSource({
        // Status, validDocument, Expected Validity
        "200, true, VALID", // Document data licence number found + data match
        "200, false, INVALID", // Document data licence number found + data mismatch
        "404, false, NOT_FOUND", // Document data licence number not found
    })
    void shouldReturnDriverMatchServiceResultWithValidityWhenPerformDriverMatchServiceReturns(
            int status, boolean validDocument, Validity expectedValidity)
            throws OAuthErrorResponseException, IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        String testDriverMatchAPIResponse;
        if (status == 200) {
            DriverMatchAPIResponse driverMatchAPIResponse =
                    DriverMatchAPIResponse.builder().validDocument(validDocument).build();
            testDriverMatchAPIResponse =
                    realObjectMapper.writeValueAsString(driverMatchAPIResponse);
        } else {
            Errors errors =
                    Errors.builder()
                            .code("ENQ018")
                            .status("404")
                            .detail("Driver number not found")
                            .build();

            DriverMatchErrorResponse driverMatchErrorResponse =
                    DriverMatchErrorResponse.builder().errors(List.of(errors)).build();
            testDriverMatchAPIResponse =
                    realObjectMapper.writeValueAsString(driverMatchErrorResponse);
        }

        CloseableHttpResponse driverMatchResponse =
                HttpResponseFixtures.createHttpResponse(
                        status, null, testDriverMatchAPIResponse, false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DriverMatchHttpRetryStatusConfig.class)))
                .thenReturn(driverMatchResponse);

        // Method arg
        DvlaFormFields dvlaFormFields = getTestData();

        DriverMatchServiceResult driverMatchServiceResult =
                driverMatchService.performMatch(dvlaFormFields, TEST_TOKEN_VALUE);

        // (POST)
        InOrder inOrderMockCloseableHttpClient = inOrder(mockHttpRetryer);
        inOrderMockCloseableHttpClient
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(driverMatchServiceResult);
        assertNotNull(driverMatchServiceResult.getValidity());
        assertEquals(expectedValidity, driverMatchServiceResult.getValidity());
        assertDriverMatchHeaders(httpRequestCaptor);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenFailingToCreateDriverMatchRequestBody()
            throws IOException {

        ObjectMapper spyObjectMapper = Mockito.spy(new ObjectMapper());

        // Just for this test so we can inject use a spy to inject exceptions
        DriverMatchService thisTestOnlyDriverMatchService =
                driverMatchService =
                        new DriverMatchService(
                                mockDvlaConfiguration,
                                mockHttpRetryer,
                                mockRequestConfig,
                                spyObjectMapper,
                                mockEventProbe);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PREPARE_MATCH_REQUEST_PAYLOAD);

        // Method arg
        DvlaFormFields dvlaFormFields = getTestData();

        // The above form data is validated and mapped into another object,
        // preventing the JsonProcessingException from occurring.
        // This triggers the exception directly to ensure it is handled should the processing change
        when(spyObjectMapper.writeValueAsString(any(DvlaPayload.class)))
                .thenThrow(
                        new InputCoercionException(
                                null, "Problem during json mapping", null, null));

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                thisTestOnlyDriverMatchService.performMatch(
                                        dvlaFormFields, TEST_TOKEN_VALUE),
                        "Expected OAuthErrorResponseException");

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenDriverMatchEndpointDoesNotRespond()
            throws IOException {
        Exception exceptionCaught = new IOException("DriverMatch Endpoint Timed out");

        doThrow(exceptionCaught)
                .when(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT);

        // Method arg
        DvlaFormFields dvlaFormFields = getTestData();

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> driverMatchService.performMatch(dvlaFormFields, TEST_TOKEN_VALUE),
                        "Expected OAuthErrorResponseException");

        // (Post)
        InOrder inOrderMockHttpClientSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpClientSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_MATCH_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(
                                exceptionCaught));
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenDriverMatchEndpointResponseStatusCodeNotValid()
            throws IOException {
        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        // Status not Valid
        DriverMatchAPIResponse driverMatchAPIResponse =
                DriverMatchAPIResponse.builder().validDocument(true).build();
        String testDriverMatchAPIResponse =
                realObjectMapper.writeValueAsString(driverMatchAPIResponse);

        CloseableHttpResponse driverMatchResponse =
                HttpResponseFixtures.createHttpResponse(
                        500, null, testDriverMatchAPIResponse, false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DriverMatchHttpRetryStatusConfig.class)))
                .thenReturn(driverMatchResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_MATCH_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        // Method arg
        DvlaFormFields dvlaFormFields = getTestData();

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> driverMatchService.performMatch(dvlaFormFields, TEST_TOKEN_VALUE),
                        "Expected OAuthErrorResponseException");

        // (Post) DriverMatch
        InOrder inOrderMockHttpClientSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpClientSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @ParameterizedTest
    @CsvSource({
        "200",
        "404" // The valid match response status codes
    })
    void shouldReturnOAuthErrorResponseExceptionWhenDriverMatchResponseCannotBeMapped(
            int statusCode) throws IOException {
        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        CloseableHttpResponse driverMatchResponse =
                HttpResponseFixtures.createHttpResponse(statusCode, null, "}BadJson{", false);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(), any(DriverMatchHttpRetryStatusConfig.class)))
                .thenReturn(driverMatchResponse);

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_MATCH_ENDPOINT_RESPONSE_BODY);

        // Method arg
        DvlaFormFields dvlaFormFields = getTestData();

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> driverMatchService.performMatch(dvlaFormFields, TEST_TOKEN_VALUE),
                        "Expected OAuthErrorResponseException");

        // (Post) DriverMatch
        InOrder inOrderMockHttpClientSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpClientSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(DVLA_MATCH_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    // Test disabled as 404 response validation is disabled
    //    @ParameterizedTest
    //    @CsvSource({
    //        "null_errors_list",
    //        "multiple_errors",
    //        "no_errors_in_list",
    //        "wrong_error_code",
    //    })
    //    void shouldReturnOAuthErrorResponseExceptionWhenDriverMatch404ResponseErrorsIsInvalid(
    //            String scenario) throws IOException {
    //        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
    //                ArgumentCaptor.forClass(HttpPost.class);
    //
    //        // Values for the test 404 error response
    //        String code = scenario.equals("wrong_error_code") ? "wrong_error_code" : "ENQ018";
    //        String status = "404";
    //        String detail = "Driver number not found";
    //
    //        Errors errors = Errors.builder().code(code).status(status).detail(detail).build();
    //
    //        List<Errors> errorsList;
    //
    //        switch (scenario) {
    //            case "null_errors_list":
    //                errorsList = null;
    //                break;
    //            case "no_errors_in_list":
    //                errorsList = new ArrayList<>();
    //                break;
    //            case "multiple_errors":
    //                errorsList = List.of(errors, errors); // two errors
    //                break;
    //            default:
    //                errorsList = List.of(errors);
    //        }
    //
    //        String testResponseBody =
    //                realObjectMapper.writeValueAsString(
    //                        DriverMatchErrorResponse.builder().errors(errorsList).build());
    //
    //        CloseableHttpResponse driverMatchResponse =
    //                HttpResponseFixtures.createHttpResponse(404, null, testResponseBody, false);
    //
    //        // HttpClient response
    //        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
    //                        httpRequestCaptor.capture(),
    // any(DriverMatchHttpRetryStatusConfig.class)))
    //                .thenReturn(driverMatchResponse);
    //
    //        OAuthErrorResponseException expectedReturnedException;
    //
    //        if (scenario.equals("wrong_error_code")) {
    //            expectedReturnedException =
    //                    new OAuthErrorResponseException(
    //                            HttpStatus.SC_INTERNAL_SERVER_ERROR,
    //                            ErrorResponse
    //
    // .MATCH_ENDPOINT_404_RESPONSE_EXPECTED_ERROR_CODE_NOT_CORRECT);
    //        } else {
    //            expectedReturnedException =
    //                    new OAuthErrorResponseException(
    //                            HttpStatus.SC_INTERNAL_SERVER_ERROR,
    //
    // ErrorResponse.MATCH_ENDPOINT_404_RESPONSE_EXPECTED_ERROR_SIZE_NOT_ONE);
    //        }
    //
    //        // Method arg
    //        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
    //
    //        OAuthErrorResponseException thrownException =
    //                assertThrows(
    //                        OAuthErrorResponseException.class,
    //                        () -> driverMatchService.performMatch(drivingPermitForm,
    // TEST_TOKEN_VALUE),
    //                        "Expected OAuthErrorResponseException");
    //
    //        // (Post) DriverMatch
    //        InOrder inOrderMockHttpClientSequence = inOrder(mockHttpRetryer);
    //        inOrderMockHttpClientSequence
    //                .verify(mockHttpRetryer, times(1))
    //                .sendHTTPRequestRetryIfAllowed(
    //                        any(HttpPost.class), any(DriverMatchHttpRetryStatusConfig.class));
    //        verifyNoMoreInteractions(mockHttpRetryer);
    //
    //        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
    //        inOrderMockEventProbeSequence
    //                .verify(mockEventProbe)
    //                .counterMetric(DVLA_MATCH_REQUEST_CREATED.withEndpointPrefix());
    //        inOrderMockEventProbeSequence
    //                .verify(mockEventProbe)
    //                .counterMetric(DVLA_MATCH_REQUEST_SEND_OK.withEndpointPrefix());
    //        inOrderMockEventProbeSequence
    //                .verify(mockEventProbe)
    //
    // .counterMetric(DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
    //        inOrderMockEventProbeSequence
    //                .verify(mockEventProbe)
    //                .counterMetric(DVLA_MATCH_RESPONSE_TYPE_INVALID.withEndpointPrefix());
    //        verifyNoMoreInteractions(mockEventProbe);
    //
    //        assertEquals(expectedReturnedException.getStatusCode(),
    // thrownException.getStatusCode());
    //        assertEquals(expectedReturnedException.getErrorReason(),
    // thrownException.getErrorReason());
    //    }

    private void assertDriverMatchHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));

        assertNotNull(httpHeadersKV.get("Authorization"));
        assertEquals(TEST_TOKEN_VALUE, httpHeadersKV.get("Authorization"));
        assertEquals(TEST_API_KEY, httpHeadersKV.get("X-API-Key"));
    }

    private DvlaFormFields getTestData() {

        return new DvlaFormFields() {
            private LocalDate issueDate = LocalDate.now().minusYears(5);
            private LocalDate expiryDate = issueDate.plusYears(10);

            @Override
            public String getDrivingLicenceNumber() {
                return "DrivingLicenceNumber";
            }

            @Override
            public String getSurname() {
                return "Surname";
            }

            @Override
            public String getIssueNumber() {
                return "1";
            }

            @Override
            public LocalDate getIssueDate() {
                return issueDate;
            }

            @Override
            public LocalDate getExpiryDate() {
                return expiryDate;
            }
        };
    }
}
