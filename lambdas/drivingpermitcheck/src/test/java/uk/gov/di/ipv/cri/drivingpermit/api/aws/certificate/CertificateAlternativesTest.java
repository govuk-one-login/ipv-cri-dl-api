package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;

import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.encryption.EncryptionCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.JwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing.SigningCertificateFromKmsKey;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CertificateAlternativesTest {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

    //This test must be run first followed by the bottom 2
    @Test
    public void testingKmsKeyExport() throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        String keyId = "Signing_key_id";
        Certificate certificate = SigningCertificateFromKmsKey.certificateFromKmsKey(keyId);
        String signingCert = formatCrtFileContents(certificate);

        Path path = Paths.get("signingCert.cer");
        byte[] strToBytes = signingCert.getBytes();
        Files.write(path, strToBytes);

        String enckeyId = "encryption_key_id";
        Certificate encCertificate = EncryptionCertificateFromKmsKey.certificateFromKmsKey(enckeyId);
        String encryptionCert = formatCrtFileContents(encCertificate);

        Path encPath = Paths.get("encryptionCert.cer");
        byte[] encStrToBytes = encryptionCert.getBytes();
        Files.write(encPath, encStrToBytes);
    }

    @Test
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
        String textBytes = decryptData(encryptedData, "encryption_key_id");
        System.out.println("Message decrypted with file private key:\n" + textBytes + "\n");
        assertEquals(textBytes, "Hello, World!");
    }

    @Test
    public void signAndVerifyTest() throws Exception {
        // The source of randomness
        String certificatePath = "signingCert.cer";

        // Step 1: Sign data using private key
        KmsSigner kmsSigner = new KmsSigner("Signing_key_id");
        JWSObject jwsObject = new JwsSigner(kmsSigner).createSignedJws();

        System.out.println("Signed JWT Data: " + jwsObject.serialize());


        // Step 1: Load the X.509 certificate
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        // Verify the JWT signature using the X.509 certificate
        boolean isSignatureValid = verifyJwtSignature(jwsObject.serialize(), certificate);
        System.out.println("Is JWT signature valid? " + isSignatureValid);
    }

    public static String formatCrtFileContents(final Certificate certificate) throws CertificateEncodingException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

        final byte[] rawCrtText = certificate.getEncoded();
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        final String prettified_cert = BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        return prettified_cert;
    }

    public static String decryptData(String encryptedData, String keyId) throws Exception {
        try (KmsClient kmsClient = KmsClient.create()) {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            DecryptRequest decryptRequest = DecryptRequest.builder()
                    .keyId(keyId)
                    .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
                    .ciphertextBlob(SdkBytes.fromByteArray(encryptedBytes))
                    .build();
            DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
            byte[] decryptedBytes = decryptResponse.plaintext().asByteArray();
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }
    }

    public static boolean verifyJwtSignature(String jwtString, X509Certificate certificate) throws Exception {
        SignedJWT jwsObject = SignedJWT.parse(jwtString);
        PublicKey publicKey = certificate.getPublicKey();

        RSASSAVerifier rsassaVerifier =
                new RSASSAVerifier((RSAPublicKey) publicKey);
        return jwsObject.verify(rsassaVerifier);
    }
}
