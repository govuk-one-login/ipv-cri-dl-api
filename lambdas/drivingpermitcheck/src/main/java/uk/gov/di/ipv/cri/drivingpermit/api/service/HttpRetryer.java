package uk.gov.di.ipv.cri.drivingpermit.api.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.util.SleepHelper;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_MAX_RETRIES;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_RETRY;

public class HttpRetryer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final int MAX_HTTP_RETRIES = 7;
    public static final long HTTP_RETRY_WAIT_TIME_LIMIT_MS = 12800L;

    private final SleepHelper sleepHelper;
    private final CloseableHttpClient httpClient;

    private final EventProbe eventProbe;

    public HttpRetryer(CloseableHttpClient httpClient, EventProbe eventProbe) {
        this.sleepHelper = new SleepHelper(HTTP_RETRY_WAIT_TIME_LIMIT_MS);
        this.httpClient = httpClient;
        this.eventProbe = eventProbe;
    }

    public CloseableHttpResponse sendHTTPRequestRetryIfAllowed(HttpUriRequest request)
            throws IOException {

        CloseableHttpResponse httpResponse = null;

        // 0 is initial request, > 0 are retries
        int tryCount = 0;
        boolean retry = false;

        do {
            // "If" added for capturing retries
            if (retry) {
                eventProbe.counterMetric(THIRD_PARTY_REQUEST_SEND_RETRY);
            }

            // Wait before sending request (0ms for first try)
            sleepHelper.busyWaitWithExponentialBackOff(tryCount);

            try {
                httpResponse = httpClient.execute(request);

                retry = shouldHttpClientRetry(httpResponse.getStatusLine().getStatusCode());

                LOGGER.info(
                        "HTTPRequestRetry - totalRequests {}, retries {}, retryNeeded {}, statusCode {}",
                        tryCount + 1,
                        tryCount,
                        retry,
                        httpResponse.getStatusLine().getStatusCode());

            } catch (IOException e) {
                if (!(e instanceof HttpConnectTimeoutException)) {
                    eventProbe.counterMetric(THIRD_PARTY_REQUEST_SEND_FAIL);
                    throw e;
                }

                // For retries (tryCount>0) we want to rethrow only the last
                // HttpConnectTimeoutException
                if (tryCount < MAX_HTTP_RETRIES) {

                    LOGGER.info(
                            "HTTPRequestRetry {} - totalRequests {}, retries {}, retrying {}",
                            e.getMessage(),
                            tryCount + 1,
                            tryCount,
                            true);

                    retry = true;
                } else {

                    LOGGER.info(
                            "HTTPRequestRetry {} - totalRequests {}, retries {}, retrying {}",
                            e.getMessage(),
                            tryCount + 1,
                            tryCount,
                            false);

                    throw e;
                }
            }
        } while (retry && (tryCount++ < MAX_HTTP_RETRIES));

        int lastStatusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info("HTTPRequestRetry Exited lastStatusCode {}", lastStatusCode);

        if (lastStatusCode == 200) {
            eventProbe.counterMetric(THIRD_PARTY_REQUEST_SEND_OK);
        } else if (tryCount < MAX_HTTP_RETRIES) {
            eventProbe.counterMetric(THIRD_PARTY_REQUEST_SEND_ERROR);
        } else {
            eventProbe.counterMetric(THIRD_PARTY_REQUEST_SEND_MAX_RETRIES);
        }

        return httpResponse;
    }

    boolean shouldHttpClientRetry(int statusCode) {
        if (statusCode == 200) {
            // OK, Success
            return false;
        } else if (statusCode == 429) {
            // Too many recent requests
            LOGGER.warn("shouldHttpClientRetry statusCode - {}", statusCode);
            return true;
        } else {
            // Retry all server errors, but not any other status codes
            return ((statusCode >= 500) && (statusCode <= 599));
        }
    }
}
