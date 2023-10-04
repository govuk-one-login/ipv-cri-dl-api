package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints;

import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryStatusConfig;

import java.util.List;

import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.BAD_GATEWAY;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.BAD_REQUEST;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.FORBIDDEN;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.GATEWAY_TIMEOUT;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.NOT_FOUND;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.SUCCESS;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.TOO_MANY_REQUESTS;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.UNAUTHORISED;
import static uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.ResponseStatusCodes.UNSPECIFIED_ERROR_500;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_SEND_MAX_RETRIES;

public class DriverMatchHttpRetryStatusConfig implements HttpRetryStatusConfig {

    // These values are from DVLA driver-get version 1.13.0
    private final List<Integer> neverRetryCodes =
            List.of(SUCCESS, BAD_REQUEST, UNAUTHORISED, FORBIDDEN, NOT_FOUND);

    private final List<Integer> retryCodes =
            List.of(TOO_MANY_REQUESTS, UNSPECIFIED_ERROR_500, BAD_GATEWAY, GATEWAY_TIMEOUT);

    private final List<Integer> successStatusCodes = List.of(SUCCESS, NOT_FOUND);

    @Override
    public boolean shouldHttpClientRetry(int statusCode) {

        if (neverRetryCodes.contains(statusCode)) {
            return false;
        }

        // Only status codes we will retry on
        return retryCodes.contains(statusCode);
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return successStatusCodes.contains(statusCode);
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return DVLA_MATCH_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return DVLA_MATCH_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
