package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import javax.net.ssl.SSLContext;

import java.io.IOException;
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
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final EventProbe eventProbe;
    private final SessionService sessionService;

    private final PersonIdentityService personIdentityService;

    private final ConfigurationService configurationService;

    private final DataStore<DocumentCheckResultItem> dataStore;

    private final Region awsRegion = Region.of(System.getenv("AWS_REGION"));

    public ServiceFactory()
            throws NoSuchAlgorithmException, CertificateException, InvalidKeySpecException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();
        this.configurationService = createConfigurationService();
        this.auditService = createAuditService(this.objectMapper);
        this.sessionService = new SessionService();

        this.dataStore =
                new DataStore<>(
                        configurationService.getDocumentCheckResultTableName(),
                        DocumentCheckResultItem.class,
                        DataStore.getClient());

        this.personIdentityService = new PersonIdentityService();
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            EventProbe eventProbe,
            ConfigurationService configurationService,
            SessionService sessionService,
            AuditService auditService,
            DataStore<DocumentCheckResultItem> dataStore,
            PersonIdentityService personIdentityService) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = eventProbe;
        this.configurationService = configurationService;
        this.sessionService = sessionService;
        this.auditService = auditService;
        this.dataStore = dataStore;
        this.personIdentityService = personIdentityService;
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

    public EventProbe getEventProbe() {
        return eventProbe;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private AuditService createAuditService(ObjectMapper objectMapper) {
        var commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();
        return new AuditService(
                SqsClient.builder()
                        .region(awsRegion)
                        // TODO: investigate solution to bring this into SQSClientBuilder for best
                        // practice
                        // .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                        .build(),
                commonLibConfigurationService,
                objectMapper,
                new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));
    }

    public DataStore<DocumentCheckResultItem> getDataStore() {
        return dataStore;
    }

    public PersonIdentityService getPersonIdentityService() {
        return personIdentityService;
    }

    private static final char[] password = "password".toCharArray();

    public CloseableHttpClient generateDcsHttpClient(
            ConfigurationService configurationService, boolean tlsOn)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException,
                    HttpException {
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

        if (!tlsOn) {
            return contextSetup(keystoreTLS, null);
        }
        return contextSetup(keystoreTLS, trustStore);
    }

    public CloseableHttpClient generateDvaHttpClient(
            ConfigurationService configurationService, boolean tlsOn)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException,
                    HttpException {
        KeyStore keystoreTLS =
                createKeyStore(
                        configurationService.getDvaTlsSelfCert(),
                        configurationService.getDvaDrivingPermitTlsKey());

        KeyStore trustStore =
                createTrustStore(
                        new Certificate[] {
                            configurationService.getDvaTlsRootCert(),
                            configurationService.getDvaTlsIntermediateCert()
                        });

        if (!tlsOn) {
            return contextSetup(keystoreTLS, null);
        }
        return contextSetup(keystoreTLS, trustStore);
    }

    public CloseableHttpClient generateDvlaHttpClient(
            ConfigurationService configurationService, boolean tlsOn)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException,
                    HttpException {
        KeyStore keystoreTLS =
                createKeyStore(
                        null /*configurationService.getDvlaTlsSelfCert()*/,
                        null /*configurationService.getDvlaDrivingPermitTlsKey()*/);

        KeyStore trustStore =
                createTrustStore(
                        new Certificate[] {
                            null /*configurationService.getDvlaTlsRootCert()*/,
                            null /*configurationService.getDvlaTlsIntermediateCert()*/
                        });

        if (!tlsOn) {
            return contextSetup(keystoreTLS, null);
        }
        return contextSetup(keystoreTLS, trustStore);
    }

    private CloseableHttpClient contextSetup(KeyStore clientTls, KeyStore caBundle)
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

    private KeyStore createKeyStore(Certificate cert, Key key)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, password);

        keyStore.setKeyEntry("TlSKey", key, password, new Certificate[] {cert});
        keyStore.setCertificateEntry("my-ca-1", cert);
        return keyStore;
    }

    private KeyStore createTrustStore(Certificate[] certificates)
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
