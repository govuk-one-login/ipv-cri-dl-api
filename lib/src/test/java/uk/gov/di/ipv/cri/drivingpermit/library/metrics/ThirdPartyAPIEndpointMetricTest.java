package uk.gov.di.ipv.cri.drivingpermit.library.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.MetricException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVA_THIRD_PARTY_API_DVA_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVLA_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVLA_THIRD_PARTY_API_KEY_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIMetricEndpointPrefix.DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class ThirdPartyAPIEndpointMetricTest {

    @Test
    void assertEndpointMetricsAreGeneratedCorrectly() {
        // The following test checks that the list of metrics generated by the
        // ThirdPartyAPIEndpointMetric
        // match the expected formats and values

        // Enums used to generate metrics
        List<ThirdPartyAPIMetricEndpointPrefix> endpointPrefixes =
                new ArrayList<>(Arrays.asList(ThirdPartyAPIMetricEndpointPrefix.values()));
        List<ThirdPartyAPIEndpointMetricType> baseMetrics =
                new ArrayList<>(Arrays.asList(ThirdPartyAPIEndpointMetricType.values()));

        // All possible combinations of metrics as enums
        List<ThirdPartyAPIEndpointMetric> enumGeneratedMetrics =
                new ArrayList<>(Arrays.asList(ThirdPartyAPIEndpointMetric.values()));

        // Create a list of the actual values used as metrics using the enums
        List<String> enumGeneratedMetricsStrings = new LinkedList<>();
        for (ThirdPartyAPIEndpointMetric enumGeneratedMetric : enumGeneratedMetrics) {
            String metricString = enumGeneratedMetric.withEndpointPrefix();
            enumGeneratedMetricsStrings.add(metricString);
        }

        // Generate all combinations of metrics from the enums in the expected format
        String expectedFormat = "%s_%s";
        List<String> expectedMetricsCaptureList = new LinkedList<>();
        int prefixSize = endpointPrefixes.size();
        int metricsSize = baseMetrics.size();
        for (int p = 0; p < prefixSize; p++) {
            for (int bm = 0; bm < metricsSize; bm++) {

                String endpointPrefix = endpointPrefixes.get(p).toString();
                String baseMetric = baseMetrics.get(bm).toString();

                String expectedCombinedMetric =
                        String.format(expectedFormat, endpointPrefix, baseMetric).toLowerCase();

                expectedMetricsCaptureList.add(expectedCombinedMetric);
            }
        }

        // Remove the test auto generate error types, that are not created in
        // ThirdPartyAPIEndpointMetric
        expectedMetricsCaptureList.remove(
                "dvla_third_party_api_token_endpoint_api_response_type_error"); // not used
        expectedMetricsCaptureList.remove(
                "dvla_third_party_api_change_password_endpoint_api_response_type_error"); // not
        // used
        expectedMetricsCaptureList.remove(
                "dvla_third_party_api_key_endpoint_api_response_type_error");

        // Add special case DVA metrics
        expectedMetricsCaptureList.add(
                String.format(
                                expectedFormat,
                                DVA_THIRD_PARTY_API_DVA_ENDPOINT,
                                "invalid_request_error")
                        .toLowerCase());

        expectedMetricsCaptureList.add(
                String.format(expectedFormat, DVA_THIRD_PARTY_API_DVA_ENDPOINT, "request_error")
                        .toLowerCase());

        // Add special case DVLA token reuse metric
        expectedMetricsCaptureList.add(
                String.format(
                                expectedFormat,
                                DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT,
                                "reusing_cached_token")
                        .toLowerCase());

        expectedMetricsCaptureList.add(
                String.format(
                                expectedFormat,
                                DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT,
                                "status_code_alert_metric")
                        .toLowerCase());

        expectedMetricsCaptureList.add(
                String.format(
                                expectedFormat,
                                DVLA_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT,
                                "password_fail_alert_metric")
                        .toLowerCase());

        expectedMetricsCaptureList.add(
                String.format(
                                expectedFormat,
                                DVLA_THIRD_PARTY_API_KEY_ENDPOINT,
                                "api_key_fail_alert_metric")
                        .toLowerCase());

        // Sort the two lists so the orders are the same
        Collections.sort(expectedMetricsCaptureList);
        Collections.sort(enumGeneratedMetricsStrings);

        int expectedSize = expectedMetricsCaptureList.size();
        int enumGenerated = enumGeneratedMetricsStrings.size();

        // Assert the two lists are same size
        assertEquals(expectedSize, enumGenerated);

        // Assert the two lists are identical (both sorted)
        for (int m = 0; m < expectedSize; m++) {

            String expected = expectedMetricsCaptureList.get(m);

            String valueFromEnum = enumGeneratedMetricsStrings.get(m);

            // For any one debugging changes/updates
            // System.out.println(m + " " + expected + " vs " + valueFromEnum);

            // Both lists are sorted,
            // If there are exact duplicates there is crossing wiring in ThirdPartyAPIEndpointMetric
            assertEquals(expected, valueFromEnum);
        }
    }

    @Test
    void
            assertMetricExceptionIsThrownIfWithEndpointPrefixAndExceptionNameIsSuppliedOAuthErrorResponseException() {

        OAuthErrorResponseException internalExceptionTypeThatShouldNotBeCapturedAsMetric =
                new OAuthErrorResponseException(
                        500, ErrorResponse.FAILED_TO_RETRIEVE_HTTP_RESPONSE_BODY);

        assertThrows(
                MetricException.class,
                () ->
                        ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR
                                .withEndpointPrefixAndExceptionName(
                                        internalExceptionTypeThatShouldNotBeCapturedAsMetric));
    }

    @Test
    void
            assertMetricExceptionIsNotThrownIfWithEndpointPrefixAndExceptionNameIsSuppliedAnAcceptableException() {

        Exception ioException = new IOException("Connection Timed Out");

        assertDoesNotThrow(
                () ->
                        ThirdPartyAPIEndpointMetric.DVLA_TOKEN_REQUEST_SEND_ERROR
                                .withEndpointPrefixAndExceptionName(ioException));
    }
}
