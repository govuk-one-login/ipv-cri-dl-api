package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    // **************************** CRI ****************************

    public static final String CONTRAINDICATION_MAPPINGS = "contraindicationMappings";

    public static final String DOCUMENT_CHECK_RESULT_TABLE_NAME = "DocumentCheckResultTableName";
    public static final String DOCUMENT_CHECK_RESULT_TTL_PARAMETER =
            "SessionTtl"; // Linked to Common SessionTTL

    public static final String MAXIMUM_ATTEMPT_COUNT = "maximumAttemptCount"; // Max Form Attempts

    public static final String IS_DCS_PERFORMANCE_STUB =
            "isDcsPerformanceStub"; // Always false unless using stubs

    public static final String IS_DVA_PERFORMANCE_STUB =
            "isDvaPerformanceStub"; // Always false unless using stubs

    public static final String IS_DVLA_PERFORMANCE_STUB =
            "isDvlaPerformanceStub"; // Always false unless using stubs

    public static final String DVA_DIRECT_ENABLED = "dvaDirectEnabled";
    public static final String DVLA_DIRECT_ENABLED = "dvlaDirectEnabled";

    public static final String LOG_DCS_RESPONSE = "logDcsResponse";
    public static final String LOG_DVA_RESPONSE = "logDvaResponse";

    // **************************** DCS ****************************

    public static final String DCS_ENDPOINT = "DCS/dcsEndpoint";

    public static final String DCS_HTTPCLIENT_TLS_CERT = "DCS/HttpClient/tlsCert-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_KEY = "DCS/HttpClient/tlsKey-2023-08-03";

    public static final String DCS_HTTPCLIENT_TLS_INTER_CERT =
            "DCS/HttpClient/tlsIntermediateCertificate";

    public static final String DCS_HTTPCLIENT_TLS_ROOT_CERT = "DCS/HttpClient/tlsRootCertificate";

    // JWS SHA-1 Certificate - Thumbprint (Header)
    public static final String DCS_SIGNING_CERT_THUMB =
            "DCS/JWS/signingCertForDcsToVerify-2023-08-03";

    // JWS RSA Signing Key
    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_KEY =
            "DCS/JWS/signingKeyForDrivingPermitToSign-2023-08-03";

    // JWE (Public Key) - not a typo
    public static final String DCS_ENCRYPTION_CERT =
            "DCS/JWE/encryptionCertForDrivingPermitToEncrypt";

    // JWE (Reply Signature)
    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "DCS/JWE/signingCertForDrivingPermitToVerify";

    // JWE (Private Key Reply Decrypt)
    public static final String DCS_DRIVING_PERMIT_ENCRYPTION_KEY =
            "DCS/JWE/encryptionKeyForDrivingPermitToDecrypt-2023-08-03"; // DCS JWE (Private Key

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
