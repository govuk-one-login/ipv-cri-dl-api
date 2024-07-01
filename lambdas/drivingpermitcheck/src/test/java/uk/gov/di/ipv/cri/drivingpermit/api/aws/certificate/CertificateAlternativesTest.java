package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.encryption.EncryptionCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.SigningCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.ProtectedHeader;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// @Disabled
@ExtendWith(MockitoExtension.class)
class CertificateAlternativesTest {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String SIGNING_KEY_ID = System.getenv("SIGNING_KEY_ID");
    public static final String ENC_KEY_ID = System.getenv("ENC_KEY_ID");

    // This test must be run first followed by the bottom 2
    @Test
    @Tag("CreateSelfSignedCertFromKMS")
    public void testingKmsKeyExport() throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        String keyId = SIGNING_KEY_ID;
        Certificate certificate = SigningCertificateFromKmsKey.certificateFromKmsKey(keyId);
        String signingCert = formatCrtFileContents(certificate);

        Path path = Paths.get("signingCert.cer");
        byte[] strToBytes = signingCert.getBytes();
        Files.write(path, strToBytes);

        String enckeyId = ENC_KEY_ID;
        Certificate encCertificate =
                EncryptionCertificateFromKmsKey.certificateFromKmsKey(enckeyId);
        String encryptionCert = formatCrtFileContents(encCertificate);

