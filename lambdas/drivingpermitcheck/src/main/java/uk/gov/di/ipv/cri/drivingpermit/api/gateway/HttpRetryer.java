package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.drivingpermit.api.util.SleepHelper;

import java.io.IOException;
import java.net.http.HttpConnectTimeoutException;

public class HttpRetryer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final int MAX_HTTP_RETRIES = 7;
    public static final long HTTP_RETRY_WAIT_TIME_LIMIT_MS = 12800L;

    private final SleepHelper sleepHelper;
    private final CloseableHttpClient httpClient;

    public HttpRetryer(CloseableHttpClient httpClient) {
        this.sleepHelper = new SleepHelper(HTTP_RETRY_WAIT_TIME_LIMIT_MS);
        this.httpClient = httpClient;
    }

    CloseableHttpResponse sendHTTPRequestRetryIfAllowed(HttpPost request)
            throws InterruptedException, IOException {

        CloseableHttpResponse httpResponse = null;

        // 0 is initial request, > 0 are retries
        int tryCount = 0;
        boolean retry = false;

        do {
            // Wait before sending request (0ms for first try)
            sleepHelper.sleepWithExponentialBackOff(tryCount);

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

        LOGGER.info(
                "HTTPRequestRetry Exited lastStatusCode {}",
                httpResponse.getStatusLine().getStatusCode());

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
