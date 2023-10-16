package uk.gov.di.ipv.cri.drivingpermit.library.metrics;

import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.MetricException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_VALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_SEND_MAX_RETRIES;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DCS_THIRD_PARTY_API_DCS_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVA_THIRD_PARTY_API_DVA_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVLA_THIRD_PARTY_API_MATCH_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT;

public enum ThirdPartyAPIEndpointMetric {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DCS End Point Metrics                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    DCS_REQUEST_CREATED(DCS_THIRD_PARTY_API_DCS_ENDPOINT, REQUEST_CREATED),
    DCS_REQUEST_SEND_OK(DCS_THIRD_PARTY_API_DCS_ENDPOINT, REQUEST_SEND_OK),
    DCS_REQUEST_SEND_ERROR(DCS_THIRD_PARTY_API_DCS_ENDPOINT, REQUEST_SEND_ERROR),

    DCS_RESPONSE_TYPE_VALID(DCS_THIRD_PARTY_API_DCS_ENDPOINT, API_RESPONSE_TYPE_VALID),
    DCS_RESPONSE_TYPE_INVALID(DCS_THIRD_PARTY_API_DCS_ENDPOINT, API_RESPONSE_TYPE_INVALID),

    DCS_RESPONSE_TYPE_ERROR(DCS_THIRD_PARTY_API_DCS_ENDPOINT, API_RESPONSE_TYPE_ERROR),

    DCS_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    DCS_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    DCS_HTTP_RETRYER_REQUEST_SEND_OK(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_OK),
    DCS_HTTP_RETRYER_REQUEST_SEND_FAIL(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_FAIL),
    DCS_HTTP_RETRYER_REQUEST_SEND_RETRY(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_RETRY),
    DCS_HTTP_RETRYER_SEND_MAX_RETRIES(
            DCS_THIRD_PARTY_API_DCS_ENDPOINT, HTTP_RETRYER_SEND_MAX_RETRIES),
    DCS_HTTP_RETRYER_SEND_ERROR(DCS_THIRD_PARTY_API_DCS_ENDPOINT, HTTP_RETRYER_SEND_ERROR),

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DVA End Point Metrics                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    DVA_REQUEST_CREATED(DVA_THIRD_PARTY_API_DVA_ENDPOINT, REQUEST_CREATED),
    DVA_REQUEST_SEND_OK(DVA_THIRD_PARTY_API_DVA_ENDPOINT, REQUEST_SEND_OK),
    DVA_REQUEST_SEND_ERROR(DVA_THIRD_PARTY_API_DVA_ENDPOINT, REQUEST_SEND_ERROR),

    DVA_RESPONSE_TYPE_VALID(DVA_THIRD_PARTY_API_DVA_ENDPOINT, API_RESPONSE_TYPE_VALID),
    DVA_RESPONSE_TYPE_INVALID(DVA_THIRD_PARTY_API_DVA_ENDPOINT, API_RESPONSE_TYPE_INVALID),

    DVA_RESPONSE_TYPE_ERROR(DVA_THIRD_PARTY_API_DVA_ENDPOINT, API_RESPONSE_TYPE_ERROR),

    DVA_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    DVA_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    DVA_HTTP_RETRYER_REQUEST_SEND_OK(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_OK),
    DVA_HTTP_RETRYER_REQUEST_SEND_FAIL(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_FAIL),
    DVA_HTTP_RETRYER_REQUEST_SEND_RETRY(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_RETRY),
    DVA_HTTP_RETRYER_SEND_MAX_RETRIES(
            DVA_THIRD_PARTY_API_DVA_ENDPOINT, HTTP_RETRYER_SEND_MAX_RETRIES),
    DVA_HTTP_RETRYER_SEND_ERROR(DVA_THIRD_PARTY_API_DVA_ENDPOINT, HTTP_RETRYER_SEND_ERROR),

    DVA_INVALID_REQUEST_ERROR(DVA_THIRD_PARTY_API_DVA_ENDPOINT, "invalid_request_error"),
    DVA_REQUEST_ERROR(DVA_THIRD_PARTY_API_DVA_ENDPOINT, "request_error"),

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DLVA Token End Point Metrics                                                              //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    DVLA_TOKEN_REQUEST_REUSING_CACHED_TOKEN(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, "reusing_cached_token"), // Unique to DVLA Token

    DVLA_TOKEN_REQUEST_CREATED(DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, REQUEST_CREATED),
    DVLA_TOKEN_REQUEST_SEND_OK(DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, REQUEST_SEND_OK),

