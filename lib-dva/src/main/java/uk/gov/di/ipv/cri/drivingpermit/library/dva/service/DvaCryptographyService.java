package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.DvaInterface;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.ProtectedHeader;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaSignedEncryptedResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Map;

public class DvaCryptographyService {

    private final DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration;
    private final Map<String, KmsSigner> kmsSigners;
    private final Map<String, JweKmsDecrypter> jweKmsDecrypters;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public DvaCryptographyService(
            DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration,
            Map<String, KmsSigner> kmsSigners,
            Map<String, JweKmsDecrypter> jweKmsDecrypters) {
        this.kmsSigners = kmsSigners;
        this.dvaCryptographyServiceConfiguration = dvaCryptographyServiceConfiguration;
        this.jweKmsDecrypters = jweKmsDecrypters;
    }

    public JWSObject preparePayload(DvaInterface documentDetails)
            throws IOException, GeneralSecurityException, JOSEException {
        JWSObject signedDocumentDetails =
                createJWS(objectMapper.writeValueAsString(documentDetails));
        JWEObject encryptedDocumentDetails = createJWE(signedDocumentDetails.serialize());
        return createJWS(encryptedDocumentDetails.serialize());
    }

    public DvaResponse unwrapDvaResponse(String dvaSignedEncryptedResponseString)
            throws ParseException, JOSEException {
        DvaSignedEncryptedResponse dvaSignedEncryptedResponse =
                new DvaSignedEncryptedResponse(dvaSignedEncryptedResponseString);
        JWSObject outerSignedPayload = JWSObject.parse(dvaSignedEncryptedResponse.getPayload());
        if (isInvalidSignature(outerSignedPayload)) {
            throw new IpvCryptoException("Dva Response Outer Signature invalid.");
        }
        JWEObject encryptedSignedPayload =
                JWEObject.parse(outerSignedPayload.getPayload().toString());
        JWSObject decryptedSignedPayload = decrypt(encryptedSignedPayload);
        if (isInvalidSignature(decryptedSignedPayload)) {
            throw new IpvCryptoException("Dva Response Inner Signature invalid.");
        }
        try {
            return objectMapper.readValue(
                    decryptedSignedPayload.getPayload().toString(), DvaResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IpvCryptoException(
                    String.format(
                            "Failed to parse decrypted Dva response: %s", exception.getMessage()));
        }
    }

    private JWSObject createJWS(String stringToSign)
            throws JOSEException, IOException, GeneralSecurityException {
        String signingCertSha = System.getenv("SIGNING_CERT_SHA");
        if (!kmsSigners.containsKey(signingCertSha)) {
            throw new IOException("expected signing sha cannot be found");
        }
        KmsSigner kmsSigner = kmsSigners.get(signingCertSha);
        Thumbprints thumbprints = KeyCertHelper.makeThumbprint(kmsSigner.getDlSigningCertificate());

        ProtectedHeader protectedHeader =
                new ProtectedHeader(
                        JWSAlgorithm.RS256.toString(),
                        thumbprints.getSha1Thumbprint(),
                        thumbprints.getSha256Thumbprint());

        String jsonHeaders = objectMapper.writeValueAsString(protectedHeader);

        JWSObject jwsObject =
                new JWSObject(
                        new JWSHeader.Builder(JWSAlgorithm.RS256)
                                .customParams(
                                        objectMapper.readValue(
                                                jsonHeaders,
                                                new TypeReference<Map<String, Object>>() {}))
                                .build(),
                        new Payload(stringToSign));

        jwsObject.sign(kmsSigner);

        return jwsObject;
    }

    private JWEObject createJWE(String data) throws JOSEException, JsonProcessingException {

        ProtectedHeader protectedHeader =
                new ProtectedHeader(
                        JWSAlgorithm.RS256.toString(),
                        dvaCryptographyServiceConfiguration
                                .getEncryptionCertThumbprints()
                                .getSha1Thumbprint(),
                        dvaCryptographyServiceConfiguration
                                .getEncryptionCertThumbprints()
                                .getSha256Thumbprint());

        String jsonHeaders = objectMapper.writeValueAsString(protectedHeader);

        var header =
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128CBC_HS256)
                        .customParams(
                                objectMapper.readValue(
                                        jsonHeaders, new TypeReference<Map<String, Object>>() {}))
                        .type(new JOSEObjectType("JWE"))
                        .build();
        var jwe = new JWEObject(header, new Payload(data));

        jwe.encrypt(
                new RSAEncrypter(
                        (RSAPublicKey)
                                dvaCryptographyServiceConfiguration
                                        .getEncryptionCert()
                                        .getPublicKey()));

        if (!jwe.getState().equals(JWEObject.State.ENCRYPTED)) {
            throw new IpvCryptoException("Something went wrong, couldn't encrypt JWE");
        }

        return jwe;
    }

    private boolean isInvalidSignature(JWSObject jwsObject) throws JOSEException {
        RSASSAVerifier rsassaVerifier =
                new RSASSAVerifier(
                        (RSAPublicKey)
                                dvaCryptographyServiceConfiguration
                                        .getSigningCert()
                                        .getPublicKey());
        return !jwsObject.verify(rsassaVerifier);
    }

    public JWSObject decrypt(JWEObject encrypted) {
        try {
            String sha256ForEncryptionCert =
                    encrypted.getHeader().getX509CertSHA256Thumbprint().toString();
            if (jweKmsDecrypters.containsKey(sha256ForEncryptionCert)) {
                encrypted.decrypt(jweKmsDecrypters.get(sha256ForEncryptionCert));
            } else {
                encrypted.decrypt((JWEDecrypter) jweKmsDecrypters.values().toArray()[0]);
            }

            return JWSObject.parse(encrypted.getPayload().toString());
        } catch (ParseException | JOSEException exception) {
            throw new IpvCryptoException(
                    String.format("Cannot Decrypt Dva Payload: %s", exception.getMessage()));
        }
    }
}
