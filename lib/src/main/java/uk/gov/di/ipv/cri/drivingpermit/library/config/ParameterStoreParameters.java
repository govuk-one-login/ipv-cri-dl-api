package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    public static final String DCS_HTTPCLIENT_TLS_CERT = "/dl-cri-api-v1/DCS/HttpClient/tlsCert";

    public static final String DCS_HTTPCLIENT_TLS_KEY = "/dl-cri-api-v1/DCS/HttpClient/tlsKey";

    public static final String DCS_HTTPCLIENT_TLS_INTER_CERT =
            "/dl-cri-api-v1/DCS/HttpClient/tlsIntermediateCertificate";

    public static final String DCS_HTTPCLIENT_TLS_ROOT_CERT =
            "/dl-cri-api-v1/DCS/HttpClient/tlsRootCertificate";

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "/dl-cri-api-v1/DCS/JWS/signingCertForDrivingPermitToVerify"; // JWS SHA-1 Certificate
    // Thumbprint (Header)

    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "/dl-cri-api-v1/DCS/JWS/signingKeyForDrivingPermitToSign"; // JWS RSA Signing Key

    public static final String DCS_ENCRYPTION_CERT =
            "/dl-cri-api-v1/DCS/JWE/encryptionCertForDrivingPermitToEncrypt"; // JWE (Public Cert)

    public static final String DCS_SIGNING_CERT =
            "/dl-cri-api-v1/DCS/JWE/signingCertForDcsToVerify"; // DCS JWE (Reply Signature)

    public static final String DCS_DRIVING_PERMIT_ENCRYPTION_KEY =
            "/dl-cri-api-v1/DCS/JWE/encryptionKeyForDrivingPermitToDecrypt"; // DCS JWE (Private Key
    // Reply Decrypt)

    public static final String DVA_HTTPCLIENT_TLS_CERT = "/dl-cri-api-v1/DVA/HttpClient/tlsCert";

    public static final String DVA_HTTPCLIENT_TLS_KEY = "/dl-cri-api-v1/DVA/HttpClient/tlsKey";

    public static final String DVA_HTTPCLIENT_TLS_INTER_CERT =
            "/dl-cri-api-v1/DVA/HttpClient/tlsIntermediateCertificate";

    public static final String DVA_HTTPCLIENT_TLS_ROOT_CERT =
            "/dl-cri-api-v1/DVA/HttpClient/tlsRootCertificate";

    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "/dl-cri-api-v1/DVA/JWS/signingCertForDrivingPermitToVerify"; // JWS SHA-1 Certificate
    // Thumbprint (Header)

    public static final String DVA_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "/dl-cri-api-v1/DVA/JWS/signingKeyForDrivingPermitToSign"; // JWS RSA Signing Key

    public static final String DVA_ENCRYPTION_CERT =
            "/dl-cri-api-v1/DVA/JWE/encryptionCertForDrivingPermitToEncrypt"; // JWE (Public Cert)

    public static final String DVA_SIGNING_CERT =
            "/dl-cri-api-v1/DVA/JWE/signingCertForDvaToVerify"; // DVA JWE (Reply Signature)

    public static final String DVA_DRIVING_PERMIT_ENCRYPTION_KEY =
            "/dl-cri-api-v1/DVA/JWE/encryptionKeyForDrivingPermitToDecrypt"; // DVA JWE (Private Key
    // Reply Decrypt)

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
