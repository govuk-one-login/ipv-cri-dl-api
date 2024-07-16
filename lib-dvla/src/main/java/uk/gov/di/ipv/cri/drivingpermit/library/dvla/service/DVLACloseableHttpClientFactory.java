package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service;

import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;

@ExcludeFromGeneratedCoverageReport
public class DVLACloseableHttpClientFactory {

    private ApacheHTTPClientFactoryService apacheHTTPClientFactoryService;

    public DVLACloseableHttpClientFactory(
            ApacheHTTPClientFactoryService apacheHTTPClientFactoryService) {
        this.apacheHTTPClientFactoryService = apacheHTTPClientFactoryService;
    }

    public CloseableHttpClient getClient() {
        return apacheHTTPClientFactoryService.generatePublicHttpClient();
    }
}
