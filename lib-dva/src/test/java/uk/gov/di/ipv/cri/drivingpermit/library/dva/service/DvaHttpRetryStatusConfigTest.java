package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

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
class DvaHttpRetryStatusConfigTest {

    private DvaHttpRetryStatusConfig dvaHttpRetryStatusConfig;

    @BeforeEach
    void setUp() {
        dvaHttpRetryStatusConfig = new DvaHttpRetryStatusConfig();
    }

    @Test
    void checkRetryStatusCodes() {

        // Server Errors
        List<Integer> retryStatusCodes =
                new java.util.ArrayList<>(IntStream.rangeClosed(500, 599).boxed().toList());
        retryStatusCodes.add(429);

        List<Integer> allOtherStatusCodes =
                IntStream.rangeClosed(1, 600)
                        .filter(statusCode -> !retryStatusCodes.contains(statusCode))
                        .boxed()
                        .toList();

        // True for the retry codes
        retryStatusCodes.forEach(
                statusCode ->
                        assertTrue(dvaHttpRetryStatusConfig.shouldHttpClientRetry(statusCode)));

        // False for all others
        allOtherStatusCodes.forEach(
                statusCode ->
                        assertFalse(dvaHttpRetryStatusConfig.shouldHttpClientRetry(statusCode)));
    }

    @Test
    void shouldReturnTrueForSuccessConditionStatusCodes() {

        // 200
        assertTrue(dvaHttpRetryStatusConfig.isSuccessStatusCode(200));

        IntStream.rangeClosed(1, 599)
                .filter(s -> s != 200)
                .forEach(
                        statusCode ->
                                assertFalse(
                                        dvaHttpRetryStatusConfig.isSuccessStatusCode(statusCode)));
    }

    @Test
    void checkTokenHttpRetryCustomMetricCallbacksWiredCorrectly() {

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix(),
                dvaHttpRetryStatusConfig.httpRetryerSendOkMetric());

        Exception testE = new Exception("Test");
        assertEquals(
                ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_FAIL
                        .withEndpointPrefixAndExceptionName(testE),
                dvaHttpRetryStatusConfig.httpRetryerSendFailMetric(testE));

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix(),
                dvaHttpRetryStatusConfig.httpRetryerSendErrorMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_REQUEST_SEND_RETRY
                        .withEndpointPrefix(),
                dvaHttpRetryStatusConfig.httpRetryerSendRetryMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVA_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix(),
                dvaHttpRetryStatusConfig.httpRetryerMaxRetriesMetric());
    }
}
