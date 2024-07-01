package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.encryption.EncryptionCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.JwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.SigningCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DOCUMENT_DATA_VERIFICATION_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private FormDataValidator mockFormDataValidator;
    @Mock private AuditService mockAuditService;
    @Mock private DrivingPermitConfigurationService drivingPermitConfigurationService;
    @Mock private ObjectMapper objectMapper;
    @Mock private EventProbe mockEventProbe;

    @Mock private ThirdPartyAPIService mockThirdPartyAPIService;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        this.identityVerificationService =
                new IdentityVerificationService(mockFormDataValidator, mockEventProbe);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws OAuthErrorResponseException {

        this.identityVerificationService =
                new IdentityVerificationService(mockFormDataValidator, mockEventProbe);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testFraudCheckResult = new DocumentCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyFraudCodes = new String[] {"sample-code"};
        String[] mappedFraudCodes = new String[] {"mapped-code"};
        testFraudCheckResult.setValid(true);
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyAPIService.performDocumentCheck(drivingPermitForm, Strategy.NO_CHANGE))
                .thenReturn(testFraudCheckResult);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        drivingPermitForm, mockThirdPartyAPIService, Strategy.NO_CHANGE);

        assertNotNull(result);
        verify(mockFormDataValidator).validate(drivingPermitForm);
        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_PASS);
        verify(mockEventProbe).counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_SUCCEEDED);
        verify(mockThirdPartyAPIService)
                .performDocumentCheck(drivingPermitForm, Strategy.NO_CHANGE);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided() {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        List<String> validationErrors = List.of("validation error");
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        OAuthErrorResponseException e =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> {
                            this.identityVerificationService.verifyIdentity(
                                    drivingPermitForm,
                                    mockThirdPartyAPIService,
                                    Strategy.NO_CHANGE);
                        });

        final String EXPECTED_ERROR = String.valueOf(ErrorResponse.FORM_DATA_FAILED_VALIDATION);

        assertEquals(EXPECTED_ERROR, String.valueOf(e.getErrorResponse()));

        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_FAIL);
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws OAuthErrorResponseException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        when(mockFormDataValidator.validate(drivingPermitForm))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyAPIService.performDocumentCheck(drivingPermitForm, Strategy.NO_CHANGE))
                .thenReturn(null);

        DocumentCheckVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        drivingPermitForm, mockThirdPartyAPIService, Strategy.NO_CHANGE);

        assertNotNull(result);
        assertFalse(result.isExecutedSuccessfully());
        assertFalse(result.isVerified());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());

        verify(mockEventProbe).counterMetric(FORM_DATA_VALIDATION_PASS);
        verify(mockEventProbe).counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED);
    }
}
