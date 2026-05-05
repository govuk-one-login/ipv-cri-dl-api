package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_SEND_MAX_RETRIES;

public class DvaHttpRetryStatusConfig implements HttpRetryStatusConfig {
    @Override
    public boolean shouldHttpClientRetry(int statusCode) {
        return switch (statusCode) {
            case 200 -> false;
            case 429 -> true;
            default -> ((statusCode >= 500) && (statusCode <= 599));
        };
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return 200 == statusCode;
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return DVA_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return DVA_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return DVA_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return DVA_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return DVA_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
