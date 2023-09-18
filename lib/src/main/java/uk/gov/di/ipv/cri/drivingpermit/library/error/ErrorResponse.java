package uk.gov.di.ipv.cri.drivingpermit.library.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA(1000, "Failed to parse Driving Permit form data"),
    FORM_DATA_FAILED_VALIDATION(1001, "Form Data failed validation"),
    TOO_MANY_RETRY_ATTEMPTS(1002, "Too many retry attempts made"),

    // Common to DCS + DVLA
    FAILED_TO_RETRIEVE_HTTP_RESPONSE_BODY(1099, "Failed to retrieve http response body"),

    /**************************************DCS Specific Errors************************************/
    FAILED_TO_PREPARE_DCS_PAYLOAD(1103, "Failed to prepare DCS payload"),
    ERROR_CONTACTING_DCS(1104, "Error when contacting DCS for document check"),
    FAILED_TO_UNWRAP_DCS_RESPONSE(1105, "Failed to unwrap DCS response"),
    DCS_RETURNED_AN_ERROR_RESPONSE(1106, "DCS returned an error response"),
    ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE(
            1127, "DCS returned unexpected http status code"),

    /**************************************DVA Specific Errors************************************/
    FAILED_TO_PREPARE_DVA_PAYLOAD(1227, "Failed to prepare DVA payload"),
    ERROR_CONTACTING_DVA(1228, "Error when contacting DVA for document check"),
    FAILED_TO_UNWRAP_DVA_RESPONSE(1229, "Failed to unwrap DVA response"),
    DVA_RETURNED_AN_ERROR(1230, "DVA returned an error response"),
    DVA_ERROR_HTTP_30X(1231, "DVA Responded with a HTTP Redirection status code"),
    DVA_ERROR_HTTP_40X(1232, "DVA Responded with a HTTP Client Error status code"),

    DVA_ERROR_HTTP_400(1233, "DVA Responded with an Invalid Request Error status code"),
    DVA_ERROR_HTTP_401(1234, "DVA Responded with an Unauthorized Error status code"),
    DVA_ERROR_HTTP_50X(1235, "DVA Responded with a HTTP Server Error status code"),
    DVA_ERROR_HTTP_X(1236, "DVA Responded with an unhandled HTTP status code"),
    DVA_RETURNED_AN_INCOMPLETE_RESPONSE(1237, "DVA returned a response with missing values"),
    DVA_D_HASH_VALIDATION_ERROR(1238, "DVA Direct response was unable to be hash validated"),
    INCORRECT_HASH_VALIDATION_ALGORITHM_ERROR(
            1239, "Request hash validator was given an invalid algorithm"),

    /**************************************DVLA Specific Errors************************************/

    ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT(
            1304, "Error occurred when attempting to invoke the third party api token endpoint"),
    FAILED_TO_PREPARE_TOKEN_REQUEST_PAYLOAD(1305, "failed to prepare token request payload"),
    FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY(1306, "Failed to map token endpoint response body"),
    ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE(
            1307, "token endpoint returned unexpected http status code");

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
