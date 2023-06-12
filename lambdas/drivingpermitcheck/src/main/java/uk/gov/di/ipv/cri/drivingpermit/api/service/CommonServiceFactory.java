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
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.HttpRetryer;

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

public class CommonServiceFactory {
    private final ConfigurationService configurationService;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final AuditService auditService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;
    private final Region awsRegion = Region.of(System.getenv("AWS_REGION"));

    public CommonServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException, KeyStoreException, IOException, HttpException {
        this.objectMapper = objectMapper;
        this.eventProbe = new EventProbe();
        this.configurationService = createConfigurationService();
        this.httpClient = generateHttpClient(configurationService);
        this.auditService = createAuditService(this.objectMapper);
        this.httpRetryer = new HttpRetryer(httpClient, eventProbe);
    }

    /** Creates common services used across CRIs/lambdas */
    @ExcludeFromGeneratedCoverageReport
    CommonServiceFactory(CloseableHttpClient httpClient, HttpRetryer httpRetryer)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();
        this.configurationService = createConfigurationService();
        this.httpClient = httpClient;
        this.httpRetryer = httpRetryer;
        this.auditService = createAuditService(this.objectMapper);
    }

    private ConfigurationService createConfigurationService()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        return new ConfigurationService(
                ParamManager.getSecretsProvider(),
                ParamManager.getSsmProvider(),
                System.getenv("ENVIRONMENT"));
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

    private static final char[] password = "password".toCharArray();

    public static CloseableHttpClient generateHttpClient(ConfigurationService configurationService)
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public EventProbe getEventProbe() {
        return eventProbe;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public HttpRetryer getHttpRetryer() {
        return httpRetryer;
    }
}
