package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    // **************************** CRI ****************************

    // TODO

    // **************************** DCS ****************************

    public static final String DCS_ENDPOINT = "dcsEndpoint";

    public static final String DCS_HTTPCLIENT_TLS_CERT = "tlsCert-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_KEY = "tlsKey-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_INTER_CERT = "tlsIntermediateCertificate";

    public static final String DCS_HTTPCLIENT_TLS_ROOT_CERT = "tlsRootCertificate";

    // JWS SHA-1 Certificate - Thumbprint (Header)
    public static final String DCS_SIGNING_CERT_THUMB = "signingCertForDcsToVerify-2023-08-03";

    // JWS RSA Signing Key
    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "signingKeyForDrivingPermitToSign-2023-08-03";

    // JWE (Public Key) - not a typo
    public static final String DCS_ENCRYPTION_CERT = "encryptionCertForDrivingPermitToEncrypt";

    // JWE (Reply Signature)
    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "signingCertForDrivingPermitToVerify";

    // JWE (Private Key Reply Decrypt)
    public static final String DCS_DRIVING_PERMIT_ENCRYPTION_KEY =
            "encryptionKeyForDrivingPermitToDecrypt-2023-08-03"; // DCS JWE (Private Key

    // **************************** DVA ****************************

    public static final String DVA_ENDPOINT = "DVA/dvaEndpoint";

    public static final String DVA_USERNAME = "DVA/Username";

    public static final String DVA_PASSWORD = "DVA/Password";

    public static final String DVA_HTTPCLIENT_TLS_CERT = "DVA/HttpClient/tlsCert";

    public static final String DVA_HTTPCLIENT_TLS_KEY = "DVA/HttpClient/tlsKey";

    public static final String DVA_HTTPCLIENT_TLS_INTER_CERT =
            "DVA/HttpClient/tlsIntermediateCertificate";

    public static final String DVA_HTTPCLIENT_TLS_ROOT_CERT = "DVA/HttpClient/tlsRootCertificate";

    // JWS SHA-1 Certificate - Thumbprint (Header)
    public static final String DVA_SIGNING_CERT_THUMB = "DVA/JWS/signingCertForDvaToVerify";

    // JWS RSA Signing Key
    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "DVA/JWS/signingKeyForDrivingPermitToSign";

    // JWE (Public Key) - not a typo
    public static final String DVA_ENCRYPTION_CERT =
            "DVA/JWE/encryptionCertForDrivingPermitToEncrypt";

    // JWE (Reply Signature)
    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "DVA/JWE/signingCertForDrivingPermitToVerify";

    // JWE (Private Key Reply Decrypt)
    public static final String DVA_DRIVING_PERMIT_ENCRYPTION_KEY =
            "DVA/JWE/encryptionKeyForDrivingPermitToDecrypt";

    // **************************** DVLA ****************************

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
