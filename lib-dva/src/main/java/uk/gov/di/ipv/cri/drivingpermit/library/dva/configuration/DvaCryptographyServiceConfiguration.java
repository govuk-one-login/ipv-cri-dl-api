package uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration;

import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;

public class DvaCryptographyServiceConfiguration {

    public static final String DVA_JWS_PARAMETER_PATH = "DVA/JWS";

    public static final String DVA_JWE_PARAMETER_PATH = "DVA/JWE";

    public static final String MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT =
            "encryptionCertForDrivingPermitToEncrypt-16-05-2024";
    public static final String MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY =
            "signingCertForDrivingPermitToVerify-16-05-2024";

    // JWS (Reply Signature)
    private Certificate signingCert;

    // JWE (Public Key)
    private final Certificate encryptionCert;

    private final String kmsSigningKeyId;
    private final String kmsEncryptionKeyId;

    private final String secondaryKmsSigningKeyId;
    private final String secondaryKmsEncryptionKeyId;

    private final String hasCA;

    public DvaCryptographyServiceConfiguration(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException {

        /////////////////
        //// JWE Map ////
        /////////////////
        Map<String, String> dvaJWEmap =
                parameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE, DVA_JWE_PARAMETER_PATH);

        // JWE (Public Key)
        encryptionCert =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaJWEmap.get(MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT));

        // JWS (Reply Signature)
        signingCert =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaJWEmap.get(MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY));

        kmsSigningKeyId = System.getenv("PRIMARY_SIGNING_KEY_ID");
        kmsEncryptionKeyId = System.getenv("PRIMARY_ENCRYPTION_KEY_ID");

        secondaryKmsSigningKeyId = System.getenv("SECONDARY_SIGNING_KEY_ID");
        secondaryKmsEncryptionKeyId = System.getenv("SECONDARY_ENCRYPTION_KEY_ID");
        hasCA = System.getenv("HAS_CA");
    }

    public Certificate getEncryptionCert() {
        return encryptionCert;
    }

    public Certificate getSigningCert() {
        return signingCert;
    }

    // Should be used exclusively for testing
    public void setSigningCert(Certificate signingCert) {
        this.signingCert = signingCert;
    }

    public String getKmsSigningKeyId() {
        return kmsSigningKeyId;
    }

    public String getKmsEncryptionKeyId() {
        return kmsEncryptionKeyId;
    }

    public String getSecondaryKmsSigningKeyId() {
        return secondaryKmsSigningKeyId;
    }

    public String getSecondaryKmsEncryptionKeyId() {
        return secondaryKmsEncryptionKeyId;
    }

    public String getHasCA() {
        return hasCA;
    }
}
