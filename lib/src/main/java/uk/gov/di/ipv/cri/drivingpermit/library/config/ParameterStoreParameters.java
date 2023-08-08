package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    public static final String DCS_HTTPCLIENT_TLS_CERT = "tlsCert-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_KEY = "tlsKey-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_INTER_CERT = "tlsIntermediateCertificate";

    public static final String DCS_HTTPCLIENT_TLS_ROOT_CERT = "tlsRootCertificate";

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "signingCertForDrivingPermitToVerify"; // JWS SHA-1 Certificate
    // Thumbprint (Header)

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "signingKeyForDrivingPermitToSign-2023-08-03"; // JWS RSA Signing Key

    public static final String DCS_ENCRYPTION_CERT =
            "encryptionCertForDrivingPermitToEncrypt"; // JWE (Public Cert)

    public static final String DCS_SIGNING_CERT =
            "signingCertForDcsToVerify-2023-08-03"; // DCS JWE (Reply Signature)

    public static final String DCS_DRIVING_PERMIT_ENCRYPTION_KEY =
            "encryptionKeyForDrivingPermitToDecrypt-2023-08-03"; // DCS JWE (Private Key
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

    public static final String DVA_ENDPOINT = "DVA/dvaEndpoint";

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
