package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testdata.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.dvla.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DVLA;

@ExtendWith(MockitoExtension.class)
class DvlaThirdPartyDocumentGatewayTest {

    @Mock private DvlaEndpointFactory mockDvlaEndpointFactory;
    @Mock private TokenRequestService mockTokenRequestService;
    @Mock private DriverMatchService mockDriverMatchService;

    private ThirdPartyAPIService dvlaThirdPartyAPIService;

    @BeforeEach
    void setUp() {
        // Mocks out the creation of all endpoints to allow mocking the endpoint responses without
        // calling them for real
        when(mockDvlaEndpointFactory.getTokenRequestService()).thenReturn(mockTokenRequestService);

        when(mockDvlaEndpointFactory.getDriverMatchService()).thenReturn(mockDriverMatchService);

        dvlaThirdPartyAPIService = new DvlaThirdPartyDocumentGateway(mockDvlaEndpointFactory);
    }

    @ParameterizedTest
    @CsvSource({
        "VALID, true", // Valid Number + Data match  = valid
        "INVALID, false", // Valid Number + Data mismatch = not valid
        "NOT_FOUND, false" // Number not found = not valid
    })
    void shouldReturnDocumentValidityGivenValidDataAndAllThirdPartyEndpointsRespond(
            Validity validity, boolean expectedIsValid) throws OAuthErrorResponseException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        // Token Value
        String testTokenValue = "TEST_TOKEN_VALUE";

        // Generated a valid api response object to create the api response for this test
        DriverMatchServiceResult testDriverMatchServiceResult =
                DriverMatchServiceResult.builder().validity(validity).requestId("123456").build();

        when(mockTokenRequestService.requestToken(any(Boolean.class))).thenReturn(testTokenValue);

        when(mockDriverMatchService.performMatch(drivingPermitForm, testTokenValue))
                .thenReturn(testDriverMatchServiceResult);

        DocumentCheckResult result =
                dvlaThirdPartyAPIService.performDocumentCheck(drivingPermitForm);

        // Using the correct third party API?
        assertEquals(
                dvlaThirdPartyAPIService.getServiceName(),
                DvlaThirdPartyDocumentGateway.class.getSimpleName());

        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertEquals(expectedIsValid, result.isValid());
        assertEquals(DVLA, result.getApiResultSource());
    }
}
