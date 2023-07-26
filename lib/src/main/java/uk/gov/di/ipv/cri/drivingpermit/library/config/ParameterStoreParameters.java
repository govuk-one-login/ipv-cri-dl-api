package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    public static final String DCS_HTTPCLIENT_TLS_CERT = "DCS/HttpClient/tlsCert";

    public static final String DCS_HTTPCLIENT_TLS_KEY = "DCS/HttpClient/tlsKey";

    public static final String DCS_HTTPCLIENT_TLS_INTER_CERT =
            "DCS/HttpClient/tlsIntermediateCertificate";

    public static final String DCS_HTTPCLIENT_TLS_ROOT_CERT = "DCS/HttpClient/tlsRootCertificate";

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "DCS/JWS/signingCertForDrivingPermitToVerify"; // JWS SHA-1 Certificate
    // Thumbprint (Header)

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "DCS/JWS/signingKeyForDrivingPermitToSign"; // JWS RSA Signing Key

    public static final String DCS_ENCRYPTION_CERT =
            "DCS/JWE/encryptionCertForDrivingPermitToEncrypt"; // JWE (Public Cert)

    public static final String DCS_SIGNING_CERT =
            "DCS/JWE/signingCertForDcsToVerify"; // DCS JWE (Reply Signature)

    public static final String DCS_DRIVING_PERMIT_ENCRYPTION_KEY =
            "DCS/JWE/encryptionKeyForDrivingPermitToDecrypt"; // DCS JWE (Private Key
    // Reply Decrypt)

    public static final String DVA_HTTPCLIENT_TLS_CERT = "DVA/HttpClient/tlsCert";

    public static final String DVA_HTTPCLIENT_TLS_KEY = "DVA/HttpClient/tlsKey";

    public static final String DVA_HTTPCLIENT_TLS_INTER_CERT =
            "DVA/HttpClient/tlsIntermediateCertificate";

    public static final String DVA_HTTPCLIENT_TLS_ROOT_CERT = "DVA/HttpClient/tlsRootCertificate";

    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "DVA/JWS/signingCertForDrivingPermitToVerify"; // JWS SHA-1 Certificate
    // Thumbprint (Header)

    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "DVA/JWS/signingKeyForDrivingPermitToSign"; // JWS RSA Signing Key

    public static final String DVA_ENCRYPTION_CERT =
            "DVA/JWE/encryptionCertForDrivingPermitToEncrypt"; // JWE (Public Cert)

    public static final String DVA_SIGNING_CERT =
            "DVA/JWE/signingCertForDvaToVerify"; // DVA JWE (Reply Signature)

    public static final String DVA_DRIVING_PERMIT_ENCRYPTION_KEY =
            "DVA/JWE/encryptionKeyForDrivingPermitToDecrypt"; // DVA JWE (Private Key
    // Reply Decrypt)

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
