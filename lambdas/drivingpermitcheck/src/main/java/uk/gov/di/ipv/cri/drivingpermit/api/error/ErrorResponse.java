package uk.gov.di.ipv.cri.drivingpermit.api.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA(1000, "Failed to parse Driving Permit form data"),
    MISSING_QUERY_PARAMETERS(1001, "Missing query parameters for auth request"),
    FAILED_TO_PARSE_OAUTH_QUERY_STRING_PARAMETERS(
            1002, "Failed to parse oauth2-specific query string parameters"),
    FAILED_TO_PREPARE_DCS_PAYLOAD(1003, "Failed to prepare DCS payload"),
    ERROR_CONTACTING_DCS(1004, "Error when contacting DCS for document check"),
    FAILED_TO_UNWRAP_DCS_RESPONSE(1005, "Failed to unwrap Dcs response"),
    DCS_RETURNED_AN_ERROR(1006, "DCS returned an error response"),
    MISSING_SHARED_ATTRIBUTES_JWT(1007, "Missing shared attributes JWT from request body"),
    FAILED_TO_PARSE(1008, "Failed to parse"),
    MISSING_CLIENT_ID_QUERY_PARAMETER(1009, "Missing client_id query parameter"),
    INVALID_REDIRECT_URL(1012, "Provided redirect URL is not in those configured for client"),
    UNKNOWN_CLIENT_ID(1013, "Unknown client id provided in request params"),
    INVALID_REQUEST_PARAM(1014, "Invalid request param"),
    FAILED_TO_SEND_AUDIT_MESSAGE_TO_SQS_QUEUE(
            1016, "Failed to send message to aws SQS audit event queue"),
    MISSING_USER_ID_HEADER(1017, "Missing user_id header in authorisation request"),
    MISSING_SESSION_ID_HEADER(1018, "Missing session_id header"),
    FAILED_TO_REVOKE_ACCESS_TOKEN(1019, "Failed to revoke access token"),
    SESSION_NOT_FOUND(1020, "Session not found"),

    FORM_DATA_FAILED_VALIDATION(1021, "Form Data failed validation"),
    DCS_ERROR_HTTP_30X(1022, "DCS Responded with a HTTP Redirection status code"),
    DCS_ERROR_HTTP_40X(1023, "DCS Responded with a HTTP Client Error status code"),
    DCS_ERROR_HTTP_50X(1024, "DCS Responded with a HTTP Server Error status code"),
    DCS_ERROR_HTTP_X(1025, "DCS Responded with an unhandled HTTP status code"),
    TOO_MANY_RETRY_ATTEMPTS(1026, "Too many retry attempts made"),

    /**************************************DVA Specific Errors************************************/
    FAILED_TO_PREPARE_DVA_PAYLOAD(1027, "Failed to prepare DCS payload"),
    ERROR_CONTACTING_DVA(1028, "Error when contacting DCS for document check"),
    FAILED_TO_UNWRAP_DVA_RESPONSE(1029, "Failed to unwrap Dcs response"),
    DVA_RETURNED_AN_ERROR(1030, "DVA returned an error response"),
    DVA_ERROR_HTTP_30X(1031, "DVA Responded with a HTTP Redirection status code"),
    DVA_ERROR_HTTP_40X(1032, "DVA Responded with a HTTP Client Error status code"),

    DVA_ERROR_HTTP_400(1033, "DVA Responded with an Invalid Request Error status code"),
    DVA_ERROR_HTTP_401(1034, "DVA Responded with an Unauthorized Error status code"),
    DVA_ERROR_HTTP_50X(1035, "DVA Responded with a HTTP Server Error status code"),
    DVA_ERROR_HTTP_X(1036, "DVA Responded with an unhandled HTTP status code"),
    DVA_RETURNED_AN_INCOMPLETE_RESPONSE(1037, "DVA returned a response with missing values"),
    DVA_D_HASH_VALIDATION_ERROR(1038, "DVA Direct response was unable to be hash validated"),
    INCORRECT_HASH_VALIDATION_ALGORITHM_ERROR(
            1039, "Request hash validator was given an invalid algorithm");

    private final int code;
    private final String message;

    ErrorResponse(
            @JsonProperty(required = true, value = "code") int code,
            @JsonProperty(required = true, value = "message") String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
