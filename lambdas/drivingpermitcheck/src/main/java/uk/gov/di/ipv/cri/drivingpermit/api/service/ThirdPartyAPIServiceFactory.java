package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DVACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DVLACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class ThirdPartyAPIServiceFactory {

    private static final int MAX_HTTP_RETRIES = 2;

    private static final int DVA = 0;
    private static final int DVLA = 1;

    private final ThirdPartyAPIService[] thirdPartyAPIServices = new ThirdPartyAPIService[3];

    public ThirdPartyAPIServiceFactory(
            ServiceFactory serviceFactory,
            DrivingPermitConfigurationService drivingPermitConfigurationService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        boolean tlsOnDva = !drivingPermitConfigurationService.isDvaPerformanceStub();

        thirdPartyAPIServices[DVA] =
                createDvaThirdPartyDocumentGateway(
                        serviceFactory, drivingPermitConfigurationService, tlsOnDva);
        thirdPartyAPIServices[DVLA] =
                createDvlaThirdPartyDocumentGateway(
                        serviceFactory, drivingPermitConfigurationService);
    }

    private ThirdPartyAPIService createDvaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory,
            DrivingPermitConfigurationService drivingPermitConfigurationService,
            boolean tlsOn)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ParameterStoreService parameterStoreService = serviceFactory.getParameterStoreService();
        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService =
                serviceFactory.getApacheHTTPClientFactoryService();

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        new DvaCryptographyServiceConfiguration(parameterStoreService));

        RequestHashValidator requestHashValidator = new RequestHashValidator();

        DVACloseableHttpClientFactory dvaCloseableHttpClientFactory =
                new DVACloseableHttpClientFactory();

        CloseableHttpClient httpClient =
                dvaCloseableHttpClientFactory.getClient(
                        parameterStoreService, apacheHTTPClientFactoryService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_RETRIES);

        return new DvaThirdPartyDocumentGateway(
                objectMapper,
                dvaCryptographyService,
                requestHashValidator,
                drivingPermitConfigurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvlaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory,
            DrivingPermitConfigurationService drivingPermitConfigurationService) {

        DvlaConfiguration dvlaConfiguration =
                drivingPermitConfigurationService.getDvlaConfiguration();
        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService =
                serviceFactory.getApacheHTTPClientFactoryService();

        DVLACloseableHttpClientFactory dvlaCloseableHttpClientFactory =
                new DVLACloseableHttpClientFactory(apacheHTTPClientFactoryService);

        HttpRetryer httpRetryer =
                new HttpRetryer(
                        dvlaCloseableHttpClientFactory.getClient(), eventProbe, MAX_HTTP_RETRIES);

        DvlaEndpointFactory dvlaEndpointFactory =
                new DvlaEndpointFactory(dvlaConfiguration, objectMapper, eventProbe, httpRetryer);

        return new DvlaThirdPartyDocumentGateway(dvlaEndpointFactory);
    }

    public ThirdPartyAPIService getDvaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVA];
    }

    public ThirdPartyAPIService getDvlaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVLA];
    }
}
