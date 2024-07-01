package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import org.apache.http.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.SigningCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.KeyUtilities.BASE64_DCS_SIGNING_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.KeyUtilities.BASE64_ENCRYPTION_PUBLIC_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.KeyUtilities.SHA_1_THUMBPRINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.KeyUtilities.SHA_256_THUMBPRINT;

@ExtendWith(MockitoExtension.class)
class DvaCryptographyServiceTest {

    @Mock private DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration;
    @Mock private KmsSigner kmsSigner;
    @Mock private SigningCertificateFromKmsKey signingCertificateFromKmsKey;
    @Mock private JweKmsDecrypter jweKmsDecrypter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preparePayload()
            throws GeneralSecurityException, JOSEException, IOException, ParseException,
                    java.text.ParseException {

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration,
                        kmsSigner,
                        signingCertificateFromKmsKey,
                        jweKmsDecrypter);
        DvaPayload dvaPayload = createSuccessDvaPayload();

        when(kmsSigner.getKeyId()).thenReturn("123456789");
        when(kmsSigner.supportedJWSAlgorithms())
                .thenReturn(Collections.singleton(JWSAlgorithm.RS256));
        when(kmsSigner.sign(any(JWSHeader.class), any(byte[].class)))
                .thenReturn(new Base64URL("123456789"));
        when(dvaCryptographyServiceConfiguration.getEncryptionCertThumbprints())
                .thenReturn(
                        new Thumbprints(
                                SHA_1_THUMBPRINT + "-encryption",
                                SHA_256_THUMBPRINT + "-encryption"));
        when(dvaCryptographyServiceConfiguration.getEncryptionCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        when(signingCertificateFromKmsKey.certificateFromKmsKey("123456789"))
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        JWSObject jwsObject = dvaCryptographyService.preparePayload(dvaPayload);

        JWSHeader jwtHeader = jwsObject.getHeader();
        Payload payload = jwsObject.getPayload();

        String[] innerJwt = payload.toString().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String innerJwtHeaderString = new String(decoder.decode(innerJwt[0]));
        JWEHeader innerJwtHeader = JWEHeader.parse(innerJwtHeaderString);

        String innerPayload = new String(decoder.decode(innerJwt[1]));

        Assertions.assertEquals("5raL6y3NlxvFIqrCa64HiYAYUiU", jwtHeader.getCustomParam("x5t"));
        Assertions.assertEquals(
                "aUJzasL_WAwBGfSBl1zUHWb5HeGabx63Mk6Ho_-T-90",
                jwtHeader.getCustomParam("x5t#S256"));

        assertEquals(
                SHA_1_THUMBPRINT + "-encryption",
                innerJwtHeader.getX509CertThumbprint().toString());
        assertEquals(
                SHA_256_THUMBPRINT + "-encryption",
                innerJwtHeader.getX509CertSHA256Thumbprint().toString());

        assertNotNull(innerPayload);
    }

