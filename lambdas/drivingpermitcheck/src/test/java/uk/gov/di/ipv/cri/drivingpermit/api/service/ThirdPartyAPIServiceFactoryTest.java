package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.acm.model.ExportCertificateResponse;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.CertAndKeyTestFixtures;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThirdPartyAPIServiceFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock ServiceFactory mockServiceFactory;
    @Mock DrivingPermitConfigurationService mockDrivingPermitConfigurationService;

    @Mock ParameterStoreService mockParameterStoreService;
    @Mock ApacheHTTPClientFactoryService mockApacheHTTPClientFactoryService;

    @Mock ClientProviderFactory clientProviderFactory;

    @Mock DvaConfiguration mockDvaConfiguration;
    @Mock DvlaConfiguration mockDvlaConfiguration;

    @Mock ObjectMapper mockObjectMapper;
    @Mock EventProbe mockEventProbe;
    @Mock AcmCertificateService acmCertificateService;

    private ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory;

    @BeforeEach
    void setUp() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");
        environmentVariables.set("SELF_SIGNED_ROOT_CERT", CertAndKeyTestFixtures.TEST_ROOT_CRT);

        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getApacheHTTPClientFactoryService())
                .thenReturn(mockApacheHTTPClientFactoryService);
        when(mockServiceFactory.getClientProviderFactory()).thenReturn(clientProviderFactory);

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
        when(acmCertificateService.exportAcmSigningCertificate())
                .thenReturn(
                        //  pragma: allowlist nextline secret
                        """
                                -----BEGIN CERTIFICATE-----
                                MIIDsDCCApigAwIBAgIRAN8vzgI+5JH/ENYddMs21eowDQYJKoZIhvcNAQELBQAw
                                WTELMAkGA1UEBhMCR0IxFzAVBgNVBAoMDkNhYmluZXQgT2ZmaWNlMQwwCgYDVQQL
                                DANHRFMxIzAhBgNVBAMMGkdEUyBETCBEVkEgVGVzdCBSb290IENBIEczMB4XDTI0
                                MDYxNDEwMTQwMVoXDTI1MDcxNDExMTQwMVowJjEkMCIGA1UEAwwbcmV2aWV3LWQu
                                ZGV2LmFjY291bnQuZ292LnVrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                                AQEAm3NZqdAvs9QbdH00cUTEuufMPHebK6QYogpxKc/SM9Nj3pexOHHZ9rCRH3VA
                                Kpv/qgS2++ELBXvfAUP3V5lh3iwY+QzWq9QJmkZqm5NDEo7bc5u+rEjmpftZDi1E
                                VGAcyUDgRl10nif2iyFy6LzU/M8YNm26D6cug6cxZyNWzK+mbeXTk+38zLScnsTu
                                9EKMZ8oeqF9xg0Y6zvEHx5/pyGAc/Dnm88DJ20Nr4NoycVtWib4tcjA4rO3yVoYL
                                4X4T94NF1FYJj5P9DAamxuRLLWPQC6gcc4kazElTVUVTGn8tS9HhoeQz6qN0+R8u
                                lqipvuf20J0coYBlT+HNRjO4HQIDAQABo4GlMIGiMCYGA1UdEQQfMB2CG3Jldmll
                                dy1kLmRldi5hY2NvdW50Lmdvdi51azAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFPKA
                                A24tSXdEi82gTV3E0C2lbvV4MB0GA1UdDgQWBBQv6gc6W96+rySLP3EpAxZZM//w
                                JzAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC
                                MA0GCSqGSIb3DQEBCwUAA4IBAQCoo/JBxEl5QKfu+rpSMAtHzwzScNLq6HP6Fy+Q
                                g+TbhWUIb5HANJf/VxDu20Oa3Hh0Ew3S/+28+4ZbEiE+38JXlnPmO93pMFmmMmyo
                                NHy0xHATmWlSaIDEtgagG0kaz+11negNJzXwwApYkgun5ig7Y8r16iZrmwE92LvW
                                70G9ln1cqF7ODt/8+WzdIs18PAZ/OxL7Pmo3hC5iNXn7go0h1tC0LpQzA0pdym54
                                li0b1qKxh+WoyIKCP4c42lr5bvtmBPZDPH11JUblfq126AmuVURXO4Cs3qHnqRej
                                bteNsvJYtttuyalLvQmepjYdGivN2y+pC7mYKCaFFFSPb2NE
                                -----END CERTIFICATE-----
                                """);

        // Self signed cert and key
        when(acmCertificateService.exportAcmTlsCertificates())
                .thenReturn(
                        ExportCertificateResponse.builder()
                                .certificate(
                                        //  pragma: allowlist nextline secret
                                        """
                                                -----BEGIN CERTIFICATE-----
                                                MIIDsDCCApigAwIBAgIRAN8vzgI+5JH/ENYddMs21eowDQYJKoZIhvcNAQELBQAw
                                                WTELMAkGA1UEBhMCR0IxFzAVBgNVBAoMDkNhYmluZXQgT2ZmaWNlMQwwCgYDVQQL
                                                DANHRFMxIzAhBgNVBAMMGkdEUyBETCBEVkEgVGVzdCBSb290IENBIEczMB4XDTI0
                                                MDYxNDEwMTQwMVoXDTI1MDcxNDExMTQwMVowJjEkMCIGA1UEAwwbcmV2aWV3LWQu
                                                ZGV2LmFjY291bnQuZ292LnVrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                                                AQEAm3NZqdAvs9QbdH00cUTEuufMPHebK6QYogpxKc/SM9Nj3pexOHHZ9rCRH3VA
                                                Kpv/qgS2++ELBXvfAUP3V5lh3iwY+QzWq9QJmkZqm5NDEo7bc5u+rEjmpftZDi1E
                                                VGAcyUDgRl10nif2iyFy6LzU/M8YNm26D6cug6cxZyNWzK+mbeXTk+38zLScnsTu
                                                9EKMZ8oeqF9xg0Y6zvEHx5/pyGAc/Dnm88DJ20Nr4NoycVtWib4tcjA4rO3yVoYL
                                                4X4T94NF1FYJj5P9DAamxuRLLWPQC6gcc4kazElTVUVTGn8tS9HhoeQz6qN0+R8u
                                                lqipvuf20J0coYBlT+HNRjO4HQIDAQABo4GlMIGiMCYGA1UdEQQfMB2CG3Jldmll
                                                dy1kLmRldi5hY2NvdW50Lmdvdi51azAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFPKA
                                                A24tSXdEi82gTV3E0C2lbvV4MB0GA1UdDgQWBBQv6gc6W96+rySLP3EpAxZZM//w
                                                JzAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC
                                                MA0GCSqGSIb3DQEBCwUAA4IBAQCoo/JBxEl5QKfu+rpSMAtHzwzScNLq6HP6Fy+Q
                                                g+TbhWUIb5HANJf/VxDu20Oa3Hh0Ew3S/+28+4ZbEiE+38JXlnPmO93pMFmmMmyo
                                                NHy0xHATmWlSaIDEtgagG0kaz+11negNJzXwwApYkgun5ig7Y8r16iZrmwE92LvW
                                                70G9ln1cqF7ODt/8+WzdIs18PAZ/OxL7Pmo3hC5iNXn7go0h1tC0LpQzA0pdym54
                                                li0b1qKxh+WoyIKCP4c42lr5bvtmBPZDPH11JUblfq126AmuVURXO4Cs3qHnqRej
                                                bteNsvJYtttuyalLvQmepjYdGivN2y+pC7mYKCaFFFSPb2NE
                                                -----END CERTIFICATE-----
                                                """)
                                .privateKey(
                                        //  pragma: allowlist nextline secret
                                        """
                                                -----BEGIN ENCRYPTED PRIVATE KEY-----
                                                MIIFKzBVBgkqhkiG9w0BBQ0wSDAnBgkqhkiG9w0BBQwwGgQUXYKIDhMlJtEG2lIe
                                                iVgL9D4wsuECAggAMB0GCWCGSAFlAwQBKgQQHEs/0ixG3g0xdW0fwVgW+QSCBNC7
                                                n492bcGsUKoOCdG9zV73GssgwnZCSaTAKbmGk2R8dpahSBashu8T2+ag9gI8WysF
                                                eRxLpee6egSCnjLpE4ejxhIsZju2Y8ut59uM7jHgfwYA0eMSlIV0j1ZJE6B0LzGt
                                                MF54dnAHz3TygNB6l2lgDKUn60kgNpQ42E7oh8/Bpex3XecqEUEpEGqyVd6LRysJ
                                                HXuRJ7aM3VKS4nN02aocPrKNaBSWXvckp1VMWOdxNtvw/TGaCfJrTWyQrxcXuB5X
                                                tF2iqB2DwLh9d1tGrFZmXAm8gJa1ir6EgNUI0oRNezbdvUSpv2i4vEOnsvzpBS8j
                                                GFBasYSlXUjJJGcy06cxMQhJoYLBc8XM3U5Ykr09osvpGIv9y35ayrBnm8iy2tJE
                                                cXidW5d7o1KzrSZ1Hyx1Z9eWdBViDlzpSRMtr5/D97hVWbm//IzJ4CT3sSRgEP9O
                                                h/8myGjPGzWqIeTgwonYIFIiBGTddTonoimsGZqmzwOd31ce1TcIphMMsbARZ3/Q
                                                olaf1lQHxC8bI7Yh8VH79z8dq+skJ3/iRpdcKBiGQ40jqoEKFX61QSUR6mkTJXF8
                                                icspaeoScaelPHznrb3Wc58umU8PP5HJJgnFsHUSzmtAY46ShjWz5OIwCBp71q5f
                                                rfhoxKKE6agNdnyWLdURn5xVc9SGeALtwQ5xJA/AhS2TXwW7uCX1ZzRBhDE2ITq1
                                                D3fVPSWNcpMdyiFDUkd0lppWHcMCZrN3c2D+PGg6juYPASGvtgR55kRVHrtzDj8P
                                                xVDpBpf5cROYUnLqdqJzyEcVEEqXlpEiEHU+7WvLCCvj7bsYsp0uQUno6sz3G61u
                                                N6+7s2UKUXo5kD61/SrUJPKeGGxyPKV3IdGiO46QwONRO2exXeFqb63PFpyLDD5J
                                                HgcBqIqpFgK25M/NV40nHxN3Bjr9TFMU7BxWuZnhcXFZDBRKDEKbbfk8+gaio0qy
                                                51+RRd7oEMAgzEymCQwUy5UDVuGlZOw0ANpdklSdBWS7GPGbozdg7U5cJrrC9Vc8
                                                Jy9OSCdS08Zzib9uIajmHV6Xypte172jOx0JIdUFheyJIzJ0X3ERsxj+r48QRq7e
                                                53FqvMQdp0ccPuGBhzqxwJEJ6MH29y/WXIQNLAUccgWoB/Ohjcp0UJjFuq+PHGmQ
                                                gRvbNIRmMcSP7N/CbXRJ9Vu2/FyfgYIFauWP/7C5cAtbQYBn6K06nV6pSI7cUias
                                                ORbguIw1uWARzoTqKM5dMCiRT4pyrwoF7nxExKdNhl3Rai0NU4ETzrQiLLcqN4RF
                                                RFzJp/1j3ltvNgqJobj36Z7uziE8VqBecbheLHERrKt7kevhxv2U4/WnUmuuKR08
                                                ty6lWNXhzISSZ5SEPx5P+/GSkZYxhuISyRVaJXNpDTjH8rmLIOL6KE3JZufqP1I+
                                                egXC+KUmXkfh0UoYHtceXB3c4QbR73/Oe0sBzOhZtdn2MoQYSeY9GSlkpvCLp59F
                                                p5XcRmjHG1FblNGQanNhEd4paJosSxlSErYvWs2M1U+t1nWiVR4h7myCEce0+BYH
                                                KZ1grL1qATioJ+w/Q43dKy6Epvn+gkrMg7Tur4GYAVWfDNCrHcbSbxugF+1X75Vj
                                                zzhAf3xwgXUIDq0qkTaaQRgDoth9TNHZPIqcN7t6Kg==
                                                -----END ENCRYPTED PRIVATE KEY-----
                                                """)
                                .build());

        thirdPartyAPIServiceFactory =
                new ThirdPartyAPIServiceFactory(
                        mockServiceFactory,
                        mockDrivingPermitConfigurationService,
                        acmCertificateService);
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
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWS_PARAMETER_PATH))
                .thenReturn(testJWSParamMap);
        when(mockParameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DvaCryptographyServiceConfiguration.DVA_JWE_PARAMETER_PATH))
                .thenReturn(testJWEParamMap);
    }
}
