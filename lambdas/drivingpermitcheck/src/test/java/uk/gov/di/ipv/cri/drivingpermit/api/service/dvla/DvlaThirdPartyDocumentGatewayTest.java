// package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;
//
// import org.apache.http.HttpStatus;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.CsvSource;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
// import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
// import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result.DriverMatchServiceResult;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
// import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
// import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
// import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DVLA;
// import static
// uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_MATCH_ENDPOINT_REJECTED_TOKEN_OR_API_KEY;
//
// @ExtendWith(MockitoExtension.class)
// class DvlaThirdPartyDocumentGatewayTest {
//
//    @Mock private DvlaEndpointFactory mockDvlaEndpointFactory;
//    @Mock private TokenRequestService mockTokenRequestService;
//    @Mock private DriverMatchService mockDriverMatchService;
//
//    private ThirdPartyAPIService dvlaThirdPartyAPIService;
//
//    @BeforeEach
//    void setUp() {
//        // Mocks out the creation of all endpoints to allow mocking the endpoint responses without
//        // calling them for real
//
// when(mockDvlaEndpointFactory.getTokenRequestService()).thenReturn(mockTokenRequestService);
//
//        when(mockDvlaEndpointFactory.getDriverMatchService()).thenReturn(mockDriverMatchService);
//
//        dvlaThirdPartyAPIService = new DvlaThirdPartyDocumentGateway(mockDvlaEndpointFactory);
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//        "VALID, true", // Valid Number + Data match  = valid
//        "INVALID, false", // Valid Number + Data mismatch = not valid
//        "NOT_FOUND, false" // Number not found = not valid
//    })
//    void shouldReturnDocumentValidityGivenValidDataAndAllThirdPartyEndpointsRespond(
//            Validity validity, boolean expectedIsValid) throws OAuthErrorResponseException {
//
//        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
//
//        // Token Value
//        String testTokenValue = "TEST_TOKEN_VALUE";
//
//        String testApiKey = "EST_API_KEY";
//
//        // Generated a valid api response object to create the api response for this test
//        DriverMatchServiceResult testDriverMatchServiceResult =
//                DriverMatchServiceResult.builder().validity(validity).requestId("123456").build();
//
//        when(mockTokenRequestService.requestToken(any(Boolean.class))).thenReturn(testTokenValue);
//
//        when(mockDriverMatchService.performMatch(drivingPermitForm, testTokenValue, testApiKey))
//                .thenReturn(testDriverMatchServiceResult);
//
//        DocumentCheckResult result =
//                dvlaThirdPartyAPIService.performDocumentCheck(drivingPermitForm);
//
//        // Using the correct third party API?
//        assertEquals(
//                dvlaThirdPartyAPIService.getServiceName(),
//                DvlaThirdPartyDocumentGateway.class.getSimpleName());
//
//        assertNotNull(result);
//        assertNotNull(result.getTransactionId());
//        assertEquals(expectedIsValid, result.isValid());
//        assertEquals(DVLA, result.getApiResultSource());
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//        "true", // RecoverySuccessful after a 401 from match
//        "false", // RecoveryFail - match still has a 401 on the second attempt
//    })
//    void shouldAttemptRecovery(boolean recoverySucessful) throws OAuthErrorResponseException {
//
//        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
//
//        // Token Value
//        String testTokenValue = "TEST_TOKEN_VALUE";
//
//        String testApiKey = "API_KEY";
//
//        // Generated a valid api response object to create the api response for this test
//        DriverMatchServiceResult testDriverMatchServiceResult =
//                DriverMatchServiceResult.builder()
//                        .validity(Validity.VALID)
//                        .requestId("123456")
//                        .build();
//
//        when(mockTokenRequestService.requestToken(any(Boolean.class)))
//                .thenReturn(testTokenValue)
//                .thenReturn(testTokenValue); // second request
//
//        DVLAMatchUnauthorizedException exceptionCaught =
//                new DVLAMatchUnauthorizedException(
//                        ERROR_MATCH_ENDPOINT_REJECTED_TOKEN_OR_API_KEY.getMessage());
//
//        // Using the correct third party API?
//        assertEquals(
//                dvlaThirdPartyAPIService.getServiceName(),
//                DvlaThirdPartyDocumentGateway.class.getSimpleName());
//
//        if (recoverySucessful) {
//            // Match issue resolved via new token
//            when(mockDriverMatchService.performMatch(drivingPermitForm, testTokenValue,
// testApiKey))
//                    .thenThrow(exceptionCaught)
//                    .thenReturn(testDriverMatchServiceResult);
//
//            DocumentCheckResult result =
//                    dvlaThirdPartyAPIService.performDocumentCheck(drivingPermitForm);
//
//            assertNotNull(result);
//            assertNotNull(result.getTransactionId());
//            assertTrue(result.isValid());
//            assertEquals(DVLA, result.getApiResultSource());
//        } else {
//            // Match issue remains
//            when(mockDriverMatchService.performMatch(drivingPermitForm, testTokenValue,
// testApiKey))
//                    .thenThrow(exceptionCaught)
//                    .thenThrow(exceptionCaught);
//
//            OAuthErrorResponseException expectedReturnedException =
//                    new OAuthErrorResponseException(
//                            HttpStatus.SC_INTERNAL_SERVER_ERROR,
//                            ErrorResponse.ERROR_DVLA_EXPIRED_TOKEN_RECOVERY_FAILED);
//
//            OAuthErrorResponseException thrownException =
//                    assertThrows(
//                            OAuthErrorResponseException.class,
//                            () ->
// dvlaThirdPartyAPIService.performDocumentCheck(drivingPermitForm),
//                            "Expected OAuthErrorResponseException");
//
//            assertEquals(
//                    expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
//            assertEquals(
//                    expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
//        }
//    }
// }
