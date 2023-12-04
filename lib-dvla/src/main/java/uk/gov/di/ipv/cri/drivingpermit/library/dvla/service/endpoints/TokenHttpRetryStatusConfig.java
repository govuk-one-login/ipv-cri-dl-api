package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints;

import uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;

import java.util.List;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.BAD_REQUEST;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.FORBIDDEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.UNAUTHORISED;

public class TokenHttpRetryStatusConfig implements HttpRetryStatusConfig {

    private final List<Integer> neverRetryCodes =
            List.of(SUCCESS, BAD_REQUEST, UNAUTHORISED, FORBIDDEN);

    private static final int TOO_MANY_REQUESTS = 429;
    private final List<Integer> retryCodes = List.of(TOO_MANY_REQUESTS);

    @Override
    public boolean shouldHttpClientRetry(int statusCode) {

        // Status codes where retrying is not valid
        if (neverRetryCodes.contains(statusCode)) {
            return false;
        }

        // Retry all server errors
        if (isServerErrorStatusCode(statusCode)) {
            return true;
        }

        // Retry status codes - all others are no retry
        return retryCodes.contains(statusCode);
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return SUCCESS == statusCode;
    }

    private boolean isServerErrorStatusCode(int statusCode) {
        return (statusCode >= 500) && (statusCode <= 599);
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_OK
                .withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL
                .withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY
                .withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES
                .withEndpointPrefix();
    }
}