        Path encPath = Paths.get("encryptionCert.cer");
        byte[] encStrToBytes = encryptionCert.getBytes();
        Files.write(encPath, encStrToBytes);
    }

    @Test
    @Tag("TestKmsEncryption")
    public void encryptDecryptTest() throws Exception {
        // The source of randomness
        String certificatePath = "encryptionCert.cer";
        String dataToEncrypt = "Hello, World!";

        // Step 1: Load the X.509 certificate
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        // Step 2: Extract the public key from the certificate
        PublicKey publicKey = CryptoUtils.extractPublicKey(certificate);

        // Step 3: Encrypt the data using the public key
        String encryptedData = CryptoUtils.encryptData(dataToEncrypt, publicKey);

        System.out.println("Encrypted Data: " + encryptedData);
        String textBytes = decryptData(encryptedData, ENC_KEY_ID);
        System.out.println("Message decrypted with file private key:\n" + textBytes + "\n");
        assertEquals(textBytes, "Hello, World!");
    }

    @Test
    @Tag("TestKmsVerification")
    public void signAndVerifyTest() throws Exception {
        // The source of randomness
        String certificatePath = "signingCert.cer";

        // Step 1: Sign data using private key
        KmsSigner kmsSigner = new KmsSigner(SIGNING_KEY_ID);
        JWSObject jwsObject = createJwt();
        jwsObject.sign(kmsSigner);

        System.out.println("Signed JWT Data: " + jwsObject.serialize());

        // Step 1: Load the X.509 certificate
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        // Verify the JWT signature using the X.509 certificate
        boolean isSignatureValid = verifyJWT(jwsObject.serialize(), certificate.getPublicKey());
        System.out.println("Is JWT signature valid? " + isSignatureValid);
        assert (isSignatureValid);
    }

    @Test
    @Tag("TestToCreateDvaResponseForE2ETest")
    public void createDVAResponse() throws Exception {
        // Used to create payloads for the complete test in DVA ThirdPartyDocument Gateway

        // The source of randomness
        String signingCertificatePath = "signingCert.cer";
        X509Certificate signingCertificate = CryptoUtils.loadCertificate(signingCertificatePath);

        String certificatePath = "encryptionCert.cer";
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        String dataToEncrypt = new ObjectMapper().writeValueAsString(createSuccessDvaResponse());

        // Step 1: Sign data using private key
        KmsSigner kmsSigner = new KmsSigner(SIGNING_KEY_ID);

        JWSObject jwsInner = createJWS(dataToEncrypt, signingCertificate, kmsSigner);
        JWEObject jwe = createJWE(jwsInner.serialize(), certificate);
        JWSObject jwsOuter = createJWS(jwe.serialize(), signingCertificate, kmsSigner);

        System.out.println(jwsOuter.serialize());
        assertNotNull(jwsOuter);
    }

    public static String formatCrtFileContents(final Certificate certificate)
            throws CertificateEncodingException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

        final byte[] rawCrtText = certificate.getEncoded();
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        final String prettified_cert =
                BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        return prettified_cert;
    }

    public static String decryptData(String encryptedData, String keyId) throws Exception {
        try (KmsClient kmsClient = KmsClient.create()) {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            DecryptRequest decryptRequest =
                    DecryptRequest.builder()
                            .keyId(keyId)
                            .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
                            .ciphertextBlob(SdkBytes.fromByteArray(encryptedBytes))
                            .build();
            DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
            byte[] decryptedBytes = decryptResponse.plaintext().asByteArray();
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }
    }

    public static boolean verifyJWT(String serializedJWT, PublicKey publicKey) throws Exception {
        // Parse the JWT
        JWSObject jwsObject = JWSObject.parse(serializedJWT);

        // Create an RSA verifier using the public key
        RSASSAVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

        // Verify the signature
        return jwsObject.verify(verifier);
    }

    public static JWSObject createJwt() {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();

        // Create the JWT claims set
        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject("1234567890")
                        .issuer("issuer")
                        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                        .claim("name", "John Doe")
                        .build();

        // Create the JWS object
        JWSObject jwsObject = new JWSObject(header, claimsSet.toPayload());
        return jwsObject;
    }

    private static DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        // below is request hash for a test response
        dvaResponse.setRequestHash(
                "5d9ecf62b4a0f2782bfa9353f578d62f6ed9c10d3fd10a6f0a408d49702bde06");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }

    /////////////// Cryptography service clone //////////////////
    // Duplicates from the DVA cryptography service used to construct test payloads
    private JWSObject createJWS(
            String stringToSign, X509Certificate certificate, KmsSigner kmsSigner)
            throws JOSEException, IOException, GeneralSecurityException {

        Thumbprints thumbprints = KeyCertHelper.makeThumbprint(certificate);

        ProtectedHeader protectedHeader =
                new ProtectedHeader(
                        JWSAlgorithm.RS256.toString(),
                        thumbprints.getSha1Thumbprint(),
                        thumbprints.getSha256Thumbprint());

        String jsonHeaders = new ObjectMapper().writeValueAsString(protectedHeader);

        JWSObject jwsObject =
                new JWSObject(
                        new JWSHeader.Builder(JWSAlgorithm.RS256)
                                .customParams(
                                        new ObjectMapper()
                                                .readValue(
                                                        jsonHeaders,
                                                        new TypeReference<
                                                                Map<String, Object>>() {}))
                                .build(),
                        new Payload(stringToSign));

        jwsObject.sign(kmsSigner);

        return jwsObject;
    }

    private JWEObject createJWE(String data, X509Certificate certificate)
            throws JOSEException, JsonProcessingException, CertificateEncodingException,
                    NoSuchAlgorithmException {

        ProtectedHeader protectedHeader =
                new ProtectedHeader(
                        JWSAlgorithm.RS256.toString(),
                        KeyCertHelper.makeThumbprint(certificate).getSha1Thumbprint(),
                        KeyCertHelper.makeThumbprint(certificate).getSha256Thumbprint());

        String jsonHeaders = new ObjectMapper().writeValueAsString(protectedHeader);

        var header =
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128CBC_HS256)
                        .customParams(
                                new ObjectMapper()
                                        .readValue(
                                                jsonHeaders,
                                                new TypeReference<Map<String, Object>>() {}))
                        .type(new JOSEObjectType("JWE"))
                        .build();
        var jwe = new JWEObject(header, new Payload(data));

        jwe.encrypt(new RSAEncrypter((RSAPublicKey) certificate.getPublicKey()));

        if (!jwe.getState().equals(JWEObject.State.ENCRYPTED)) {
            throw new IpvCryptoException("Something went wrong, couldn't encrypt JWE");
        }

        return jwe;
    }
}
