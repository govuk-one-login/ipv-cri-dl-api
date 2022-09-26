package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.HttpRetryer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ServiceFactoryTest {
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService mockConfigurationService;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private PersonIdentityValidator mockPersonIdentityValidator;
    @Mock private CloseableHttpClient mockHttpClient;
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
