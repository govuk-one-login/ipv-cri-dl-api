package uk.gov.di.ipv.cri.drivingpermit.event.service;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;

import static uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.*;

@ExcludeFromGeneratedCoverageReport
public class ChangeApiKeyHttpRetryStatusConfig implements HttpRetryStatusConfig {

    @Override
    public boolean shouldHttpClientRetry(int statusCode) {
        return false;
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return SUCCESS == statusCode;
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return DVLA_API_KEY_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return DVLA_API_KEY_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return DVLA_API_KEY_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return DVLA_API_KEY_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return DVLA_API_KEY_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
