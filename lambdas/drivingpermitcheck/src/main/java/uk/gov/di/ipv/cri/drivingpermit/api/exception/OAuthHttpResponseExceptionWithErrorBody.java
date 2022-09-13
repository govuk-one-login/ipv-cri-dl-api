package uk.gov.di.ipv.cri.drivingpermit.api.exception;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;

@ExcludeFromGeneratedCoverageReport
public class OAuthHttpResponseExceptionWithErrorBody extends HttpResponseExceptionWithErrorBody {
    public OAuthHttpResponseExceptionWithErrorBody(int statusCode, ErrorResponse errorResponse) {
        super(statusCode, errorResponse);
    }
}
