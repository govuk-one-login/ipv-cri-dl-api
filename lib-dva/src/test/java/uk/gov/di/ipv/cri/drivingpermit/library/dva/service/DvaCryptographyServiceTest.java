package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
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
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.KeyUtilities.BASE64_ENCRYPTION_PUBLIC_CERT;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DvaCryptographyServiceTest {

    @Mock private DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration;
    @Mock private KmsSigner kmsSigner;
    @Mock private JweKmsDecrypter jweKmsDecrypter;

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preparePayload()
            throws GeneralSecurityException, JOSEException, IOException, ParseException,
                    java.text.ParseException {
        environmentVariables.set("SIGNING_CERT_SHA", "signsha256");
        when(kmsSigner.supportedJWSAlgorithms()).thenReturn(Set.of(RS256));
        when(kmsSigner.sign(any(JWSHeader.class), any(byte[].class)))
                .thenReturn(new Base64URL("123456789"));

        when(kmsSigner.getDlSigningCertificate())
                .thenReturn(
                        KeyCertHelper.getDecodedX509Certificate(
                                "MIIDBzCCAe+gAwIBAgIGAZF1WmijMA0GCSqGSIb3DQEBCwUAMDoxODA2BgNVBAMML0RyaXZpbmcgTGljZW5jZSBDUkkgSlNPTiBTaWduaW5nIERldiAyNS0wNi0yMDI0MB4XDTI0MDgyMTE0MzIyNFoXDTI1MDgyMTE0MzIyNFowOjE4MDYGA1UEAwwvRHJpdmluZyBMaWNlbmNlIENSSSBKU09OIFNpZ25pbmcgRGV2IDI1LTA2LTIwMjQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCoUPIfrOrbuP3q4fSJgmNC4YbmukpNCjqK/yrTp+ykB+GpxvjcVWymdb7ywRlqC49Qfl18gHne261B82YKLDIRViz+q36ZlC1woTAIl/SLeXPdEFx26nLO4qqhBsfFPtALbZ/DIzMWIquThiTWxIg3JZS/ujYL2EwOBEJ18zQnc3NvFQ8tax5rz5mz0u3STTasn/xaFsnEO45GwaAIXW4ygnAa2sg5udI8RbyA25hEPfKDMypIAJgqsFLjyp1BGeQdqoHw2fJ6ECntKxiq9oLvJbYX+mgxxp9KIqxfg1yyjpg37MoY19U+iPytbMTLNkIZGXFXK3Zz5PE4iL1rVpqhAgMBAAGjEzARMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAAOjP27zad26Rs5b20XkqLAoNGgIu2gaNhzLE0Uw0gZDeYYOnAfaBwZ0oOFL1PSQ3/u4C9wBH8o0sRrnk+OGCL/HzSSb6BV1SClLwhdCONP37PLhjJf6LLG1D7MDMaZvfwy3hVW7JWAf1F/chUnOJXfwfwxNEaRqa5CKelTfOXCDdDxdCOj2mr1IH2WNzcWV5xuPXmbLjCsWyyJ4F/7Dpu6MhlAeUGq8jynhO6UfEYDUxcpMXGhuXgllEbkli2CDZQX3LaBaiSM8rWyUaImnjVD8ouQVJCZS/DeP0DDXNdY195RvVebb0RPfPj8pqRziPfam2rxin/ws3FvZnfEczZw="));

        Map<String, uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner> kmsSigners =
                Map.of("signsha256", kmsSigner);
        Map<String, JweKmsDecrypter> jweKmsDecrypters =
                Map.of("bx7aYu1FDHz_uhm7T70IbB1516aYfHKCyohtvk51bok", jweKmsDecrypter);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration, kmsSigners, jweKmsDecrypters);
        DvaPayload dvaPayload = createSuccessDvaPayload();

        when(dvaCryptographyServiceConfiguration.getEncryptionCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(BASE64_ENCRYPTION_PUBLIC_CERT));

        JWSObject jwsObject = dvaCryptographyService.preparePayload(dvaPayload);

        JWSHeader jwtHeader = jwsObject.getHeader();
        Payload payload = jwsObject.getPayload();

        String[] innerJwt = payload.toString().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String innerJwtHeaderString = new String(decoder.decode(innerJwt[0]));
        JWEHeader innerJwtHeader = JWEHeader.parse(innerJwtHeaderString);

        String innerPayload = new String(decoder.decode(innerJwt[1]));

        Assertions.assertEquals("QTT1noQ0do6ztAtp-WTrdkV4xWI", jwtHeader.getCustomParam("x5t"));
        Assertions.assertEquals(
                "Wc9FKYU--By7Bs3ATYjEV5rl_CnZ9OH050ZPcwubxA0",
                jwtHeader.getCustomParam("x5t#S256"));

        assertEquals(
                "5raL6y3NlxvFIqrCa64HiYAYUiU",
                innerJwtHeader.getX509CertThumbprint().toString()); // NOSONAR
        assertEquals(
                "aUJzasL_WAwBGfSBl1zUHWb5HeGabx63Mk6Ho_-T-90",
                innerJwtHeader.getX509CertSHA256Thumbprint().toString());

        assertNotNull(innerPayload);
    }

    @Test
    void unwrapDvaResponse() throws Exception {

        String dlSigningCert =
                "MIIDsTCCApmgAwIBAgIRAOm99Of9G+2I5NNHOiOpF7UwDQYJKoZIhvcNAQELBQAwWjELMAkGA1UEBhMCR0IxGDAWBgNVBAoMD0dPVlVLIE9uZSBMb2dpbjEMMAoGA1UECwwDR0RTMSMwIQYDVQQDDBpHRFMgREwgRFZBIFRlc3QgUm9vdCBDQSBHMzAeFw0yNDA5MTIwOTE5MjdaFw0yNTEwMTIxMDE5MjdaMCYxJDAiBgNVBAMMG3Jldmlldy1kLmRldi5hY2NvdW50Lmdvdi51azCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOnCPzQoSUHJkcEdLKiRy36Y1bFxVPs+XODQ2btTP2ebMB7tOtDwjS6KZU7dWhdufjpJS8KYRXbvZDlKhAQKGSg+qO8404bzEqIafeUSs7RToE2wmFVYYZBwjQM8JBuoMyD/e8N1rUe0k10mEMGjDct9eTyGsx/hS1EUNF52Oa7kfFyYIF7aG1Q96Mq3xaSbVZOxJuXnNu3gB/tSNuCGowqECHCdEJ84BtxOVhy8Z7ywxHlrqlrtcYi1e6y5lSZq6/xTQPYETi0ir6lwv+0hgOnRHtnA8bOGmYrEqpG4PbPmi+AEpbUkIvw1kSnLYnvi4C6qgwdaLopvoj2axhH9QWMCAwEAAaOBpTCBojAmBgNVHREEHzAdghtyZXZpZXctZC5kZXYuYWNjb3VudC5nb3YudWswCQYDVR0TBAIwADAfBgNVHSMEGDAWgBT7nltAzxz+KhiyMnW4P3eWaLs+hDAdBgNVHQ4EFgQU5xNvBOue5Rt6EOtrv0HQkaHfm1QwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEA1z+I7oK8GubAJ5EtKQLRuvHadYWXbYUgxw1LVIHU0ggbIXpQMn6Bx6aJh/yJEH8N1PWS44yvIT5GDO+dC+GM9iSJZA7JbNMbw/dtaIO79VSGLK6P7EMUPn/7ztGDPZwNk0oTC7MJrGph7X6NJu3B+eYx0FXkV7H52ZMjF441t0MWYJcI/z6mzquhQykINWKlaJ5TX7HAtG/BCgCz9lDpmVy3JMxtwEMNtJy37UwBhZlmBHUdSO6W0XGVbTojB4K63NCnIri6XLXq8miChx/tQA+b6wWzLu3IzkfoZFQkOxE2qQKNoKvboG+PXurWLZvMt4cUEgQr1AWu3W7bw/Cv5w==";
        JWSObject jwsResponseObject =
                JWSObject.parse(
                        "eyJ4NXQjUzI1NiI6ImJ4N2FZdTFGREh6X3VobTdUNzBJYkIxNTE2YVlmSEtDeW9odHZrNTFib2siLCJhbGciOiJSUzI1NiIsIng1dCI6ImtmaVJNM01jQVNxV1dOUEl1bTNhVHZuNHd0cyJ9.ZXlKNE5YUWpVekkxTmlJNkluTm9ZVEkxTmkxbGJtTnllWEIwYVc5dUlpd2lkSGx3SWpvaVNsZEZJaXdpWlc1aklqb2lRVEV5T0VOQ1F5MUlVekkxTmlJc0ltRnNaeUk2SWxKVFFTMVBRVVZRSWl3aWVEVjBJam9pYzJoaE1TMWxibU55ZVhCMGFXOXVJbjAuaWpmQXBhOGthaGpsNWhxTGZUNzdMZjdHYVQ3YkRRSFU5d05uSTRVLUQ3MkYtdnhIS1Awc0lOeXUwdkNSSTN5QnQ4MS1NaXBMMDlTdkl2MGd6RlRFVExqUE1RVk5rTGc1V1Rlbjc3QjJLdTVFLU9La2VteVE2eXFWcFU5X3gyTFYtWUk4dlVwZWZBQ3dva2d6bXMwNEw0Y1VOSnJjTTFOVE9DanVWQ0lsZGVkYjUweno3VEpjRVRyazRtRFQ5TGlSTUtsTm1BMEo3alpLNHViZEFYRVhuSno2eUNIUVBkR1p2Q25sS080cVI2d0VBc3l3LVJYQ3o5X25qSkNYUlBLT2ZNMWZBcFZCLW9yUklDNlFyazkxY3BCczQxU1NEekRDMU4xNHhjUjRfc3VfeHhoOFBiSXZJWWc5UTJhTjdma3RnZmEzV1NCU1k1dWlrcENnenVMUFBnLkNFand4SmlVTzdRbG5VR3dTZjVRdlEuTkZPRzBLX1Q1ajR3N0lsazNpMjlBYllERmdMYlEza2JNTnhESkxjU1huVTNEZ1lZLWtoZlRKQ3ctV2Vqb18tTThoME1GV0lxM3Vad1lHVmY1bXZENzFuSEdoWnJBVjJ1MWhZUXNMZFRIbEdIX0FIb1RaRl80SHJDcmNLOFNVTUdFT2NYNGVreXE4d0FSM2VJdE8wRHpxU21aTHlveTg2V1BDeC1qYU5YejNSbnR3LV9JVm95bUtabjk4Y0xEeF9zTnk4RGpDWEQ0QmU0N0NGTnEtMEdIYnF3SUc0MU9GWFY2S0Q5bHJBNEFyQWR1b0dZV0h3TktSUHhfU1d5YTR1YVFzOHhOOWpwQXIyNEhBcUoySTFld2xMM0FaeTZzYVhwaURpdXRwLXFzWV8tSTd4X0hhR25WRFkzTm93a1FuYnJCUjZUUzZaRUNiSzJqTzlfQnNsVUxwcUg2MjRBZVlWaUo3NVdyYlRHa20ybnI5TUpzQmludjJGQ1liTUtTTXh1MWF0QllIeXpvSGQ2MDZRdUlleDJwTmx6d0FJaG5MellrNkNEX2hYNlNDbUhUUUdXTkc4Z3czLWw4VkQ3RUU3aWhtSzdDdHJRZVQxN2p5QXZ0SDB3WFZvT2prUFpTRmxzbTRaWTNvdjJYd3hTQUlXZDZSdlRiY3RIY09tYzNaMVo0dkRJMzlVSzd2VTFpdnRXRXI4Y05vbFRIemU4cExqZHZCa19KT3Q1TkxXS2tXLUJKeDhyRmwwWWZ1X29aN3otV0U4dWlYR093SHZTcnFpQ2VTWk1hSkhRbWUzQS1MNG9DckVDLUNoUlJKV1Q5MUJyWWYyVXlvSTJDTlY0Qmp1eWtUYTd0RnVac1d3anJYYzFOUC1OY2EwckxMUEYtdV83M0ltQmpaMXpCcE5BN2wxTW04b2cwYkMyZmsxaU9kUTdJdWpZN3VBWXN4S0F1cDRLWHJoN1FpLUVuOFk3WkN5WGstSVRFMWlfVTB2UHVvR0p4Vm1BOUdBUEpzanhjOTFwYlJMeXo1el93cWxMWGhLWVBhNXNqcC0tUWx1ZmMyS3RSRWVWRlFSSElxRGZkbXdnVWx5Q093aDJzcUlFZk9sLVhnd2xvV1NMNi0tRXhvNmpqVDBvMTgxQVhmRjR5WlJWY0lrT0hIZllhWUUuendYeTdBelBDcHVtMFBsVmdNUEh1QQ.sZ9RpiVRYM1ppiRmmL8LzkykfLzwVs38OBz6_CX7FoYCAk68oQepAsL5z_8cVYp5ggt7jFWN_rCaIY_SQVdamDJdyeLWUQX73uJ5KJo8btVNOzg28L1QOeAYr4GFtKRPe4nHNicZfcvs_lbh-zY0juq8cuD4y-iZSm0Ctvl387cfSPp54jdXz0YLH6RQM9zWAigdK0IRVLSrltw9HfMIL9Vo6bOsC8R6yQrMvlJHUoEuUYRGzAAD3S_7OaL63yq2s_wqEyCEoBLCTQk2_KF8-PYqOHaEmUNaiWt87nSt3Wi6AjZAvd99p3J7Qv1h5CpfObJFOO8V7ViqBENpgq9TgA");

        when(dvaCryptographyServiceConfiguration.getSigningCert())
                .thenReturn(KeyCertHelper.getDecodedX509Certificate(dlSigningCert));

        JWEObject jweObject = JWEObject.parse(jwsResponseObject.getPayload().toString());

        Base64URL encryptedKey = jweObject.getEncryptedKey();
        Base64URL iv = jweObject.getIV();
        Base64URL cipherText = jweObject.getCipherText();
        Base64URL authTag = jweObject.getAuthTag();
        byte[] aad =
                "eyJ4NXQjUzI1NiI6InNoYTI1Ni1lbmNyeXB0aW9uIiwidHlwIjoiSldFIiwiZW5jIjoiQTEyOENCQy1IUzI1NiIsImFsZyI6IlJTQS1PQUVQIiwieDV0Ijoic2hhMS1lbmNyeXB0aW9uIn0"
                        .getBytes();
        String decryptedValue =
                "eyJ4NXQjUzI1NiI6ImJ4N2FZdTFGREh6X3VobTdUNzBJYkIxNTE2YVlmSEtDeW9odHZrNTFib2siLCJhbGciOiJSUzI1NiIsIng1dCI6ImtmaVJNM01jQVNxV1dOUEl1bTNhVHZuNHd0cyJ9.eyJyZXF1ZXN0SGFzaCI6IjVkOWVjZjYyYjRhMGYyNzgyYmZhOTM1M2Y1NzhkNjJmNmVkOWMxMGQzZmQxMGE2ZjBhNDA4ZDQ5NzAyYmRlMDYiLCJ2YWxpZERvY3VtZW50Ijp0cnVlLCJlcnJvck1lc3NhZ2UiOm51bGx9.fsMAV74HUBXdrC0gJP7CpST79ixAAFX6FiLNmW7e2obTmdpUg__LzCnUIwJJY4ZrG3uiCq0Os6Z3CI4QEByfiQHVOv1q__bqEsult-hx3-rPh9-bwleyNRWKxdqeLzPklIsRTd-Y0ZKYOonCDpNJuW3YI6nPNVXJgHarlhnCpVwFbGP24XbO3LaxcDokq3LbpWDewU5ZcisbWpr60gjwk5qytQOeLj1vq8XJkQAVhFUWNjiMNVT2DRCcB67rYcX9a1NhmT3CYyhyGjwlZudgE154CZqG5iiVI8e7G3ULnzPF2FWrsmhmMhoUThyqCd0bvt1JAD4bIKfPcT_irkEn5g";
        when(jweKmsDecrypter.decrypt(
                        any(JWEHeader.class),
                        eq(encryptedKey),
                        eq(iv),
                        eq(cipherText),
                        eq(authTag),
                        eq(aad)))
                .thenReturn(decryptedValue.getBytes());

        Map<String, uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner> kmsSigners =
                Map.of("signsha256", kmsSigner);
        Map<String, JweKmsDecrypter> jweKmsDecrypters =
                Map.of("sha256-encryption", jweKmsDecrypter);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration, kmsSigners, jweKmsDecrypters);

        DvaResponse dvaResponse =
                dvaCryptographyService.unwrapDvaResponse(jwsResponseObject.serialize());

        assertNotNull(dvaResponse);
        assertEquals(
                objectMapper.writeValueAsString(createSuccessDvaResponse()),
                objectMapper.writeValueAsString(dvaResponse));
    }

    private DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        dvaResponse.setRequestHash(
                "5d9ecf62b4a0f2782bfa9353f578d62f6ed9c10d3fd10a6f0a408d49702bde06");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
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
}
