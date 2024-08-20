package uk.gov.di.ipv.cri.drivingpermit.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class ServiceFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock EventProbe mockEventProbe;
    @Mock ClientProviderFactory mockClientProviderFactory;
    @Mock ApacheHTTPClientFactoryService mockApacheHTTPClientFactoryService;
    @Mock ParameterStoreService mockParameterStoreService;
    @Mock SessionService mockSessionService;
    @Mock AuditService mockAuditService;
    @Mock DocumentCheckResultStorageService mockDocumentCheckResultStorageService;
    @Mock PersonIdentityService mockPersonIdentityService;
    @Mock ConfigurationService mockCommonLibConfigurationService;

    private ServiceFactory serviceFactory;

    @BeforeEach
    void setUp() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");

        serviceFactory =
                new ServiceFactory(
                        mockEventProbe,
                        mockClientProviderFactory,
                        mockApacheHTTPClientFactoryService,
                        mockParameterStoreService,
                        mockSessionService,
                        mockAuditService,
                        mockDocumentCheckResultStorageService,
                        mockPersonIdentityService,
                        mockCommonLibConfigurationService);
    }

    @Test
    void shouldReturnObjectMapper() {
        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        assertNotNull(objectMapper);

        ObjectMapper objectMapper2 = serviceFactory.getObjectMapper();

        assertEquals(objectMapper, objectMapper2);
    }

    @Test
    void shouldReturnEventProbe() {
        EventProbe eventProbe = serviceFactory.getEventProbe();
        assertNotNull(eventProbe);

        EventProbe eventProbe2 = serviceFactory.getEventProbe();
        assertEquals(eventProbe, eventProbe2);
    }

    @Test
    void shouldReturnClientProviderFactory() {
        ClientProviderFactory clientProviderFactory1 = serviceFactory.getClientProviderFactory();
        assertNotNull(clientProviderFactory1);

        ClientProviderFactory clientProviderFactory2 = serviceFactory.getClientProviderFactory();
        assertEquals(clientProviderFactory1, clientProviderFactory2);
    }

    @Test
    void shouldReturnApacheHTTPClientFactoryService() {
        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService1 =
                serviceFactory.getApacheHTTPClientFactoryService();
        assertNotNull(apacheHTTPClientFactoryService1);

        ApacheHTTPClientFactoryService apacheHTTPClientFactoryService2 =
                serviceFactory.getApacheHTTPClientFactoryService();
        assertEquals(apacheHTTPClientFactoryService1, apacheHTTPClientFactoryService2);
    }

    @Test
    void shouldReturnParameterStoreService() {
        ParameterStoreService parameterStoreService1 = serviceFactory.getParameterStoreService();
        assertNotNull(parameterStoreService1);

        ParameterStoreService parameterStoreService2 = serviceFactory.getParameterStoreService();
        assertEquals(parameterStoreService1, parameterStoreService2);
    }

    @Test
    void shouldReturnCommonLibConfigurationService() {
        ConfigurationService commonLibConfigurationService1 =
                serviceFactory.getCommonLibConfigurationService();
        assertNotNull(commonLibConfigurationService1);

        ConfigurationService commonLibConfigurationService2 =
                serviceFactory.getCommonLibConfigurationService();
        assertEquals(commonLibConfigurationService1, commonLibConfigurationService2);
    }

    @Test
    void shouldReturnSessionService() {
        SessionService sessionService = serviceFactory.getSessionService();
        assertNotNull(sessionService);

        SessionService sessionService2 = serviceFactory.getSessionService();
        assertEquals(sessionService, sessionService2);

        assertEquals(sessionService, sessionService2);
    }

    @Test
    void shouldReturnAuditService() {

        AuditService auditService = serviceFactory.getAuditService();
        assertNotNull(auditService);

        AuditService auditService2 = serviceFactory.getAuditService();
        assertEquals(auditService, auditService2);
    }

    @Test
    void shouldReturnPersonIdentityService() {
        PersonIdentityService personIdentityService = serviceFactory.getPersonIdentityService();
        assertNotNull(personIdentityService);

        PersonIdentityService personIdentityService2 = serviceFactory.getPersonIdentityService();
        assertEquals(personIdentityService, personIdentityService2);
    }

    @Test
    void shouldReturnDocumentCheckResultStore() {

        DocumentCheckResultStorageService documentCheckResultStorageService1 =
                serviceFactory.getDocumentCheckResultStorageService();
        assertNotNull(documentCheckResultStorageService1);

        DocumentCheckResultStorageService documentCheckResultStorageService2 =
                serviceFactory.getDocumentCheckResultStorageService();
        assertEquals(documentCheckResultStorageService1, documentCheckResultStorageService2);
    }
}