    // @Test
    void unwrapDvaResponse() throws Exception {

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration,
                        kmsSigner,
                        signingCertificateFromKmsKey,
                        jweKmsDecrypter);
        when(kmsSigner.getKeyId()).thenReturn("123456789");
        when(kmsSigner.supportedJWSAlgorithms())
                .thenReturn(Collections.singleton(JWSAlgorithm.RS256));
        when(signingCertificateFromKmsKey.certificateFromKmsKey("123456789"))
                .thenReturn(
                        KeyCertHelper.getDecodedX509Certificate(
                                "MIIDBzCCAe+gAwIBAgIGAZF1WmijMA0GCSqGSIb3DQEBCwUAMDoxODA2BgNVBAMML0RyaXZpbmcgTGljZW5jZSBDUkkgSlNPTiBTaWduaW5nIERldiAyNS0wNi0yMDI0MB4XDTI0MDgyMTE0MzIyNFoXDTI1MDgyMTE0MzIyNFowOjE4MDYGA1UEAwwvRHJpdmluZyBMaWNlbmNlIENSSSBKU09OIFNpZ25pbmcgRGV2IDI1LTA2LTIwMjQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCoUPIfrOrbuP3q4fSJgmNC4YbmukpNCjqK/yrTp+ykB+GpxvjcVWymdb7ywRlqC49Qfl18gHne261B82YKLDIRViz+q36ZlC1woTAIl/SLeXPdEFx26nLO4qqhBsfFPtALbZ/DIzMWIquThiTWxIg3JZS/ujYL2EwOBEJ18zQnc3NvFQ8tax5rz5mz0u3STTasn/xaFsnEO45GwaAIXW4ygnAa2sg5udI8RbyA25hEPfKDMypIAJgqsFLjyp1BGeQdqoHw2fJ6ECntKxiq9oLvJbYX+mgxxp9KIqxfg1yyjpg37MoY19U+iPytbMTLNkIZGXFXK3Zz5PE4iL1rVpqhAgMBAAGjEzARMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAAOjP27zad26Rs5b20XkqLAoNGgIu2gaNhzLE0Uw0gZDeYYOnAfaBwZ0oOFL1PSQ3/u4C9wBH8o0sRrnk+OGCL/HzSSb6BV1SClLwhdCONP37PLhjJf6LLG1D7MDMaZvfwy3hVW7JWAf1F/chUnOJXfwfwxNEaRqa5CKelTfOXCDdDxdCOj2mr1IH2WNzcWV5xuPXmbLjCsWyyJ4F/7Dpu6MhlAeUGq8jynhO6UfEYDUxcpMXGhuXgllEbkli2CDZQX3LaBaiSM8rWyUaImnjVD8ouQVJCZS/DeP0DDXNdY195RvVebb0RPfPj8pqRziPfam2rxin/ws3FvZnfEczZw="));
        when(kmsSigner.sign(any(JWSHeader.class), any(byte[].class)))
                .thenReturn(
                        Base64URL.encode(
                                "n7B1lp5IfJ-jBRsJh4XfaFd1VoJkYoVOSVWKYE64314BbP7luFt0p4rZhEVaqTUxGC7A4vMiQiZPZC6mqQjKqKUj6SbBTAQJrhRBhi245FI73i4-uxntXWHEIRCgKXrF9rgdtwctCUEnJeYsYy75JinJxN3e-FN_nVGqQcbUXEZDZy5GITlw0VfY4ljyOLWDQUymMR6vi5GCtMEOhF8rOIruQ5Za4vUL2bwwE7UAK7vH1HJ3UA7bunNYV043h-4GGx4N3ksCA3yccrbnq2ESLoOjgNXo8bnrfZVIyVNE5CMZikgFJTn3DDzSwHwgO5bggudaTZJL2eBpG7EcQD4RQQ"));

        when(dvaCryptographyServiceConfiguration.getEncryptionCertThumbprints())
                .thenReturn(
                        new Thumbprints(
                                SHA_1_THUMBPRINT + "-encryption",
                                SHA_256_THUMBPRINT + "-encryption"));
        when(dvaCryptographyServiceConfiguration.getEncryptionCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        JWSObject jwsResponseObject =
                dvaCryptographyService.preparePayload(createSuccessDvaResponse());

        when(dvaCryptographyServiceConfiguration.getSigningCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_DCS_SIGNING_CERT));
        when(kmsSigner.sign(any(JWSHeader.class), any(byte[].class)))
                .thenReturn(jwsResponseObject.getSignature());

        DvaResponse dvaResponse =
                dvaCryptographyService.unwrapDvaResponse(jwsResponseObject.serialize());

        assertNotNull(dvaResponse);
        assertEquals(
                objectMapper.writeValueAsString(createSuccessDvaResponse()),
                objectMapper.writeValueAsString(dvaResponse));
    }

    private DvaPayload createSuccessDvaPayload() {
        DvaPayload dvaPayload = new DvaPayload();
        dvaPayload.setRequestId(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")); // dummy UUID
        dvaPayload.setForenames(Arrays.asList("KENNETH"));
        dvaPayload.setSurname("DECERQUEIRA");
        dvaPayload.setDriverLicenceNumber("12345678");
        dvaPayload.setDateOfBirth(LocalDate.of(1965, 7, 8));
        dvaPayload.setPostcode("BA2 5AA");
        dvaPayload.setDateOfIssue(LocalDate.of(2018, 4, 19));
        dvaPayload.setExpiryDate(LocalDate.of(2042, 10, 1));
        dvaPayload.setIssuerId("DVA");
        return dvaPayload;
    }

    private DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        dvaResponse.setRequestHash(
                "98882b9f7f4173eb355f00a7510132b40aec702768a645c195678294bc16768d");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }

    private PrivateKey getPrivateKey(String privateKey)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
    }
}
