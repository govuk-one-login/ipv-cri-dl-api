package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ThirdPartyAPIServiceFactory {

    private static final int DCS = 0;
    private static final int DVA = 1;
    private static final int DVLA = 2;

    private final ThirdPartyAPIService[] thirdPartyAPIServices = new ThirdPartyAPIService[3];

    public ThirdPartyAPIServiceFactory(ServiceFactory serviceFactory)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        // TODO We need three of these one for each api/stub
        boolean tlsOnDCS = !configurationService.isPerformanceStub();
        boolean tlsOnDva = !configurationService.isPerformanceStub();
        boolean tlsOnDvla = !configurationService.isPerformanceStub();

        thirdPartyAPIServices[DCS] = createDcsThirdPartyDocumentGateway(serviceFactory, tlsOnDCS);
        thirdPartyAPIServices[DVA] = createDvaThirdPartyDocumentGateway(serviceFactory, tlsOnDva);
        thirdPartyAPIServices[DVLA] =
                null; // createDvlaThirdPartyDocumentGateway(serviceFactory, tlsOnDvla);
    }

    private ThirdPartyAPIService createDcsThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        DcsCryptographyService dcsCryptographyService =
                new DcsCryptographyService(configurationService);

        CloseableHttpClient httpClient =
                serviceFactory.generateDcsHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe);

        return new DcsThirdPartyDocumentGateway(
                objectMapper,
                dcsCryptographyService,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(configurationService);

        RequestHashValidator requestHashValidator = new RequestHashValidator();

        CloseableHttpClient httpClient =
                serviceFactory.generateDvaHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe);

        return new DvaThirdPartyDocumentGateway(
                objectMapper,
                dvaCryptographyService,
                requestHashValidator,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvlaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        // Todo endpoint factor if multiple endpoints
        DvlaEndpointFactory dvlaEndpointFactory = new DvlaEndpointFactory(configurationService);

        CloseableHttpClient httpClient =
                serviceFactory.generateDvlaHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe);

        return new DvlaThirdPartyDocumentGateway(
                objectMapper, dvlaEndpointFactory, configurationService, httpRetryer, eventProbe);
    }

    public ThirdPartyAPIService getDcsThirdPartyAPIService() {
        return thirdPartyAPIServices[DCS];
    }

    public ThirdPartyAPIService getDvaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVA];
    }

    public ThirdPartyAPIService getDvlaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVLA];
    }
}
