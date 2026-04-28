package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;

import java.io.Serial;

@ExcludeFromGeneratedCoverageReport
public class OAuthErrorResponseException extends Exception {
    @Serial private static final long serialVersionUID = -9083587180249149383L;
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
