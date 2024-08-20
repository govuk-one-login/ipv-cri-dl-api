package uk.gov.di.ipv.cri.drivingpermit.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    // **************************** CRI ****************************

    public static final String DOCUMENT_CHECK_RESULT_TABLE_NAME = "DocumentCheckResultTableName";
    public static final String DOCUMENT_CHECK_RESULT_TTL_PARAMETER =
            "SessionTtl"; // Linked to Common SessionTTL

    // **************************** DVA ****************************

    public static final String DVA_ENDPOINT = "DVA/dvaEndpoint";

    public static final String DVA_USERNAME = "DVA/Username";

    public static final String DVA_PASSWORD = "DVA/Password";

    // **************************** DVLA ****************************

    // DVLA Secrets Manager - Managed Rotation
    public static final String DVLA_PASSWORD_SECRET = "DVLA/password";

    public static final String DVLA_TOKEN_TABLE_NAME = "DVLA/TokenTableName";

    // ************************ Issue Cred VC ************************

    public static final String MAX_JWT_TTL_UNIT = "JwtTtlUnit"; // Issue Cred VC TTL

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
