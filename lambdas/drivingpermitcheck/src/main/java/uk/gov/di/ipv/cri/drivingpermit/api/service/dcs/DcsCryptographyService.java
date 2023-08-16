package uk.gov.di.ipv.cri.drivingpermit.api.service.dcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ProtectedHeader;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.request.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response.DcsSignedEncryptedResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Map;

public class DcsCryptographyService {

    private final ConfigurationService configurationService;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public DcsCryptographyService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public JWSObject preparePayload(DcsPayload documentDetails)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException,
                    JOSEException, JsonProcessingException {
        JWSObject signedDocumentDetails =
                createJWS(objectMapper.writeValueAsString(documentDetails));
        JWEObject encryptedDocumentDetails = createJWE(signedDocumentDetails.serialize());
        return createJWS(encryptedDocumentDetails.serialize());
    }

    public DcsResponse unwrapDcsResponse(String dcsSignedEncryptedResponseString)
            throws JOSEException, ParseException, JsonProcessingException, CertificateException {
        DcsSignedEncryptedResponse dcsSignedEncryptedResponse =
                new DcsSignedEncryptedResponse(dcsSignedEncryptedResponseString);
        JWSObject outerSignedPayload = JWSObject.parse(dcsSignedEncryptedResponse.getPayload());
        if (isInvalidSignature(outerSignedPayload)) {
            throw new IpvCryptoException("DCS Response Outer Signature invalid.");
        }
        JWEObject encryptedSignedPayload =
                JWEObject.parse(outerSignedPayload.getPayload().toString());
        JWSObject decryptedSignedPayload = decrypt(encryptedSignedPayload);
        if (isInvalidSignature(decryptedSignedPayload)) {
            throw new IpvCryptoException("DCS Response Inner Signature invalid.");
        }
        try {
            return objectMapper.readValue(
                    decryptedSignedPayload.getPayload().toString(), DcsResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IpvCryptoException(
                    String.format(
                            "Failed to parse decrypted DCS response: %s", exception.getMessage()));
        }
    }

    private JWSObject createJWS(String stringToSign) throws JOSEException, JsonProcessingException {

        ProtectedHeader protectedHeader =
                new ProtectedHeader(
                        JWSAlgorithm.RS256.toString(),
                        configurationService.getSigningCertThumbprintsDcs().getSha1Thumbprint(),
                        configurationService.getSigningCertThumbprintsDcs().getSha256Thumbprint());

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

        jwsObject.sign(new RSASSASigner(configurationService.getDrivingPermitCriSigningKey()));

        return jwsObject;
    }

    private JWEObject createJWE(String data) throws JOSEException, CertificateException {

        var header =
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256)
                        .type(new JOSEObjectType("JWE"))
                        .build();
        var jwe = new JWEObject(header, new Payload(data));

        jwe.encrypt(
                new RSAEncrypter(
                        (RSAPublicKey) configurationService.getDcsEncryptionCert().getPublicKey()));

        if (!jwe.getState().equals(JWEObject.State.ENCRYPTED)) {
            throw new IpvCryptoException("Something went wrong, couldn't encrypt JWE");
        }

        return jwe;
    }

    private boolean isInvalidSignature(JWSObject jwsObject)
            throws CertificateException, JOSEException {
        RSASSAVerifier rsassaVerifier =
                new RSASSAVerifier(
                        (RSAPublicKey) configurationService.getDcsSigningCert().getPublicKey());
        return !jwsObject.verify(rsassaVerifier);
    }

    public JWSObject decrypt(JWEObject encrypted) {
        try {
            RSADecrypter rsaDecrypter =
                    new RSADecrypter(configurationService.getDrivingPermitEncryptionKey());
            encrypted.decrypt(rsaDecrypter);

            return JWSObject.parse(encrypted.getPayload().toString());
        } catch (ParseException | JOSEException exception) {
            throw new IpvCryptoException(
                    String.format("Cannot Decrypt DCS Payload: %s", exception.getMessage()));
        }
    }
}