    DVLA_TOKEN_REQUEST_SEND_ERROR(DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, REQUEST_SEND_ERROR),

    DVLA_TOKEN_RESPONSE_TYPE_VALID(DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, API_RESPONSE_TYPE_VALID),
    DVLA_TOKEN_RESPONSE_TYPE_INVALID(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, API_RESPONSE_TYPE_INVALID),

    DVLA_TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    DVLA_TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    DVLA_TOKEN_RESPONSE_STATUS_CODE_ALERT_METRIC(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT,
            "status_code_alert_metric"), // Unique to DVLA Token

    DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_OK(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_OK),
    DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_FAIL),
    DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_RETRY),
    DVLA_TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, HTTP_RETRYER_SEND_MAX_RETRIES),
    DVLA_TOKEN_HTTP_RETRYER_SEND_ERROR(
            DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT, HTTP_RETRYER_SEND_ERROR),

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DVLA Match End Point Metrics                                                              //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    DVLA_MATCH_REQUEST_CREATED(DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, REQUEST_CREATED),
    DVLA_MATCH_REQUEST_SEND_OK(DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, REQUEST_SEND_OK),

    DVLA_MATCH_REQUEST_SEND_ERROR(DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, REQUEST_SEND_ERROR),

    DVLA_MATCH_RESPONSE_TYPE_VALID(DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, API_RESPONSE_TYPE_VALID),
    DVLA_MATCH_RESPONSE_TYPE_INVALID(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, API_RESPONSE_TYPE_INVALID),

    DVLA_RESPONSE_TYPE_ERROR(DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, API_RESPONSE_TYPE_ERROR),

    DVLA_MATCH_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    DVLA_MATCH_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_OK(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_OK),
    DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_FAIL(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_FAIL),
    DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_RETRY(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, HTTP_RETRYER_REQUEST_SEND_RETRY),
    DVLA_MATCH_HTTP_RETRYER_SEND_MAX_RETRIES(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, HTTP_RETRYER_SEND_MAX_RETRIES),
    DVLA_MATCH_HTTP_RETRYER_SEND_ERROR(
            DVLA_THIRD_PARTY_API_MATCH_ENDPOINT, HTTP_RETRYER_SEND_ERROR);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // End Of Metric Descriptions                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static final String METRIC_FORMAT = "%s_%s";
    private static final String METRIC_CAUSE_FORMAT = METRIC_FORMAT;

    private final String metricWithEndpointPrefix;

    // To avoid copy and paste errors in the alternative large list of string mappings for each
    // endpoint metric combo
    ThirdPartyAPIEndpointMetric(
            ThirdPartyAPIMetricEndpointPrefix prefix, ThirdPartyAPIEndpointMetricType metricType) {
        String endPointPrefixLowerCase = prefix.toString().toLowerCase();
        String metricTypeLowercase = metricType.toString().toLowerCase();
        this.metricWithEndpointPrefix =
                String.format(METRIC_FORMAT, endPointPrefixLowerCase, metricTypeLowercase);
    }

    // To allow special case metrics that do not apply to all endpoints (eg UP/DOWN health)
    ThirdPartyAPIEndpointMetric(ThirdPartyAPIMetricEndpointPrefix prefix, String metric) {
        String endPointPrefixLowerCase = prefix.toString().toLowerCase();
        String metricLowercase = metric.toLowerCase();
        this.metricWithEndpointPrefix =
                String.format(METRIC_FORMAT, endPointPrefixLowerCase, metricLowercase);
    }

    public String withEndpointPrefix() {
        return metricWithEndpointPrefix;
    }

    /**
     * Created for attaching Exception to REQUEST_SEND_ERROR - format effectively - %s_%s_%s. NOTE:
     * invalid to provide OAuthErrorResponseException. OAuthErrorResponseException is a generated
     * exception, metrics should only capture caught executions.
     *
     * @return String in the format endpont_metric_exceptionname
     */
    public String withEndpointPrefixAndExceptionName(Exception e) {
        if (e instanceof OAuthErrorResponseException) {
            // OAuthErrorResponseException is a generated exception,
            // metrics should only capture caught executions
            throw new MetricException(
                    "OAuthErrorResponseException is not a valid exception for metrics");
        }

        return String.format(
                METRIC_CAUSE_FORMAT, metricWithEndpointPrefix, e.getClass().getSimpleName());
    }
}
