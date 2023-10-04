package uk.gov.di.ipv.cri.drivingpermit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KeyCertHelperTest {

    public static final String TEST_TLS_CRT =
            "MIIEaDCCAlACCQDMfWM01YUdWTANBgkqhkiG9w0BAQsFADB2MQswCQYDVQQGEwJVSzEPMA0GA1UEBwwGTE9ORE9OMQwwCgYDVQQKDANHRFMxFTATBgNVBAsMDFBhc3Nwb3J0IENSSTESMBAGA1UEAwwJbG9jYWxob3N0MR0wGwYJKoZIhvcNAQkBFg50ZXN0QGxvY2FsaG9zdDAeFw0yMzA1MzExNTQ1NTJaFw0yNDEwMTIxNTQ1NTJaMHYxCzAJBgNVBAYTAlVLMQ8wDQYDVQQHDAZMT05ET04xDDAKBgNVBAoMA0dEUzEVMBMGA1UECwwMUGFzc3BvcnQgQ1JJMRIwEAYDVQQDDAlsb2NhbGhvc3QxHTAbBgkqhkiG9w0BCQEWDnRlc3RAbG9jYWxob3N0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArKCIj/x7/9DxFE+hY0MdnthRDCnF0/6cktYWaf+5xO6Nb8jsqGn2AFBYkNsNxqdR8HaY2ni9aiS06kgqUlGRfQMxe7QF5wqDX85OLHAWu8Sq11YiPJxOtpwdR033c3xuVc9X1JaOX/oyN1rJWMAHWaDKh5jdR+d8nlTmu0dO+8cn+fFhnwPlWhjbMfTNRbmBdjT7xDD7BlkQ/Bfd+vyL1+u9nfxHKS3RwA4UzEDPVVgZK7JXkmqmaZKrlbE4zcDFST1LcgO5/60QqqNV796kbE+ST1g+QoDXmhqeI1jouO7a/LlTJiLbZY1pOik/g7ID747UP+MzIThVNvMnrpavrwIDAQABMA0GCSqGSIb3DQEBCwUAA4ICAQBtmSx1XJuASz/aE+qTXIeVdkPeGrsgdBwBaoOYBmE2xVnIWMTtpAn58+g5CdMZz2gRfshghzukcM4lbxI9PCV2PKfKmoxCv1TRFqAj3bWs46KqRngNqF5SE6Db+xsTxjqHuS2XIqm/1t9BsFMcMkcY2FNwNInMG19jUCLNDwgzzEjc7lYjLYfLp8izQtx+h9bxuUdDyI5i+yDYyprGAjW5obCrxyDt1z3o+zAdwxAAk1jIPu9aLMPXz21lodalJHE9A4kAZFd/5x0GX7fogNzZZBXEyGJatBwADBZ2GPDE3Kb/kc+TAC4/ImBZAbeFg0OkwAvLaO6k9YlENgS8ZsR8J/T7BnXyoXyx/0dq5dplnqmCsAUvtCA3Ds3LC15EYvpTDCj8qeJtPnzhLtoI3fPC6Cl+BvHRYgxezT7EQ0brlbMDOBglqqhiM13xM3wZvDHA4cj/lxwongPov7QpgAR/M3Qv1LpnGpu57q4BDMeuO2P6eWmBTA8aLbur2q3t3LJoccw9o3vIICvu3+rIHOnNWUhWIZL0FvqgAtqN9CnBrm+cnVJ0l8EFEAfg3c83iRkwntd4H/LT4WAkCW/vKxymcvundo3sHXFKSNjFuDuj36xY8LVjPDSg4dt12YX5oBt/l3Cr6LY0AqDZ3PTeZNojglTXv6ggemQoK9WfqNaa+g==";
    public static final String TEST_TLS_KEY =
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCsoIiP/Hv/0PEUT6FjQx2e2FEMKcXT/pyS1hZp/7nE7o1vyOyoafYAUFiQ2w3Gp1HwdpjaeL1qJLTqSCpSUZF9AzF7tAXnCoNfzk4scBa7xKrXViI8nE62nB1HTfdzfG5Vz1fUlo5f+jI3WslYwAdZoMqHmN1H53yeVOa7R077xyf58WGfA+VaGNsx9M1FuYF2NPvEMPsGWRD8F936/IvX672d/EcpLdHADhTMQM9VWBkrsleSaqZpkquVsTjNwMVJPUtyA7n/rRCqo1Xv3qRsT5JPWD5CgNeaGp4jWOi47tr8uVMmIttljWk6KT+DsgPvjtQ/4zMhOFU28yeulq+vAgMBAAECggEAEsaDE+YKNpCrJ+JhU0KBfj1Y01HXym7u6KmQVocR1XF8wKPB2a9sqiwS9e5dWTXpm5XKmjh3oYvMgY7Hy5xDjq1CPIof+jkSgOEXb43haxrA2hrsLltx1Dq2lILOXykxdvPOQbj+U0ILJCojrBt2sTDj1kuyBZgP3ciuL8BZvtiErcO8XNxU+8NR+/jbB/t/EPF2T+hj9CE1TaRPAZrVyNznhDAE48p0iji+JeM6FAPfm7RGaQiz24T/x+5QZB/4ot5rfhNBQIrZF9Cpae7mjeNOd28mZ+O2jQMpK4htXXUkEE3FKLN3s87Ga/x9ttmivy3gi0wLvF2H6pFBsruowQKBgQDTW+GJ+KZDav/+QmIsE/OZXVMcRQ5H1VHzVLwRtF966cH4ktguNpIczQ2TJn6kaD5fRwWLvTt35z+Lg/+gjJE6G7qz9trnUeh+595s6+AM11mmMDTwOUB7dLHq1evmOrzRc2+Z4LwVj2WHjUvn4tAfLLNd4z3SMjzqTIVa5JdcAwKBgQDRFm7ePJugfnUsQsr6zrONbsV0kLVhblQQxAhPFI3cN+g/Zs/f7AYAohN0xHO7L9TtVpnB349B5SGWjeIjzavH/LM1DpkwNTwI5uh+U9uFmKEB2/2+aW08zW2ma4lsA7VJaZ3/uATIcsxvOUIhNJZFjtSToEnCUcYcL+rq0GnL5QKBgQC0dOddGJpER5An81/EiIhc8ixqn58wv7aHjlpAt4Mp+3lslVvUIS2hP4YOlDhegM54JLhcdgXXOu6rXNkdp8a7GT/QNbzy8aES8T+cOcwCyr81QEWCsS8X+SuK+HjACnh6UBgCBrovok4qbP5ZDvNpGo5OZB29YL1Xwlu9nhyJJwKBgFELi//yqDRGsC5u2XTsSCbLSBhNuzC/7i/tfHcCtZYlyY1B0dOvDgiSyk3ZlC57dOJmTgUbHdFh+mn8JyrPnwVOyzwGo5RPHi0pK/dDk/St28L3vdxfyyrlSdpXFpZavZtuKAMmFhyIevulyx9J3fGqUnXCjLSYs9zbKgjIrimNAoGARWCqd/RepNTFFeQybqQbT8rNAZNVNguuSvBLax6toE/ncpcv7mpOzS9rSCCVXjGJyJx4BHvvMgidJcnnUGNd47J3Hk86pdZ1xmUnXeWyN9d7bl82v2yiGp42E6Ot3k8+/5BxVL9URr9dlEYb5EELKcXy+9UjuBbKVIkCAXK0JBw=";
    public static final String TEST_ROOT_CRT =
            "MIIFaDCCA1ACCQCUAfshwIH7VzANBgkqhkiG9w0BAQsFADB2MQswCQYDVQQGEwJVSzEPMA0GA1UEBwwGTE9ORE9OMQwwCgYDVQQKDANHRFMxFTATBgNVBAsMDFBhc3Nwb3J0IENSSTESMBAGA1UEAwwJbG9jYWxob3N0MR0wGwYJKoZIhvcNAQkBFg50ZXN0QGxvY2FsaG9zdDAeFw0yMzA1MzExNTQyMDRaFw0yNjAzMjAxNTQyMDRaMHYxCzAJBgNVBAYTAlVLMQ8wDQYDVQQHDAZMT05ET04xDDAKBgNVBAoMA0dEUzEVMBMGA1UECwwMUGFzc3BvcnQgQ1JJMRIwEAYDVQQDDAlsb2NhbGhvc3QxHTAbBgkqhkiG9w0BCQEWDnRlc3RAbG9jYWxob3N0MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAo5Z2V2nMidWk93F9XRGnOJWRLQXYuiZfDbSrcJb9q0/tpaApC+hDHNtt2aqWcvJVchPwJMzsg5gSFvBtI19VckdwSbBHFe3Kota9rGLJnm6JHBtOos0BVuPusVqAQqeKH2/q0oVLJ6DIgBtQygtelAxi37PgeXYLw63MPHto8RvFNEVYswvXdj0TwnoWd9GfSOpEAepJkY1Yzh9QSMSioxBV1Mzk8Nc/vnCyKVg5K5PiOnjIWL6IaQ0BDHEErgG418oXgzkL0woft+GEx1+jTeDCl5Sfg5eaAtCTFZjLxpaVdm8pTzVh+uBHYH2hlrwuh2T0gp5r63MUYMcmVwy8fYUUWAavFOF8Io9Qot55oRyOcmDaO1GR2oaWBJMxw6atdbz2AyuwHicKOlH2wFdqskDHfhYPZO2p4I3eYTSmXmznL5EMT1oz1WAPr5AsfjVWcCYo1i5mpPU8PWwXFRtBMgIk/s22JD5Gyqvh6Nd3H3TpC4lQCeBMVYo5003VNbU5gV088BZumOF1MzfiEAvhcapufTCWf90IzAYuh824JgqG0Z/GPP7Fb6FbUE/xACbL8NEKFru5Q4tK3Ur5UK15yewkq9AKufee2fEv43E+Ia+8rqY/pEMcY+tjJJl723npDztzV7Y6qg7Xl9+P4TIW7xth7o3X42LfGqwIDWYfea8CAwEAATANBgkqhkiG9w0BAQsFAAOCAgEAZeNEs9SqlVC4s7Kl/dH8UU05TptEkqti2eo0nFnb4qZ7p9n5em+Sc31L2094ck3PlpmaloXToDoRx4qkDBfrfBD1urLrP/lTrgJN1WPMbHJSu0W+MnbmPEsXOvZmJogRrXCU5bS7RBvHIsHvnC7ylTxzEl8p40s1jkiupuTV6HJ4JLi06SMIIuvuYHWsyR9G8UMBeT+KGTEPT0K0UiG+eRnSKw1muBE9LqRD3xpso+KB0Xr05sQMV1pCtY8zCQXVuCyXL836ZjV9lZ9kP7wxSVvshhVtCP5XLvVVX1+9IBNUSoIkcE5n8qy0XdgfTz6OzTijEnUGLqsxDgi3FAdN7QvSx8qZ3LcNl35y+y38HS08rce3NF6f6p9zINZNwG9PQQ5sPRTcGQ1L27fk1UdIsyf0iOuC+9fKhxh4WcnqF24QTQI+vyWn8/sp18cbjFQJNc9jwiVrLGZsf+ib3VJx7YyKv7YUOmuZ9zTSbMIjAf/0Qk9LCHsov8cnwr6rpyRCnG8Fx5gW9ni7Z9pTr1nyFOqCELnvULA6OsEXijziW2yrv4q4uhbe4XB/RinGd7X/wqAF1ZJbw+rdqv+46TalxhQ87oYMneiuYx38dFlknwdLC3jErKSYlhE9UOStYjvO36PPGT0S3NMnsB4Fepr11CPDa+mgUwau2YByFAtRCNA=";

    @Test
    void shouldReturnDecodedX509Certificate() {

        AtomicReference<Certificate> cert = new AtomicReference<>();
        assertDoesNotThrow(() -> cert.set(KeyCertHelper.getDecodedX509Certificate(TEST_ROOT_CRT)));
        assertNotNull(cert.get());
    }

    @Test
    void shouldReturnDecodedPrivateRSAKey() {
        AtomicReference<PrivateKey> key = new AtomicReference<>();
        assertDoesNotThrow(() -> key.set(KeyCertHelper.getDecodedPrivateRSAKey(TEST_TLS_KEY)));
        assertNotNull(key.get());
    }

    @Test
    void shouldCreateThumbprints() {
        AtomicReference<Thumbprints> key = new AtomicReference<>();
        assertDoesNotThrow(
                () ->
                        key.set(
                                KeyCertHelper.makeThumbprint(
                                        (X509Certificate)
                                                KeyCertHelper.getDecodedX509Certificate(
                                                        TEST_ROOT_CRT))));
        assertNotNull(key.get());
    }
}
