package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.HttpRetryer;

import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceFactoryTest {
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService mockConfigurationService;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private PersonIdentityValidator mockPersonIdentityValidator;
    @Mock private HttpClient mockHttpClient;
    @Mock private DcsCryptographyService mockDcsCryptographyService;
    @Mock private HttpRetryer mockHttpRetryer;

    @Mock private AuditService mockAuditService;

    @Test
    void shouldCreateIdentityVerificationService()
            throws NoSuchAlgorithmException, InvalidKeyException {
        ServiceFactory serviceFactory =
                new ServiceFactory(
                        mockObjectMapper,
                        mockConfigurationService,
                        mockDcsCryptographyService,
                        mockContraindicationMapper,
                        mockPersonIdentityValidator,
                        mockHttpClient,
                        mockAuditService,
                        mockHttpRetryer);

        IdentityVerificationService identityVerificationService =
                serviceFactory.getIdentityVerificationService();

        assertNotNull(identityVerificationService);
    }
}
