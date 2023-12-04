package uk.gov.di.ipv.cri.drivingpermit.api.service.dcs;

import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_HTTP_RETRYER_SEND_MAX_RETRIES;

public class DcsHttpRetryStatusConfig implements HttpRetryStatusConfig {
    @Override
    public boolean shouldHttpClientRetry(int statusCode) {
        // TODO values are from Fraud CRI align this with the DCS api
        if (statusCode == 200) {
            // OK, Success
            return false;
        } else if (statusCode == 429) {
            // Too many recent requests
            return true;
        } else {
            // Retry all server errors, but not any other status codes
            return ((statusCode >= 500) && (statusCode <= 599));
        }
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return 200 == statusCode;
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return DCS_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return DCS_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return DCS_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return DCS_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return DCS_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
