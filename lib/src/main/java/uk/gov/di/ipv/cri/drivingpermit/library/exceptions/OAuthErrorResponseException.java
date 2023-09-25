package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;

@ExcludeFromGeneratedCoverageReport
public class OAuthErrorResponseException extends Exception {
    private final int statusCode;
    private final ErrorResponse errorResponse;

    public OAuthErrorResponseException(int statusCode, ErrorResponse errorResponse) {
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public String getErrorReason() {
        return this.errorResponse.getMessage();
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public ErrorResponse getErrorResponse() {
        return this.errorResponse;
    }
}
