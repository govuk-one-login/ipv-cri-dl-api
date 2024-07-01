package uk.gov.di.ipv.cri.drivingpermit.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.acm.AcmClient;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.time.Clock;

public class ServiceFactory {

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final ClientProviderFactory clientProviderFactory;
    private final ApacheHTTPClientFactoryService apacheHTTPClientFactoryService;
    private final ParameterStoreService parameterStoreService;

    private final AuditService auditService;
    private final SessionService sessionService;

    private final PersonIdentityService personIdentityService;

    private final DocumentCheckResultStorageService documentCheckResultStorageService;

    // Common-Lib
    private final ConfigurationService commonLibConfigurationService;

    private AcmClient acmClient;

    @ExcludeFromGeneratedCoverageReport
    public ServiceFactory() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();

        this.clientProviderFactory = new ClientProviderFactory();
        this.apacheHTTPClientFactoryService = new ApacheHTTPClientFactoryService();
        this.parameterStoreService =
                new ParameterStoreService(clientProviderFactory.getSSMProvider());

        this.commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService(
                        clientProviderFactory.getSSMProvider(),
                        clientProviderFactory.getSecretsProvider());

        this.auditService =
                new AuditService(
                        clientProviderFactory.getSqsClient(),
                        commonLibConfigurationService,
                        objectMapper,
                        new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));

        this.sessionService =
                new SessionService(
                        commonLibConfigurationService,
                        clientProviderFactory.getDynamoDbEnhancedClient());

        final String documentCheckTableName =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.STACK,
                        ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TABLE_NAME);

        this.documentCheckResultStorageService =
                new DocumentCheckResultStorageService(documentCheckTableName);

        this.personIdentityService =
                new PersonIdentityService(
                        commonLibConfigurationService,
                        clientProviderFactory.getDynamoDbEnhancedClient());

        Region awsRegion = Region.of(System.getenv("AWS_REGION"));
        // AWS SDK CRT Client (SYNC) - connection defaults are in SdkHttpConfigurationOption
        SdkHttpClient sdkHttpClient = AwsCrtHttpClient.builder().maxConcurrency(100).build();
        EnvironmentVariableCredentialsProvider environmentVariableCredentialsProvider =
                EnvironmentVariableCredentialsProvider.create();

        this.acmClient =
                AcmClient.builder()
                        .region(awsRegion)
                        .httpClient(sdkHttpClient)
                        .credentialsProvider(environmentVariableCredentialsProvider)
                        .build();
    }

    // Service factory used to avoid passing all these parameters elsewhere
    // Suppressed S107 added, to avoid breaking apart the service factory (just yet)
    @java.lang.SuppressWarnings("java:S107")
    ServiceFactory(
            EventProbe eventProbe,
            ClientProviderFactory clientProviderFactory,
            ApacheHTTPClientFactoryService apacheHTTPClientFactoryService,
            ParameterStoreService parameterStoreService,
            SessionService sessionService,
            AuditService auditService,
            DocumentCheckResultStorageService documentCheckResultStorageService,
            PersonIdentityService personIdentityService,
            ConfigurationService commonLibConfigurationService) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = eventProbe;
        this.clientProviderFactory = clientProviderFactory;
        this.apacheHTTPClientFactoryService = apacheHTTPClientFactoryService;
        this.parameterStoreService = parameterStoreService;
        this.sessionService = sessionService;
        this.auditService = auditService;
        this.documentCheckResultStorageService = documentCheckResultStorageService;
        this.personIdentityService = personIdentityService;
        this.commonLibConfigurationService = commonLibConfigurationService;
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

    public DocumentCheckResultStorageService getDocumentCheckResultStorageService() {
        return documentCheckResultStorageService;
    }

    public PersonIdentityService getPersonIdentityService() {
        return personIdentityService;
    }

    public ClientProviderFactory getClientProviderFactory() {
        return clientProviderFactory;
    }

    public ApacheHTTPClientFactoryService getApacheHTTPClientFactoryService() {
        return apacheHTTPClientFactoryService;
    }

    public ParameterStoreService getParameterStoreService() {
        return parameterStoreService;
    }

    public ConfigurationService getCommonLibConfigurationService() {
        // Note SSM parameter gets via this service use a 5min cache time
        return commonLibConfigurationService;
    }

    public AcmClient getAcmClient() {
        return acmClient;
    }
}
