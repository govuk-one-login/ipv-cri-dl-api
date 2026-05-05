package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;

import java.io.Serial;

@ExcludeFromGeneratedCoverageReport
public class UnauthorisedException extends Exception {
    @Serial private static final long serialVersionUID = -1601002853267841501L;
    private final int statusCode;
    private final ErrorResponse errorResponse;

    public UnauthorisedException(int statusCode, ErrorResponse errorResponse) {
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
