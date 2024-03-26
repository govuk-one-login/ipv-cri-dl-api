package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.util.CertAndKeyTestFixtures;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class ThirdPartyAPIServiceFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock ServiceFactory mockServiceFactory;
    @Mock DrivingPermitConfigurationService mockDrivingPermitConfigurationService;

    @Mock ParameterStoreService mockParameterStoreService;
    @Mock ClientFactoryService mockClientFactoryService;

    @Mock DvaConfiguration mockDvaConfiguration;
    @Mock DvlaConfiguration mockDvlaConfiguration;

    @Mock ObjectMapper mockObjectMapper;
    @Mock EventProbe mockEventProbe;
    @Mock CloseableHttpClient mockCloseableHttpClient;

    private ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory;

    @BeforeEach
    void setUp() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");

        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getClientFactoryService()).thenReturn(mockClientFactoryService);

        when(mockDrivingPermitConfigurationService.isDvaPerformanceStub()).thenReturn(false);

        // DVA
        when(mockServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);

        // DVLA
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);

        when(mockDrivingPermitConfigurationService.getDvlaConfiguration())
                .thenReturn(mockDvlaConfiguration);
        when(mockDvlaConfiguration.getTokenEndpoint()).thenReturn("TOKEN_END_POINT");
        when(mockDvlaConfiguration.getTokenTableName()).thenReturn("TOKEN_TABLE");
        when(mockDvlaConfiguration.getMatchEndpoint()).thenReturn("DRIVER_MATCH_ENDPOINT");

        mockDvaCryptographyServiceConfigurationParameterPathReads();

        thirdPartyAPIServiceFactory =
                new ThirdPartyAPIServiceFactory(
                        mockServiceFactory, mockDrivingPermitConfigurationService);
    }

    @Test
    void shouldReturnDvaThirdPartyService() {
        ThirdPartyAPIService thirdPartyAPIService =
                thirdPartyAPIServiceFactory.getDvaThirdPartyAPIService();
        assertNotNull(thirdPartyAPIService);
        assertInstanceOf(DvaThirdPartyDocumentGateway.class, thirdPartyAPIService);
    }

    @Test
    void shouldReturnDvlaThirdPartyService() {
        ThirdPartyAPIService thirdPartyAPIService =
                thirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService();
        assertNotNull(thirdPartyAPIService);
        assertInstanceOf(DvlaThirdPartyDocumentGateway.class, thirdPartyAPIService);
    }

    private void mockDvaCryptographyServiceConfigurationParameterPathReads() {

        // Mock Parameter store fetches in DvaCryptographyServiceConfiguration
        Map<String, String> testJWSParamMap =
                Map.of(
                        DvaCryptographyServiceConfiguration.MAP_KEY_SIGNING_CERT_FOR_DVA_TO_VERIFY,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SIGNING_KEY_FOR_DRIVING_PERMIT_TO_SIGN,
                        CertAndKeyTestFixtures.TEST_TLS_KEY);

        Map<String, String> testJWEParamMap =
                Map.of(
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_ENCRYPTION_KEY_FOR_DRIVING_PERMIT_TO_DECRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_KEY);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        DvaCryptographyServiceConfiguration.DVA_JWS_PARAMETER_PATH))
                .thenReturn(testJWSParamMap);
        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        DvaCryptographyServiceConfiguration.DVA_JWE_PARAMETER_PATH))
                .thenReturn(testJWEParamMap);
    }
}
