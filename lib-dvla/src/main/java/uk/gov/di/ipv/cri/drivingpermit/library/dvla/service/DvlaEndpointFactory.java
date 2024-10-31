package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.http.client.config.RequestConfig;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

@ExcludeFromGeneratedCoverageReport
@Getter
public class DvlaEndpointFactory {

    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;

    public DvlaEndpointFactory(
            DvlaConfiguration dvlaConfiguration,
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            HttpRetryer httpRetryer,
            DynamoDbEnhancedClient dynamoDbEnhancedClient) {

        // Same on all endpoints
        RequestConfig defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();

        tokenRequestService =
                new TokenRequestService(
                        dvlaConfiguration,
                        dynamoDbEnhancedClient,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);

        driverMatchService =
                new DriverMatchService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
    }
}
