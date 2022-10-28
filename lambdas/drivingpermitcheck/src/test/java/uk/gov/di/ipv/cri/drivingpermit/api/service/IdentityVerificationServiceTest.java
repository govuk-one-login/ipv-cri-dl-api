package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;
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

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private ThirdPartyDocumentGateway mockThirdPartyGateway;
    @Mock private FormDataValidator formDataValidator;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private AuditService mockAuditService;
    @Mock private ConfigurationService configurationService;
    @Mock private ObjectMapper objectMapper;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        this.identityVerificationService =
                new IdentityVerificationService(
                        mockThirdPartyGateway,
                        formDataValidator,
                        mockContraindicationMapper,
                        mockAuditService,
                        configurationService,
                        objectMapper);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testFraudCheckResult = new DocumentCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyFraudCodes = new String[] {"sample-code"};
        String[] mappedFraudCodes = new String[] {"mapped-code"};
        testFraudCheckResult.setValid(true);
        when(formDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performDocumentCheck(drivingPermitForm))
                .thenReturn(testFraudCheckResult);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(drivingPermitForm);

        assertNotNull(result);
        verify(formDataValidator).validate(drivingPermitForm);
        verify(mockThirdPartyGateway).performDocumentCheck(drivingPermitForm);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided()
            throws OAuthHttpResponseExceptionWithErrorBody {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        List<String> validationErrors = List.of("validation error");
        when(formDataValidator.validate(drivingPermitForm))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            this.identityVerificationService.verifyIdentity(drivingPermitForm);
                        });

        final String EXPECTED_ERROR = String.valueOf(ErrorResponse.FORM_DATA_FAILED_VALIDATION);

        assertEquals(EXPECTED_ERROR, String.valueOf(e.getErrorResponse()));
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws IOException, InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        when(formDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performDocumentCheck(drivingPermitForm)).thenReturn(null);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(drivingPermitForm);

        assertNotNull(result);
        assertFalse(result.isExecutedSuccessfully());
        assertFalse(result.isVerified());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());
    }
}
