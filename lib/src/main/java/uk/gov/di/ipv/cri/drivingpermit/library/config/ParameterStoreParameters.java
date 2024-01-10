package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    // **************************** CRI ****************************

    public static final String DOCUMENT_CHECK_RESULT_TABLE_NAME = "DocumentCheckResultTableName";
    public static final String DOCUMENT_CHECK_RESULT_TTL_PARAMETER =
            "SessionTtl"; // Linked to Common SessionTTL

    public static final String MAXIMUM_ATTEMPT_COUNT = "maximumAttemptCount"; // Max Form Attempts

    public static final String DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG =
            "devEnvironmentOnlyEnhancedDebug";

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
            "DCS/JWE/encryptionCertForDrivingPermitToEncrypt-2023-10-12";

    // JWE (Reply Signature)
    public static final String DCS_DRIVING_PERMIT_CRI_SIGNING_CERT =
            "DCS/JWE/signingCertForDrivingPermitToVerify-2023-10-12";

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
            "DVA/HttpClient/tlsIntermediateCertificate-2023-11-13";

    public static final String DVA_HTTPCLIENT_TLS_ROOT_CERT =
            "DVA/HttpClient/tlsRootCertificate-2023-11-13";

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
            "DVA/JWE/signingCertForDrivingPermitToVerify"; // should be JWS

    // JWE (Private Key Reply Decrypt)
    public static final String DVA_DRIVING_PERMIT_ENCRYPTION_KEY =
            "DVA/JWE/encryptionKeyForDrivingPermitToDecrypt";

    // ***************************DVA HELD***************************

    public static final String DRIVING_PERMIT_DVA_ROOT_CERT = "DVA/heldByDVA/tlsRootCert";

    public static final String DRIVING_PERMIT_DVA_INTERMEDIATE_CERT =
            "DVA/heldByDVA/tlsIntermediateCert";

    public static final String DRIVING_PERMIT_DVA_SIGNING_CERT = "DVA/heldByDVA/dvaSigningCert";

    public static final String DRIVING_PERMIT_DVA_ENCRYPTION_CERT =
            "DVA/heldByDVA/dvaEncryptionCert";

    // **************************** DVLA ****************************

    public static final String DVLA_ENDPOINT_URL = "DVLA/endpointUrl";
    public static final String DVLA_ENDPOINT_TOKEN = "DVLA/tokenPath";
    public static final String DVLA_ENDPOINT_MATCH = "DVLA/matchPath";
    public static final String DVLA_ENDPOINT_PASSWORD_PATH = "DVLA/passwordPath";
    public static final String DVLA_API_KEY = "DVLA/apiKey";
    public static final String DVLA_ENDPOINT_API_KEY_PATH = "DVLA/apiKeyPath";
    public static final String DVLA_USERNAME = "DVLA/username";
    public static final String DVLA_PASSWORD = "DVLA/password";

    public static final String DVLA_TOKEN_TABLE_NAME = "DVLA/TokenTableName";

    public static final String DVLA_PASSWORD_ROTATION_ENABLED = "DVLA/passwordRotationEnabled";

    public static final String DVLA_API_KEY_ROTATION_ENABLED = "DVLA/apiKeyRotationEnabled";

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
