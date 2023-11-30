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
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DcsConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
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
import java.util.UUID;

public class DrivingPermitServiceFactory {
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final EventProbe eventProbe;
    private final SessionService sessionService;

    private final PersonIdentityService personIdentityService;

    private final ConfigurationService configurationService;

    private final DataStore<DocumentCheckResultItem> dataStore;

    private final Region awsRegion = Region.of(System.getenv("AWS_REGION"));

    // Used internally at runtime when loading/retrieving keys into/from the SSL Keystore
    private static final char[] RANDOM_RUN_TIME_KEYSTORE_PASSWORD =
            UUID.randomUUID().toString().toCharArray();

    public DrivingPermitServiceFactory()
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
    DrivingPermitServiceFactory(
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

        ParameterStoreService parameterStoreService =
                new ParameterStoreService((ParamManager.getSsmProvider()));

        return new ConfigurationService(parameterStoreService);
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

    public CloseableHttpClient generateDcsHttpClient(
            ConfigurationService configurationService, boolean tlsOn)
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException,
                    HttpException {

        DcsConfiguration dcsConfiguration = configurationService.getDcsConfiguration();

        KeyStore keystoreTLS =
                createKeyStore(dcsConfiguration.getTlsSelfCert(), dcsConfiguration.getTlsKey());

        KeyStore trustStore =
                createTrustStore(
                        new Certificate[] {
                            dcsConfiguration.getTlsRootCert(),
                            dcsConfiguration.getTlsIntermediateCert()
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

        DvaConfiguration dvaConfiguration = configurationService.getDvaConfiguration();

        KeyStore keystoreTLS =
                createKeyStore(dvaConfiguration.getTlsSelfCert(), dvaConfiguration.getTlsKey());

        KeyStore trustStore =
                createTrustStore(
                        new Certificate[] {
                            dvaConfiguration.getTlsRootCert(),
                            dvaConfiguration.getTlsIntermediateCert()
                        });

        if (!tlsOn) {
            return contextSetup(keystoreTLS, null);
        }
        return contextSetup(keystoreTLS, trustStore);
    }

    public CloseableHttpClient generateDvlaHttpClient() {
        return HttpClients.custom().build();
    }

    private CloseableHttpClient contextSetup(KeyStore clientTls, KeyStore caBundle)
            throws HttpException {
        try {
            SSLContext sslContext =
                    SSLContexts.custom()
                            .loadKeyMaterial(clientTls, RANDOM_RUN_TIME_KEYSTORE_PASSWORD)
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
        keyStore.load(null, RANDOM_RUN_TIME_KEYSTORE_PASSWORD);

        keyStore.setKeyEntry(
                "TlSKey", key, RANDOM_RUN_TIME_KEYSTORE_PASSWORD, new Certificate[] {cert});
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
