package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ThirdPartyAPIServiceFactory {

    private static final int MAX_HTTP_RETRIES = 2;

    private static final int DCS = 0;
    private static final int DVA = 1;
    private static final int DVLA = 2;

    private final ThirdPartyAPIService[] thirdPartyAPIServices = new ThirdPartyAPIService[3];

    public ThirdPartyAPIServiceFactory(DrivingPermitServiceFactory drivingPermitServiceFactory)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {
        ConfigurationService configurationService =
                drivingPermitServiceFactory.getConfigurationService();

        boolean tlsOnDCS = !configurationService.isDcsPerformanceStub();
        boolean tlsOnDva = !configurationService.isDvaPerformanceStub();
        boolean tlsOnDvla = !configurationService.isDvlaPerformanceStub();

        thirdPartyAPIServices[DCS] =
                createDcsThirdPartyDocumentGateway(drivingPermitServiceFactory, tlsOnDCS);
        thirdPartyAPIServices[DVA] =
                createDvaThirdPartyDocumentGateway(drivingPermitServiceFactory, tlsOnDva);
        thirdPartyAPIServices[DVLA] =
                createDvlaThirdPartyDocumentGateway(drivingPermitServiceFactory, tlsOnDvla);
    }

    private ThirdPartyAPIService createDcsThirdPartyDocumentGateway(
            DrivingPermitServiceFactory drivingPermitServiceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = drivingPermitServiceFactory.getObjectMapper();
        EventProbe eventProbe = drivingPermitServiceFactory.getEventProbe();
        ConfigurationService configurationService =
                drivingPermitServiceFactory.getConfigurationService();

        DcsCryptographyService dcsCryptographyService =
                new DcsCryptographyService(configurationService);

        CloseableHttpClient httpClient =
                drivingPermitServiceFactory.generateDcsHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_RETRIES);

        return new DcsThirdPartyDocumentGateway(
                objectMapper,
                dcsCryptographyService,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvaThirdPartyDocumentGateway(
            DrivingPermitServiceFactory drivingPermitServiceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = drivingPermitServiceFactory.getObjectMapper();
        EventProbe eventProbe = drivingPermitServiceFactory.getEventProbe();
        ConfigurationService configurationService =
                drivingPermitServiceFactory.getConfigurationService();

        DvaConfiguration dvaConfiguration = configurationService.getDvaConfiguration();

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(dvaConfiguration);

        RequestHashValidator requestHashValidator = new RequestHashValidator();

        CloseableHttpClient httpClient =
                drivingPermitServiceFactory.generateDvaHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_RETRIES);

        return new DvaThirdPartyDocumentGateway(
                objectMapper,
                dvaCryptographyService,
                requestHashValidator,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvlaThirdPartyDocumentGateway(
            DrivingPermitServiceFactory drivingPermitServiceFactory, boolean tlsOn) {

        ConfigurationService configurationService =
                drivingPermitServiceFactory.getConfigurationService();
        DvlaConfiguration dvlaConfiguration = configurationService.getDvlaConfiguration();
        ObjectMapper objectMapper = drivingPermitServiceFactory.getObjectMapper();
        EventProbe eventProbe = drivingPermitServiceFactory.getEventProbe();

        HttpRetryer httpRetryer =
                new HttpRetryer(
                        drivingPermitServiceFactory.generateDvlaHttpClient(),
                        eventProbe,
                        MAX_HTTP_RETRIES);

        DvlaEndpointFactory dvlaEndpointFactory =
                new DvlaEndpointFactory(dvlaConfiguration, objectMapper, eventProbe, httpRetryer);

        return new DvlaThirdPartyDocumentGateway(dvlaEndpointFactory);
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
