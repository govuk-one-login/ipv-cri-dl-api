package uk.gov.di.ipv.cri.drivingpermit.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.config.CertExpiryReminderConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DVACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.CertAndKeyTestFixtures;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertExpiryReminderConfigTest {

    @Mock private ParameterStoreService mockParameterStoreService;

    @Test
    void shouldReadAllValues() {

        mockReadDVAHTTPClientCerts();

        mockReadDVACryptoCerts();

        mockReadDVAHeldCerts();

        assertDoesNotThrow(() -> new CertExpiryReminderConfig(mockParameterStoreService));
    }

    private void mockReadDVAHTTPClientCerts() {
        Map<String, String> testDvaHtpClientCertsKeysMap =
                Map.of(
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_CERT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_ROOT_CERT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_INT_CERT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DVACloseableHttpClientFactory.HTTP_CLIENT_PARAMETER_PATH))
                .thenReturn(testDvaHtpClientCertsKeysMap);
    }

    private void mockReadDVACryptoCerts() {
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
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWS_PARAMETER_PATH))
                .thenReturn(testJWSParamMap);
        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWE_PARAMETER_PATH))
                .thenReturn(testJWEParamMap);
    }

    private void mockReadDVAHeldCerts() {
        Map<String, String> testDvaHeldCertMap =
                Map.of(
                        "tlsRootCert",
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        "tlsIntermediateCert",
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        "dvaSigningCert",
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        "dvaEncryptionCert",
                        CertAndKeyTestFixtures.TEST_TLS_CRT);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE, "DVA/heldByDVA"))
                .thenReturn(testDvaHeldCertMap);
    }
}
