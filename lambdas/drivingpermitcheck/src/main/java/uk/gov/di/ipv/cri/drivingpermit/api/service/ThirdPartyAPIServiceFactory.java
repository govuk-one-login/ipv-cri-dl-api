package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
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
import java.util.Map;

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
        String primarySigningKeyId = dvaCryptographyServiceConfiguration.getKmsSigningKeyId();
        String secondarySigningKeyId =
                dvaCryptographyServiceConfiguration.getSecondaryKmsSigningKeyId();

        String primaryEncryptionKeyId = dvaCryptographyServiceConfiguration.getKmsEncryptionKeyId();
        String secondaryEncryptionKeyId =
                dvaCryptographyServiceConfiguration.getSecondaryKmsEncryptionKeyId();

        X509Certificate primaryDlSigningCertificate =
                (X509Certificate) dvaCryptographyServiceConfiguration.getSigningThumbprintCert();
        X509Certificate secondaryDlSigningCertificate =
                (X509Certificate)
                        dvaCryptographyServiceConfiguration.getSecondarySigningThumbprintCert();

        X509Certificate primaryDlDecryptionCertificate =
                (X509Certificate) dvaCryptographyServiceConfiguration.getDecryptionThumbprintCert();
        X509Certificate secondaryDlDecryptionCertificate =
                (X509Certificate)
                        dvaCryptographyServiceConfiguration.getSecondaryDecryptionThumbprintCert();
        boolean hasCA = Boolean.parseBoolean(dvaCryptographyServiceConfiguration.getHasCA());

        if (hasCA) {
            String dlSigningCertificateString = acmCertificateService.exportAcmSigningCertificate();
            dlSigningCertificateString =
                    dlSigningCertificateString
                            .replace("\n", "")
                            .replace(BEGIN_CERT, "")
                            .replace(END_CERT, "");
            primaryDlSigningCertificate =
                    KeyCertHelper.getDecodedX509Certificate(dlSigningCertificateString);

            String secondaryDlSigningCertificateString =
                    acmCertificateService.exportAcmSecondarySigningCertificate();
            secondaryDlSigningCertificateString =
                    secondaryDlSigningCertificateString
                            .replace("\n", "")
                            .replace(BEGIN_CERT, "")
                            .replace(END_CERT, "");
            secondaryDlSigningCertificate =
                    KeyCertHelper.getDecodedX509Certificate(secondaryDlSigningCertificateString);

            String dlDecryptionCertificateString =
                    acmCertificateService.exportAcmEncryptionCertificate();
            dlDecryptionCertificateString =
                    dlDecryptionCertificateString
                            .replace("\n", "")
                            .replace(BEGIN_CERT, "")
                            .replace(END_CERT, "");
            primaryDlDecryptionCertificate =
                    KeyCertHelper.getDecodedX509Certificate(dlDecryptionCertificateString);

            String secondaryDlDecryptionCertificateString =
                    acmCertificateService.exportAcmSecondaryEncryptionCertificate();
            secondaryDlDecryptionCertificateString =
                    secondaryDlDecryptionCertificateString
                            .replace("\n", "")
                            .replace(BEGIN_CERT, "")
                            .replace(END_CERT, "");
            secondaryDlDecryptionCertificate =
                    KeyCertHelper.getDecodedX509Certificate(secondaryDlDecryptionCertificateString);
        }

        KmsClient kmsClient = serviceFactory.getClientProviderFactory().getKMSClient();

        KmsSigner primaryKmsSigner =
                new KmsSigner(primarySigningKeyId, primaryDlSigningCertificate, kmsClient);
        KmsSigner secondaryKmsSigner =
                new KmsSigner(secondarySigningKeyId, secondaryDlSigningCertificate, kmsClient);

        JweKmsDecrypter primaryJweKmsDecrypter =
                new JweKmsDecrypter(primaryEncryptionKeyId, kmsClient);
        JweKmsDecrypter secondaryJweKmsDecrypter =
                new JweKmsDecrypter(secondaryEncryptionKeyId, kmsClient);

        String primarySigningSha256Thumbprint =
                KeyCertHelper.makeThumbprint(primaryDlSigningCertificate).getSha256Thumbprint();
        String secondarySigningSha256Thumbprint =
                KeyCertHelper.makeThumbprint(secondaryDlSigningCertificate).getSha256Thumbprint();

        String primaryEncryptionSha256Thumbprint =
                KeyCertHelper.makeThumbprint(primaryDlDecryptionCertificate).getSha256Thumbprint();
        String secondaryEncryptionSha256Thumbprint =
                KeyCertHelper.makeThumbprint(secondaryDlDecryptionCertificate)
                        .getSha256Thumbprint();

        Map<String, KmsSigner> kmsSigners =
                Map.of(
                        primarySigningSha256Thumbprint,
                        primaryKmsSigner,
                        secondarySigningSha256Thumbprint,
                        secondaryKmsSigner);
        Map<String, JweKmsDecrypter> kmsDecrypters =
                Map.of(
                        primaryEncryptionSha256Thumbprint,
                        primaryJweKmsDecrypter,
                        secondaryEncryptionSha256Thumbprint,
                        secondaryJweKmsDecrypter);
        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration, kmsSigners, kmsDecrypters);

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
        DynamoDbEnhancedClient dynamoDbEnhancedClient =
                serviceFactory.getClientProviderFactory().getDynamoDbEnhancedClient();
        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService =
                serviceFactory.getApacheHTTPClientFactoryService();

        DVLACloseableHttpClientFactory dvlaCloseableHttpClientFactory =
                new DVLACloseableHttpClientFactory(apacheHTTPClientFactoryService);

        HttpRetryer httpRetryer =
                new HttpRetryer(
                        dvlaCloseableHttpClientFactory.getClient(), eventProbe, MAX_HTTP_RETRIES);

        DvlaEndpointFactory dvlaEndpointFactory =
                new DvlaEndpointFactory(
                        dvlaConfiguration,
                        objectMapper,
                        eventProbe,
                        httpRetryer,
                        dynamoDbEnhancedClient);

        return new DvlaThirdPartyDocumentGateway(dvlaEndpointFactory, dvlaConfiguration);
    }

    public ThirdPartyAPIService getDvaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVA];
    }

    public ThirdPartyAPIService getDvlaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVLA];
    }
}
