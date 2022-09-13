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
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.util.TestDataCreator;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private ThirdPartyDocumentGateway mockThirdPartyGateway;
    @Mock private PersonIdentityValidator personIdentityValidator;
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
                        personIdentityValidator,
                        mockContraindicationMapper,
                        mockAuditService,
                        configurationService,
                        objectMapper);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody {
        DrivingPermitForm drivingPermitForm = TestDataCreator.createTestDrivingPermitForm();
        DocumentCheckResult testFraudCheckResult = new DocumentCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyFraudCodes = new String[] {"sample-code"};
        String[] mappedFraudCodes = new String[] {"mapped-code"};
        testFraudCheckResult.setValid(true);
        when(personIdentityValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performDocumentCheck(drivingPermitForm))
                .thenReturn(testFraudCheckResult);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(drivingPermitForm);

        assertNotNull(result);
        verify(personIdentityValidator).validate(drivingPermitForm);
        verify(mockThirdPartyGateway).performDocumentCheck(drivingPermitForm);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided()
            throws OAuthHttpResponseExceptionWithErrorBody {
        DrivingPermitForm drivingPermitForm = TestDataCreator.createTestDrivingPermitForm();
        List<String> validationErrors = List.of("validation error");
        when(personIdentityValidator.validate(drivingPermitForm))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(drivingPermitForm);

        assertNotNull(result);
        assertNull(result.getContraIndicators());
        assertFalse(result.isSuccess());
        assertEquals(validationErrors.get(0), result.getValidationErrors().get(0));
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws IOException, InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException {
        DrivingPermitForm drivingPermitForm = TestDataCreator.createTestDrivingPermitForm();
        when(personIdentityValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performDocumentCheck(drivingPermitForm)).thenReturn(null);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(drivingPermitForm);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());
    }
}
