package uk.gov.di.ipv.cri.drivingpermit.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.time.Clock;

public class ServiceFactory {

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    private final ClientFactoryService clientFactoryService;
    private final ParameterStoreService parameterStoreService;

    private final AuditService auditService;
    private final SessionService sessionService;

    private final PersonIdentityService personIdentityService;

    private final DocumentCheckResultStorageService documentCheckResultStorageService;

    // Common-Lib
    private final ConfigurationService commonLibConfigurationService;

    public ServiceFactory() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();

        this.clientFactoryService = new ClientFactoryService();
        this.parameterStoreService = new ParameterStoreService(clientFactoryService);

        this.commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();

        this.auditService =
                new AuditService(
                        clientFactoryService.getSqsClient(),
                        commonLibConfigurationService,
                        objectMapper,
                        new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));

        this.sessionService = new SessionService();

        final String documentCheckTableName =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.STACK,
                        ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TABLE_NAME);

        this.documentCheckResultStorageService =
                new DocumentCheckResultStorageService(documentCheckTableName);

        this.personIdentityService = new PersonIdentityService();
    }

    // Service factory used to avoid passing all these parameters elsewhere
    // Suppressed S107 added, to avoid breaking apart the service factory (just yet)
    @ExcludeFromGeneratedCoverageReport
    @java.lang.SuppressWarnings("java:S107")
    ServiceFactory(
            EventProbe eventProbe,
            ClientFactoryService clientFactoryService,
            ParameterStoreService parameterStoreService,
            SessionService sessionService,
            AuditService auditService,
            DocumentCheckResultStorageService documentCheckResultStorageService,
            PersonIdentityService personIdentityService,
            ConfigurationService commonLibConfigurationService) {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = eventProbe;
        this.clientFactoryService = clientFactoryService;
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

    public ClientFactoryService getClientFactoryService() {
        return clientFactoryService;
    }

    public ParameterStoreService getParameterStoreService() {
        return parameterStoreService;
    }

    public ConfigurationService getCommonLibConfigurationService() {
        // Note SSM parameter gets via this service use a 5min cache time
        return commonLibConfigurationService;
    }
}
