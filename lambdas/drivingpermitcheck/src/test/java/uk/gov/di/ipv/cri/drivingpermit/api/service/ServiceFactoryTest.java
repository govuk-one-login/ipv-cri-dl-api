package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class ServiceFactoryTest {
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService mockConfigurationService;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private FormDataValidator mockFormDataValidator;
    @Mock private CloseableHttpClient mockHttpClient;
    @Mock private DcsCryptographyService mockDcsCryptographyService;
    @Mock private AuditService mockAuditService;
    @Mock private EventProbe mockEventProbe;
    @SystemStub private EnvironmentVariables environmentVariables;
    ServiceFactory serviceFactory;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException, InvalidKeyException {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        // environmentVariables.set("AuditEventNamePrefix",
        // "/common-cri-parameters/AuditEventNamePrefix");

        serviceFactory =
                new ServiceFactory(
                        mockEventProbe,
                        mockConfigurationService,
                        mockDcsCryptographyService,
                        mockContraindicationMapper,
                        mockFormDataValidator,
                        mockHttpClient,
                        mockAuditService);
    }

    @Test
    void shouldCreateIdentityVerificationService()
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    HttpException, InvalidKeySpecException, KeyStoreException, IOException {

        serviceFactory =
                new ServiceFactory(
                        mockEventProbe,
                        mockConfigurationService,
                        mockDcsCryptographyService,
                        mockContraindicationMapper,
                        mockFormDataValidator,
                        mockHttpClient,
                        mockAuditService);

        IdentityVerificationService identityVerificationService =
                serviceFactory.getIdentityVerificationService();

        assertNotNull(identityVerificationService);
    }
}
