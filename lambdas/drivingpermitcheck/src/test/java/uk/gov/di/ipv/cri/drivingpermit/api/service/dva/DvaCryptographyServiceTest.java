package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.BASE64_DCS_SIGNING_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.BASE64_DCS_SIGNING_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.BASE64_ENCRYPTION_PRIVATE_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.BASE64_ENCRYPTION_PUBLIC_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.SHA_1_THUMBPRINT;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.KeyUtilities.SHA_256_THUMBPRINT;

@ExtendWith(MockitoExtension.class)
class DvaCryptographyServiceTest {

    @Mock private ConfigurationService configurationService;
    @Mock private DvaConfiguration dvaConfiguration;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preparePayload()
            throws CertificateException, JOSEException, JsonProcessingException,
                    InvalidKeySpecException, NoSuchAlgorithmException, ParseException {
        when(configurationService.getDvaConfiguration()).thenReturn(dvaConfiguration);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(configurationService);

        when(dvaConfiguration.getSigningKey()).thenReturn(getPrivateKey(BASE64_DCS_SIGNING_KEY));
        when(dvaConfiguration.getSigningCertThumbprints())
                .thenReturn(new Thumbprints(SHA_1_THUMBPRINT, SHA_256_THUMBPRINT));

        when(dvaConfiguration.getEncryptionCertThumbprints())
                .thenReturn(
                        new Thumbprints(
                                SHA_1_THUMBPRINT + "-encryption",
                                SHA_256_THUMBPRINT + "-encryption"));
        when(dvaConfiguration.getEncryptionCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        DvaPayload dvaPayload = createSuccessDvaPayload();
        JWSObject jwsObject = dvaCryptographyService.preparePayload(dvaPayload);

        JWSHeader jwtHeader = jwsObject.getHeader();
        Payload payload = jwsObject.getPayload();

        String[] innerJwt = payload.toString().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String innerJwtHeaderString = new String(decoder.decode(innerJwt[0]));
        JWEHeader innerJwtHeader = JWEHeader.parse(innerJwtHeaderString);

        String innerPayload = new String(decoder.decode(innerJwt[1]));

        assertEquals(SHA_1_THUMBPRINT, jwtHeader.getCustomParam("x5t"));
        assertEquals(SHA_256_THUMBPRINT, jwtHeader.getCustomParam("x5t#S256"));

        assertEquals(
                SHA_1_THUMBPRINT + "-encryption",
                innerJwtHeader.getX509CertThumbprint().toString());
        assertEquals(
                SHA_256_THUMBPRINT + "-encryption",
                innerJwtHeader.getX509CertSHA256Thumbprint().toString());

        assertNotNull(innerPayload);
    }

    @Test
    void unwrapDvaResponse()
            throws CertificateException, JOSEException, JsonProcessingException,
                    InvalidKeySpecException, NoSuchAlgorithmException, ParseException {
        when(configurationService.getDvaConfiguration()).thenReturn(dvaConfiguration);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(configurationService);

        when(dvaConfiguration.getSigningKey()).thenReturn(getPrivateKey(BASE64_DCS_SIGNING_KEY));
        when(dvaConfiguration.getSigningCertThumbprints())
                .thenReturn(new Thumbprints(SHA_1_THUMBPRINT, SHA_256_THUMBPRINT));

        when(dvaConfiguration.getEncryptionCertThumbprints())
                .thenReturn(
                        new Thumbprints(
                                SHA_1_THUMBPRINT + "-encryption",
                                SHA_256_THUMBPRINT + "-encryption"));
        when(dvaConfiguration.getEncryptionCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        JWSObject jwsResponseObject =
                dvaCryptographyService.preparePayload(createSuccessDvaResponse());

        when(dvaConfiguration.getSigningCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_DCS_SIGNING_CERT));
        when(dvaConfiguration.getEncryptionKey())
                .thenReturn(getPrivateKey(BASE64_ENCRYPTION_PRIVATE_KEY));

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
