package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;

import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.Duration;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final ConfigurationService configurationService;
    private final PersonIdentityValidator personIdentityValidator;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AuditService auditService;
    private final HttpRetryer httpRetryer;

    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException {
        this.objectMapper = objectMapper;
        this.personIdentityValidator = new PersonIdentityValidator();
        this.configurationService = createConfigurationService();
        this.dcsCryptographyService = new DcsCryptographyService(configurationService);
        this.contraindicationMapper = new ContraIndicatorRemoteMapper(configurationService);
        this.httpClient = createHttpClient();
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
            HttpClient httpClient,
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

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }
}
