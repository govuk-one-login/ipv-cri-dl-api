package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import software.amazon.awssdk.services.kms.KmsClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DVACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DVLACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

public class ThirdPartyAPIServiceFactory {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";

    private static final int MAX_HTTP_RETRIES = 2;

    private static final int DVA = 0;
    private static final int DVLA = 1;

    private final ThirdPartyAPIService[] thirdPartyAPIServices = new ThirdPartyAPIService[3];

    public ThirdPartyAPIServiceFactory(
            ServiceFactory serviceFactory,
            DrivingPermitConfigurationService drivingPermitConfigurationService,
            AcmCertificateService acmCertificateService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        boolean tlsOnDva = !drivingPermitConfigurationService.isDvaPerformanceStub();

        thirdPartyAPIServices[DVA] =
                createDvaThirdPartyDocumentGateway(
                        serviceFactory,
                        drivingPermitConfigurationService,
                        acmCertificateService,
                        tlsOnDva);
        thirdPartyAPIServices[DVLA] =
                createDvlaThirdPartyDocumentGateway(
                        serviceFactory, drivingPermitConfigurationService);
    }

    private ThirdPartyAPIService createDvaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory,
            DrivingPermitConfigurationService drivingPermitConfigurationService,
            AcmCertificateService acmCertificateService,
            boolean tlsOn)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        EventProbe eventProbe = serviceFactory.getEventProbe();
        ParameterStoreService parameterStoreService = serviceFactory.getParameterStoreService();
        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService =
                serviceFactory.getApacheHTTPClientFactoryService();

        DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration =
                new DvaCryptographyServiceConfiguration(parameterStoreService);
        String signingKeyId = dvaCryptographyServiceConfiguration.getKmsSigningKeyId();
        String encryptionKeyId = dvaCryptographyServiceConfiguration.getKmsEncryptionKeyId();

        X509Certificate dlSigningCertificate =
                (X509Certificate) dvaCryptographyServiceConfiguration.getSigningThumbprintCert();
        boolean hasCA = Boolean.parseBoolean(dvaCryptographyServiceConfiguration.getHasCA());

        if (hasCA) {
            String dlSigningCertificateString = acmCertificateService.exportAcmSigningCertificate();
            dlSigningCertificateString =
                    dlSigningCertificateString
                            .replace("\n", "")
                            .replace(BEGIN_CERT, "")
                            .replace(END_CERT, "");
            dlSigningCertificate =
                    KeyCertHelper.getDecodedX509Certificate(dlSigningCertificateString);
        }

        KmsClient kmsClient = serviceFactory.getClientProviderFactory().getKMSClient();
        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration,
                        new KmsSigner(signingKeyId, dlSigningCertificate, kmsClient),
                        new JweKmsDecrypter(encryptionKeyId, kmsClient));

        RequestHashValidator requestHashValidator = new RequestHashValidator();

        DVACloseableHttpClientFactory dvaCloseableHttpClientFactory =
                new DVACloseableHttpClientFactory();

        CloseableHttpClient httpClient =
                dvaCloseableHttpClientFactory.getClient(
                        dvaCryptographyServiceConfiguration,
                        parameterStoreService,
                        apacheHTTPClientFactoryService,
                        acmCertificateService,
                        tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_RETRIES);

        return new DvaThirdPartyDocumentGateway(
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
