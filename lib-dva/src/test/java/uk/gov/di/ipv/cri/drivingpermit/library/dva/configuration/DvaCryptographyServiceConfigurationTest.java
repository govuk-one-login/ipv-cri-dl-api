package uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.CertAndKeyTestFixtures;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DvaCryptographyServiceConfigurationTest {

    @Mock private ParameterStoreService mockParameterStoreService;

    @Test
    void shouldReturnValuesForAllParameters() {

        mockReadDVACryptoCerts();

        DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration =
                assertDoesNotThrow(
                        () -> new DvaCryptographyServiceConfiguration(mockParameterStoreService));

        assertNotNull(dvaCryptographyServiceConfiguration);
        assertNotNull(dvaCryptographyServiceConfiguration.getEncryptionCert());
        assertNotNull(dvaCryptographyServiceConfiguration.getSigningCert());
        assertNotNull(dvaCryptographyServiceConfiguration.getEncryptionCertThumbprints());
        assertNotNull(dvaCryptographyServiceConfiguration.getSigningThumbprintCert());
    }

    private void mockReadDVACryptoCerts() {
        Map<String, String> testJWSParamMap =
                Map.of(
                        DvaCryptographyServiceConfiguration.MAP_KEY_SIGNING_CERT_FOR_DVA_TO_VERIFY,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SECONDARY_SIGNING_CERT_FOR_DVA_TO_VERIFY,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SIGNING_KEY_FOR_DRIVING_PERMIT_TO_SIGN,
                        CertAndKeyTestFixtures.TEST_TLS_KEY);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWS_PARAMETER_PATH))
                .thenReturn(testJWSParamMap);

        Map<String, String> testJWEParamMap =
                Map.of(
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_DECRYPTION_CERT_FOR_DVA_TO_ENCRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SECONDARY_DECRYPTION_CERT_FOR_DVA_TO_ENCRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DvaCryptographyServiceConfiguration
                                .MAP_KEY_ENCRYPTION_KEY_FOR_DRIVING_PERMIT_TO_DECRYPT,
                        CertAndKeyTestFixtures.TEST_TLS_KEY);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWE_PARAMETER_PATH))
                .thenReturn(testJWEParamMap);
    }
}
