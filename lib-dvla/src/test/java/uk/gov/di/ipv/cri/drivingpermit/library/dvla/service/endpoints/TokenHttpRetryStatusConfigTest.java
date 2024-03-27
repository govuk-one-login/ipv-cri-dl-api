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
class TokenHttpRetryStatusConfigTest {

    private TokenHttpRetryStatusConfig tokenHttpRetryStatusConfig;

    @BeforeEach
    void setUp() {
        tokenHttpRetryStatusConfig = new TokenHttpRetryStatusConfig();
    }

    @Test
    void checkRetryStatusCodes() {

        List<Integer> neverRetryStatusCodes = List.of(200, 400, 401, 403);

        // Server Errors
        List<Integer> retryStatusCodes =
                new java.util.ArrayList<>(IntStream.rangeClosed(500, 599).boxed().toList());
        retryStatusCodes.add(429);

        List<Integer> allOtherStatusCodes =
                IntStream.rangeClosed(1, 599)
                        .filter(statusCode -> !neverRetryStatusCodes.contains(statusCode))
                        .filter(statusCode -> !retryStatusCodes.contains(statusCode))
                        .boxed()
                        .toList();

        // True for the retry codes
        retryStatusCodes.forEach(
                statusCode ->
                        assertTrue(tokenHttpRetryStatusConfig.shouldHttpClientRetry(statusCode)));

        // False for never retry
        neverRetryStatusCodes.forEach(
                statusCode ->
                        assertFalse(tokenHttpRetryStatusConfig.shouldHttpClientRetry(statusCode)));

        // False for all others
        allOtherStatusCodes.forEach(
                statusCode ->
                        assertFalse(tokenHttpRetryStatusConfig.shouldHttpClientRetry(statusCode)));
    }

    @Test
    void shouldReturnTrueForSuccessConditionStatusCodes() {

        // 200
        assertTrue(tokenHttpRetryStatusConfig.isSuccessStatusCode(200));

        IntStream.rangeClosed(1, 599)
                .filter(s -> s != 200)
                .forEach(
                        statusCode ->
                                assertFalse(
                                        tokenHttpRetryStatusConfig.isSuccessStatusCode(
                                                statusCode)));
    }

    @Test
    void checkTokenHttpRetryCustomMetricCallbacksWiredCorrectly() {

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_OK
                        .withEndpointPrefix(),
                tokenHttpRetryStatusConfig.httpRetryerSendOkMetric());

        Exception testE = new Exception("Test");
        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL
                        .withEndpointPrefixAndExceptionName(testE),
                tokenHttpRetryStatusConfig.httpRetryerSendFailMetric(testE));

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix(),
                tokenHttpRetryStatusConfig.httpRetryerSendErrorMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY
                        .withEndpointPrefix(),
                tokenHttpRetryStatusConfig.httpRetryerSendRetryMetric());

        assertEquals(
                ThirdPartyAPIEndpointMetric.DVLA_TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES
                        .withEndpointPrefix(),
                tokenHttpRetryStatusConfig.httpRetryerMaxRetriesMetric());
    }
}
