package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DCS_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private FormDataValidator mockFormDataValidator;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private AuditService mockAuditService;
    @Mock private ConfigurationService configurationService;
    @Mock private ObjectMapper objectMapper;
    @Mock private EventProbe mockEventProbe;

    @Mock private ThirdPartyAPIService mockThirdPartyAPIService;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        this.identityVerificationService =
                new IdentityVerificationService(
                        mockFormDataValidator, mockContraindicationMapper, mockEventProbe);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody {

        this.identityVerificationService =
                new IdentityVerificationService(
                        mockFormDataValidator, mockContraindicationMapper, mockEventProbe);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testFraudCheckResult = new DocumentCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyFraudCodes = new String[] {"sample-code"};
        String[] mappedFraudCodes = new String[] {"mapped-code"};
        testFraudCheckResult.setValid(true);
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyAPIService.performDocumentCheck(drivingPermitForm))
                .thenReturn(testFraudCheckResult);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        drivingPermitForm, mockThirdPartyAPIService);

        assertNotNull(result);
        verify(mockFormDataValidator).validate(drivingPermitForm);
        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_PASS);
        verify(mockThirdPartyAPIService).performDocumentCheck(drivingPermitForm);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided()
            throws OAuthHttpResponseExceptionWithErrorBody {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        List<String> validationErrors = List.of("validation error");
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            this.identityVerificationService.verifyIdentity(
                                    drivingPermitForm, mockThirdPartyAPIService);
                        });

        final String EXPECTED_ERROR = String.valueOf(ErrorResponse.FORM_DATA_FAILED_VALIDATION);

        assertEquals(EXPECTED_ERROR, String.valueOf(e.getErrorResponse()));

        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_FAIL);
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws IOException, InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyAPIService.performDocumentCheck(drivingPermitForm)).thenReturn(null);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        drivingPermitForm, mockThirdPartyAPIService);

        assertNotNull(result);
        assertFalse(result.isExecutedSuccessfully());
        assertFalse(result.isVerified());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());

        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_PASS);
        verify(mockEventProbe).counterMetric(DCS_CHECK_REQUEST_FAILED);
    }

    //    @Test
    //    void verifyDvaDirectEnabledParameterRoutesUsersToDvaWhenTrue()
    //            throws IOException, InterruptedException, CertificateException, ParseException,
    //                    JOSEException, OAuthHttpResponseExceptionWithErrorBody {
    //        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
    //            Logger mockedStaticLogger = mock(Logger.class);
    //            mockedLogManager.when(LogManager::getLogger).thenReturn(mockedStaticLogger);
    //
    //            when(configurationService.getDvaDirectEnabled()).thenReturn(true);
    //            this.identityVerificationService =
    //                    new IdentityVerificationService(
    //                            mockFormDataValidator, mockContraindicationMapper,
    // mockEventProbe);
    //            DrivingPermitForm drivingPermitForm =
    // DrivingPermitFormTestDataGenerator.generate();
    //            DocumentCheckResult testFraudCheckResult = new DocumentCheckResult();
    //            testFraudCheckResult.setExecutedSuccessfully(true);
    //            String[] thirdPartyFraudCodes = new String[] {"sample-code"};
    //            String[] mappedFraudCodes = new String[] {"mapped-code"};
    //            testFraudCheckResult.setValid(true);
    //            when(mockFormDataValidator.validate(drivingPermitForm))
    //                    .thenReturn(ValidationResult.createValidResult());
    //            when(mockThirdPartyGateway.performDocumentCheck(drivingPermitForm))
    //                    .thenReturn(testFraudCheckResult);
    //
    //            DocumentCheckVerificationResult result =
    //                    this.identityVerificationService.verifyIdentity(
    //                            drivingPermitForm, mockThirdPartyGateway);
    //
    //            assertNotNull(result);
    //            verify(mockFormDataValidator).validate(drivingPermitForm);
    //            verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_PASS);
    //            /*
    //            TODO: This below line will need to be updated to new performDvaDocumentCheck
    // method once
    //            created, also remove the verify logline and make logger in class static again
    //             */
    //
    //            verify(mockThirdPartyGateway).performDocumentCheck(drivingPermitForm);
    //            verify(mockedStaticLogger).info("Performing document check (DVA direct)");
    //        }
    //    }
}
