package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.HttpClientException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.CertAndKeyTestFixtures;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DVACloseableHttpClientFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ParameterStoreService mockParameterStoreService;
    @Mock private ClientFactoryService mockClientFactoryService;

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldReturnClient(boolean tlsOn) {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        if (tlsOn) {
            mockReadDVAHTTPClientCerts();
        }

        DVACloseableHttpClientFactory dvaCloseableHttpClientFactory =
                new DVACloseableHttpClientFactory();

        // Note creates a full TLS http client using test certs + REAL ClientFactoryService
        assertDoesNotThrow(
                () ->
                        dvaCloseableHttpClientFactory.getClient(
                                mockParameterStoreService, new ClientFactoryService(), tlsOn));
    }

    @ParameterizedTest
    @CsvSource({
        "InvalidKeySpecException",
        "CertificateException",
        "KeyStoreException",
        "IOException",
        "UnrecoverableKeyException",
        "KeyManagementException"
    })
    void shouldThrowHttpClientExceptionOnCatchingException(String thrownExceptionName)
            throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
                    InvalidKeySpecException, KeyStoreException, IOException, KeyManagementException,
                    NoSuchMethodException, ClassNotFoundException, InvocationTargetException,
                    InstantiationException, IllegalAccessException {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        mockReadDVAHTTPClientCerts();

        DVACloseableHttpClientFactory dvaCloseableHttpClientFactory =
                new DVACloseableHttpClientFactory();

        Exception thrownException =
                switch (thrownExceptionName) {
                    case "InvalidKeySpecException" -> thrownException =
                            new InvalidKeySpecException(thrownExceptionName);
                    case "CertificateException" -> thrownException =
                            new CertificateException(thrownExceptionName);
                    case "KeyStoreException" -> thrownException =
                            new KeyStoreException(thrownExceptionName);
                    case "IOException" -> thrownException = new IOException(thrownExceptionName);
                    case "UnrecoverableKeyException" -> thrownException =
                            new UnrecoverableKeyException(thrownExceptionName);
                    case "KeyManagementException" -> thrownException =
                            new KeyManagementException(thrownExceptionName);
                    default -> throw new IllegalStateException(
                            "Unexpected value: " + thrownExceptionName);
                };

        when(mockClientFactoryService.generateHTTPClientFromExternalApacheHttpClient(
                        anyString(), anyString(), anyString(), anyString()))
                .thenThrow(thrownException);

        Exception thrown =
                assertThrows(
                        HttpClientException.class,
                        () ->
                                dvaCloseableHttpClientFactory.getClient(
                                        mockParameterStoreService, mockClientFactoryService, true));

        assertEquals(
                thrownException.getClass().getName() + ": " + thrownException.getMessage(),
                thrown.getMessage());
    }

    private void mockReadDVAHTTPClientCerts() {
        Map<String, String> testDvaHtpClientCertsKeysMap =
                Map.of(
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_CERT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT,
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_KEY,
                        CertAndKeyTestFixtures.TEST_TLS_KEY,
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_ROOT_CERT,
                        CertAndKeyTestFixtures.TEST_ROOT_CRT,
                        DVACloseableHttpClientFactory.MAP_KEY_TLS_INT_CERT,
                        CertAndKeyTestFixtures.TEST_TLS_CRT);

        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DVACloseableHttpClientFactory.HTTP_CLIENT_PARAMETER_PATH))
                .thenReturn(testDvaHtpClientCertsKeysMap);
    }
}
