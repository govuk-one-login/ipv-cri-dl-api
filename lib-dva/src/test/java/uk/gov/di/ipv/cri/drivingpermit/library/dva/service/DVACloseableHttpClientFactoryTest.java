package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.acm.model.ExportCertificateResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.HttpClientException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class DVACloseableHttpClientFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ParameterStoreService mockParameterStoreService;
    @Mock private ApacheHTTPClientFactoryService mockApacheHTTPClientFactoryService;

    @Mock private AcmCertificateService acmCertificateService;

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
                                mockParameterStoreService,
                                new ApacheHTTPClientFactoryService(),
                                acmCertificateService,
                                tlsOn));
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

        when(mockApacheHTTPClientFactoryService.generateHTTPClientFromExternalApacheHttpClient(
                        anyString(), anyString(), anyString(), anyString()))
                .thenThrow(thrownException);

        Exception thrown =
                assertThrows(
                        HttpClientException.class,
                        () ->
                                dvaCloseableHttpClientFactory.getClient(
                                        mockParameterStoreService,
                                        mockApacheHTTPClientFactoryService,
                                        acmCertificateService,
                                        true));

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

        // Self signed cert and key
        when(acmCertificateService.exportAcmTlsCertificates())
                .thenReturn(
                        ExportCertificateResponse.builder()
                                .certificate(
                                        "-----BEGIN CERTIFICATE-----\n"
                                                + "MIIDsDCCApigAwIBAgIRAN8vzgI+5JH/ENYddMs21eowDQYJKoZIhvcNAQELBQAw\n"
                                                + "WTELMAkGA1UEBhMCR0IxFzAVBgNVBAoMDkNhYmluZXQgT2ZmaWNlMQwwCgYDVQQL\n"
                                                + "DANHRFMxIzAhBgNVBAMMGkdEUyBETCBEVkEgVGVzdCBSb290IENBIEczMB4XDTI0\n"
                                                + "MDYxNDEwMTQwMVoXDTI1MDcxNDExMTQwMVowJjEkMCIGA1UEAwwbcmV2aWV3LWQu\n"
                                                + "ZGV2LmFjY291bnQuZ292LnVrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n"
                                                + "AQEAm3NZqdAvs9QbdH00cUTEuufMPHebK6QYogpxKc/SM9Nj3pexOHHZ9rCRH3VA\n"
                                                + "Kpv/qgS2++ELBXvfAUP3V5lh3iwY+QzWq9QJmkZqm5NDEo7bc5u+rEjmpftZDi1E\n"
                                                + "VGAcyUDgRl10nif2iyFy6LzU/M8YNm26D6cug6cxZyNWzK+mbeXTk+38zLScnsTu\n"
                                                + "9EKMZ8oeqF9xg0Y6zvEHx5/pyGAc/Dnm88DJ20Nr4NoycVtWib4tcjA4rO3yVoYL\n"
                                                + "4X4T94NF1FYJj5P9DAamxuRLLWPQC6gcc4kazElTVUVTGn8tS9HhoeQz6qN0+R8u\n"
                                                + "lqipvuf20J0coYBlT+HNRjO4HQIDAQABo4GlMIGiMCYGA1UdEQQfMB2CG3Jldmll\n"
                                                + "dy1kLmRldi5hY2NvdW50Lmdvdi51azAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFPKA\n"
                                                + "A24tSXdEi82gTV3E0C2lbvV4MB0GA1UdDgQWBBQv6gc6W96+rySLP3EpAxZZM//w\n"
                                                + "JzAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC\n"
                                                + "MA0GCSqGSIb3DQEBCwUAA4IBAQCoo/JBxEl5QKfu+rpSMAtHzwzScNLq6HP6Fy+Q\n"
                                                + "g+TbhWUIb5HANJf/VxDu20Oa3Hh0Ew3S/+28+4ZbEiE+38JXlnPmO93pMFmmMmyo\n"
                                                + "NHy0xHATmWlSaIDEtgagG0kaz+11negNJzXwwApYkgun5ig7Y8r16iZrmwE92LvW\n"
                                                + "70G9ln1cqF7ODt/8+WzdIs18PAZ/OxL7Pmo3hC5iNXn7go0h1tC0LpQzA0pdym54\n"
                                                + "li0b1qKxh+WoyIKCP4c42lr5bvtmBPZDPH11JUblfq126AmuVURXO4Cs3qHnqRej\n"
                                                + "bteNsvJYtttuyalLvQmepjYdGivN2y+pC7mYKCaFFFSPb2NE\n"
                                                + "-----END CERTIFICATE-----\n")
                                .privateKey(
                                        "-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
                                                + "MIIFKzBVBgkqhkiG9w0BBQ0wSDAnBgkqhkiG9w0BBQwwGgQUl5nMSjyTFrc7lo8C\n"
                                                + "AdSGvywCKPUCAggAMB0GCWCGSAFlAwQBKgQQ+450L4LECqylOl6Hd3WWfwSCBNCC\n"
                                                + "+ZGDxqe2ZntEPwrzsyzqRzTQWQpR3rYS69Gdu4rHElXiFutHHtY2uHmazXm6em5T\n"
                                                + "O6KSPd2d5NkdNnySUlV5lMWxmVhw8wGMWTY7V3/WeaZ+7Ece3ETI4ykodUYdW+c+\n"
                                                + "hIrBm/f3nM6iJ8p3DPmEmYw0ZQCJea1U2YasTyBkbWh9cOTqcod4B4c9Ls7G9uvP\n"
                                                + "gBTOCzowVrDcUjeqnxWIzGokIqkratFhsW4K01MJZiPuBLCZueMGvlwcyML5nvEa\n"
                                                + "b8oBXnj3MVDXhg+HVVA2Ts3BUCv9jkDJ2KaFytNssI5RXRXudeeG9J3Du4jRJ0v9\n"
                                                + "LVZE7UyDNB9AsG7VVCGlhlzyC4jULN1+251APoVQH7cfdl+nsBcq6V9/Ee32Myev\n"
                                                + "7HixRh8qaDkApVx6jxq9oJEuEZ2QHK2Q08If+24FqXixyRBFf/A2KMQliLex3fkQ\n"
                                                + "pJWLX9uDmoCRb7OL+pPOWlJPYca+YKaP9IkRmK+iWEVNXglAUaUe27QlkN+rKQ62\n"
                                                + "pouR76wDVxhidagQt0g/840Mrph1m0eG//6/5y+Zcz3rng2UBFSD3JJU3bgoxv5m\n"
                                                + "IdFjqo/45AMJkfQ1EuKxGMfl8HkE/WKJGg0OED/oBIVxUKcm+Nz3yrP+e5aM4hSA\n"
                                                + "MFKrA7v7+ixW9cwH/mKOFp8vMCrVT9XCfQlUr4RY2hxN1TvzfHVVcbQmo+XqQ8py\n"
                                                + "4dxJ1DgjoiR/tUWsN9ydqeJX92Y0w4qo0gE8GuBI09vAH4fKjik5A7Qw6Dw/fLJr\n"
                                                + "cYJNl3AvSRh1wpGkJ50YYQoHIjAhDOIsjJKOKcCr7XpaVf2kGIOcWXNfM3sSvhfS\n"
                                                + "yNOjHDU8Slj3/eVpfigPkTxR/kMAi5IWmvL/AlsIkyRakqDiEsiKkn7cfC/mP2MF\n"
                                                + "WebrdggIf5/TG82wRY9R9/CcMLhI9KMbzJm1NpqB19epHxcwqZINOAeMcnNPU3/U\n"
                                                + "UXtCA9Dfj4QWyG6eDwU9ie5SZ9QSHz2XwM4BKRd7e/YcO8XA/m108Eku0HK5l8Vz\n"
                                                + "tdbDAPLpQAh9csAcFGkf0oiPeYWHXyfiEUe+kaSzNxBUEctSPkDmod80kECyvJuC\n"
                                                + "jbF0LvpVFSAzqKgrryMUh/N7j1N9qSkH5KfjaWCT5Rnvoam86rJidcrCC4mxeQgW\n"
                                                + "XXjf1OHUL1qHkqHYe2H4oF62IV6yKirf+fcqcZjXScSW339yB038J4LrNbeUHD14\n"
                                                + "8z97UGIJQmMfxiqFBrvOGfw39ZDfmYOF5bk8F8Xv8kAFTT7UvAW03EXrigHBlKaq\n"
                                                + "uH7drpje+aV7OgQo+hw3+fsz4wFFTIatRg/3bhq3icsptBThzLi5iBcGNCIRT838\n"
                                                + "XAVuggOP3ywBRz74otarFhlIAPly/7RrZcSRMyYt0TPCUG8iNljbiw5y+FIQI7DH\n"
                                                + "GHiIbdC9/sWhSay8OBfrCH/40d7thwfadvp2Pz8WZWFcBBgFsck2b1FHDeAdCsun\n"
                                                + "ihPxC58kU6csCKf7zxLTLnbUQNDiL/+X/7eWes0JaEgVG7PXag3JPIzFNcFAZsKA\n"
                                                + "SLs3tfVQg09+hyrwMH2uS9MCV0opMIVBWNPhzHwK7T3nMJ9cJfcDZEBzqpnOSn2D\n"
                                                + "R/kfpFZicLJsMVrkVSB/Ih5CB4iFfCEHtkPjKc+a/g==\n"
                                                + "-----END ENCRYPTED PRIVATE KEY-----")
                                .build());
    }
}
