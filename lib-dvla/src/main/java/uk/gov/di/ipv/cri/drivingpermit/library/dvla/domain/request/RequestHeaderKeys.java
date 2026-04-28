package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

@ExcludeClassFromGeneratedCoverageReport
public class RequestHeaderKeys {
    private RequestHeaderKeys() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_API_KEY = "X-API-Key";
}
