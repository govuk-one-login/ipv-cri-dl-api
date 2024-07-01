package uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration;

import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class DvaCryptographyServiceConfiguration {

    public static final String DVA_JWS_PARAMETER_PATH = "DVA/JWS";

    public static final String MAP_KEY_SIGNING_CERT_FOR_DVA_TO_VERIFY = "signingCertForDvaToVerify";
    public static final String MAP_KEY_SIGNING_KEY_FOR_DRIVING_PERMIT_TO_SIGN =
            "signingKeyForDrivingPermitToSign";

    public static final String DVA_JWE_PARAMETER_PATH = "DVA/JWE";

    public static final String MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT =
            "encryptionCertForDrivingPermitToEncrypt-16-05-2024";
    public static final String MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY =
            "signingCertForDrivingPermitToVerify-16-05-2024";
    public static final String MAP_KEY_ENCRYPTION_KEY_FOR_DRIVING_PERMIT_TO_DECRYPT =
            "encryptionKeyForDrivingPermitToDecrypt";

    // JWS (Reply Signature)
    private Certificate signingCert;

    // JWE (Public Key)
    private final Certificate encryptionCert;

    // JWE encryptionCertThumbprints
    private final Thumbprints encryptionCertThumbprints;

    public DvaCryptographyServiceConfiguration(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException {

        /////////////////
        //// JWE Map ////
        /////////////////
        Map<String, String> dvaJWEmap =
                parameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE, DVA_JWE_PARAMETER_PATH);

        // JWE encryptionCertThumbprints
        X509Certificate encryptionX509Cert =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaJWEmap.get(MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT));
        encryptionCertThumbprints = KeyCertHelper.makeThumbprint(encryptionX509Cert);

        // JWE (Public Key)
        encryptionCert =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaJWEmap.get(MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT));

        // JWS (Reply Signature)
        signingCert =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaJWEmap.get(MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY));
    }

    public Certificate getEncryptionCert() {
        return encryptionCert;
    }

    public Certificate getSigningCert() {
        return signingCert;
    }

    public Thumbprints getEncryptionCertThumbprints() {
        return encryptionCertThumbprints;
    }

    // Should be used exclusively for testing
    public void setSigningCert(Certificate signingCert) {
        this.signingCert = signingCert;
    }
}
