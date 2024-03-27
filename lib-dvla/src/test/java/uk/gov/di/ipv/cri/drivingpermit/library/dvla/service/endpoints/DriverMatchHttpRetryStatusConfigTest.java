package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DriverMatchHttpRetryStatusConfigTest {

    private DriverMatchHttpRetryStatusConfig driverMatchHttpRetryStatusConfig;

    @BeforeEach
    void setUp() {
        driverMatchHttpRetryStatusConfig = new DriverMatchHttpRetryStatusConfig();
    }

    @Test
    void checkRetryStatusCodes() {

        List<Integer> neverRetryStatusCodes = List.of(200, 400, 401, 403);

        List<Integer> retryStatusCodes = List.of(429, 500, 502, 504);

        List<Integer> allOtherStatusCodes =
                IntStream.rangeClosed(1, 599)
                        .filter(statusCode -> !neverRetryStatusCodes.contains(statusCode))
                        .filter(statusCode -> !retryStatusCodes.contains(statusCode))
                        .boxed()
                        .toList();

        // True for the retry codes
        retryStatusCodes.forEach(
                statusCode ->
                        assertTrue(
                                driverMatchHttpRetryStatusConfig.shouldHttpClientRetry(
                                        statusCode)));

        // False for never retry
        neverRetryStatusCodes.forEach(
                statusCode ->
                        assertFalse(
                                driverMatchHttpRetryStatusConfig.shouldHttpClientRetry(
                                        statusCode)));

        // False for all others
        allOtherStatusCodes.forEach(
                statusCode ->
                        assertFalse(
                                driverMatchHttpRetryStatusConfig.shouldHttpClientRetry(
                                        statusCode)));
    }

    @Test
    void shouldReturnTrueForSuccessConditionStatusCodes() {

        // 200
        assertTrue(driverMatchHttpRetryStatusConfig.isSuccessStatusCode(200));

        IntStream.rangeClosed(1, 599)
                .filter(s -> s != 200)
                .filter(s -> s != 404)
                .forEach(
                        statusCode ->
                                assertFalse(
                                        driverMatchHttpRetryStatusConfig.isSuccessStatusCode(
                                                statusCode)));
    }

    @Test
    void checkTokenHttpRetryCustomMetricCallbacksWiredCorrectly() {

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_OK
                        .withEndpointPrefix(),
                driverMatchHttpRetryStatusConfig.httpRetryerSendOkMetric());

        Exception testE = new Exception("Test");
        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_FAIL
                        .withEndpointPrefixAndExceptionName(testE),
                driverMatchHttpRetryStatusConfig.httpRetryerSendFailMetric(testE));

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix(),
                driverMatchHttpRetryStatusConfig.httpRetryerSendErrorMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_REQUEST_SEND_RETRY
                        .withEndpointPrefix(),
                driverMatchHttpRetryStatusConfig.httpRetryerSendRetryMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_MATCH_HTTP_RETRYER_SEND_MAX_RETRIES
                        .withEndpointPrefix(),
                driverMatchHttpRetryStatusConfig.httpRetryerMaxRetriesMetric());
    }
}
