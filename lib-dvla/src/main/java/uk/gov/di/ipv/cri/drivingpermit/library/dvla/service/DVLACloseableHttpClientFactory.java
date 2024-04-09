package uk.gov.di.ipv.cri.drivingpermit.library.dvla.service;

import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ClientFactoryService;

@ExcludeFromGeneratedCoverageReport
public class DVLACloseableHttpClientFactory {

    public DVLACloseableHttpClientFactory() {
        /* Intended */
    }

    public CloseableHttpClient getClient() {
        return new ClientFactoryService().generatePublicHttpClient();
    }
}
