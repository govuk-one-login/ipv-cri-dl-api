package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final ConfigurationService configurationService;
    private final PersonIdentityValidator personIdentityValidator;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final AuditService auditService;
    private final HttpRetryer httpRetryer;

    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException, KeyStoreException, IOException, HttpException {
        this.objectMapper = objectMapper;
        this.personIdentityValidator = new PersonIdentityValidator();
        this.configurationService = createConfigurationService();
        this.dcsCryptographyService = new DcsCryptographyService(configurationService);
        this.contraindicationMapper = new ContraIndicatorRemoteMapper(configurationService);
        this.httpClient = generateHttpClient(configurationService);
        this.auditService = createAuditService(this.objectMapper);
        this.httpRetryer = new HttpRetryer(httpClient);
        this.identityVerificationService = createIdentityVerificationService(this.auditService);
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            ObjectMapper objectMapper,
            ConfigurationService configurationService,
            DcsCryptographyService dcsCryptographyService,
            ContraindicationMapper contraindicationMapper,
            PersonIdentityValidator personIdentityValidator,
            CloseableHttpClient httpClient,
            AuditService auditService,
            HttpRetryer httpRetryer)
            throws NoSuchAlgorithmException, InvalidKeyException {
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
        this.dcsCryptographyService = dcsCryptographyService;
        this.contraindicationMapper = contraindicationMapper;
        this.personIdentityValidator = personIdentityValidator;
        this.httpClient = httpClient;
        this.auditService = auditService;
        this.httpRetryer = httpRetryer;
        this.identityVerificationService = createIdentityVerificationService(this.auditService);
    }

    private ConfigurationService createConfigurationService()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        return new ConfigurationService(
                ParamManager.getSecretsProvider(),
                ParamManager.getSsmProvider(),
                System.getenv("ENVIRONMENT"));
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public IdentityVerificationService getIdentityVerificationService() {
        return this.identityVerificationService;
    }

    private IdentityVerificationService createIdentityVerificationService(AuditService auditService)
            throws NoSuchAlgorithmException, InvalidKeyException {

        ThirdPartyDocumentGateway thirdPartyGateway =
                new ThirdPartyDocumentGateway(
                        this.objectMapper,
                        this.dcsCryptographyService,
                        this.configurationService,
                        this.httpRetryer);

        return new IdentityVerificationService(
                thirdPartyGateway,
                this.personIdentityValidator,
                this.contraindicationMapper,
                auditService,
                configurationService,
                objectMapper);
    }

    public AuditService getAuditService() {
        return auditService;
    }

    private AuditService createAuditService(ObjectMapper objectMapper) {
        var commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();
        return new AuditService(
                SqsClient.builder().build(),
                commonLibConfigurationService,
                objectMapper,
                new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));
    }

    private static final char[] password = "password".toCharArray();

    public static CloseableHttpClient generateHttpClient(ConfigurationService configurationService)
            throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException,
                    KeyStoreException, IOException, HttpException {
        KeyStore keystoreTLS =
                createKeyStore(
                        configurationService.getDrivingPermitTlsSelfCert(),
                        configurationService.getDrivingPermitTlsKey());

        KeyStore trustStore =
                createTrustStore(
                        new Certificate[] {
                            configurationService.getDcsTlsRootCert(),
                            configurationService.getDcsIntermediateCert()
                        });

        return contextSetup(keystoreTLS, trustStore);
    }

    private static CloseableHttpClient contextSetup(KeyStore clientTls, KeyStore caBundle)
            throws HttpException {
        try {
            SSLContext sslContext =
                    SSLContexts.custom()
                            .loadKeyMaterial(clientTls, password)
                            .loadTrustMaterial(caBundle, null)
                            .build();

            return HttpClients.custom().setSSLContext(sslContext).build();
        } catch (NoSuchAlgorithmException
                | KeyManagementException
                | KeyStoreException
                | UnrecoverableKeyException e) {
            throw new HttpException(e.getMessage());
        }
    }

    private static KeyStore createKeyStore(Certificate cert, Key key)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, password);

        keyStore.setKeyEntry("TlSKey", key, password, new Certificate[] {cert});
        keyStore.setCertificateEntry("my-ca-1", cert);
        return keyStore;
    }

    private static KeyStore createTrustStore(Certificate[] certificates)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        int k = 0;
        for (Certificate cert : certificates) {
            k++;
            keyStore.setCertificateEntry("my-ca-" + k, cert);
        }

        return keyStore;
    }
}
