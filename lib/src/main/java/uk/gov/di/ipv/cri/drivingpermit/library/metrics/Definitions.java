package uk.gov.di.ipv.cri.drivingpermit.library.metrics;

public class Definitions {

    // Record all escape routes from the lambdas.
    // OK for expected routes with ERROR being all others
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK =
            "lambda_driving_permit_check_completed_ok";
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR =
            "lambda_driving_permit_check_completed_error";

    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK =
            "lambda_issue_credential_completed_ok";
    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR =
            "lambda_issue_credential_completed_error";

    // Document Status after an attempt
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX =
            "lambda_driving_permit_check_attempt_status_verified_"; // Attempt count appended
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY =
            "lambda_driving_permit_check_attempt_status_retry";
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED =
            "lambda_driving_permit_check_attempt_status_unverified";

    // FormDataValidator
    public static final String FORM_DATA_VALIDATION_PASS = "form_data_validation_pass";
    public static final String FORM_DATA_VALIDATION_FAIL = "form_data_validation_fail";

    // IssuingAuthority
    public static final String ISSUING_AUTHORITY_PREFIX = "issuing_authority_";

    // DCS
    public static final String DCS_CHECK_REQUEST_SUCCEEDED = "dcs_check_request_succeeded";
    public static final String DCS_CHECK_REQUEST_FAILED = "dcs_check_request_failed";

    // VC Contra Indicators (CI is Appended)
    public static final String DRIVING_PERMIT_CI_PREFIX = "driving_permit_ci_";

    // HTTP Connection Send (Both)
    public static final String THIRD_PARTY_REQUEST_CREATED = "third_party_requests_created";
    public static final String THIRD_PARTY_REQUEST_SEND_RETRY = "third_party_requests_send_retry";
    public static final String THIRD_PARTY_REQUEST_SEND_OK = "third_party_request_send_ok";
    public static final String THIRD_PARTY_REQUEST_SEND_ERROR = "third_party_request_send_error";
    public static final String THIRD_PARTY_REQUEST_SEND_MAX_RETRIES =
            "third_party_request_send_max_retries";
    public static final String THIRD_PARTY_REQUEST_SEND_FAIL =
            "third_party_requests_send_fail"; // IOException

    // Third Party Response Type DCS
    public static final String THIRD_PARTY_DCS_RESPONSE_OK = "third_party_dcs_response_ok";
    public static final String THIRD_PARTY_DCS_RESPONSE_TYPE_ERROR =
            "third_party_dcs_response_type_error";

    // Third Party Response Type DVA
    public static final String THIRD_PARTY_DVA_RESPONSE_OK = "third_party_dva_response_ok";
    public static final String THIRD_PARTY_DVA_RESPONSE_TYPE_ERROR =
            "third_party_dva_response_type_error";

    public static final String THIRD_PARTY_DVA_INVALID_REQUEST_ERROR =
            "third_party_dva_invalid_request_error";

    public static final String THIRD_PARTY_DVA_UNAUTHORIZED_ERROR =
            "third_party_dva_unauthorized_error";

    private Definitions() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
