package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class CertificateAlternativesTest {

    public static final String SIGNING_KEY_ID = System.getenv("SIGNING_KEY_ID");
    public static final String ENC_KEY_ID = System.getenv("ENC_KEY_ID");
    public static final String SIGNING_CERT_SHA = System.getenv("SIGNING_CERT_SHA");

    @Mock private DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration;

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    public void setup() {
        environmentVariables.set("SQS_AUDIT_EVENT_PREFIX", "PREFIX_CRI");
        environmentVariables.set(
                "COMMON_PARAMETER_NAME_PREFIX", "driving-permit-common-cri-api-local");

        environmentVariables.set("SQS_AUDIT_EVENT_QUEUE_URL", "arn-for-sqs");
        environmentVariables.set("HAS_CA", true);
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "dl-cri-api-v1");
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", "common-cri-api");
    }

    @Test
    @Tag("TestKmsEncryption")
    @Tag("Crypto-regression")
    void encryptDecryptTest() throws Exception {
        // The source of randomness
        String certificatePath = "encryptionCert-acm.cer";
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
        assertEquals("Hello, World!", textBytes);
    }

    @Test
    @Tag("TestKmsVerification")
    @Tag("Crypto-regression")
    void signAndVerifyTest() throws Exception {
        // Step 1: Load certificate created by ACM
        String certificatePath = "signingCert-acm.cer";
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        // Step 2: Sign data using private key
        KmsSigner kmsSigner =
                new KmsSigner(
                        SIGNING_KEY_ID,
                        certificate,
                        new ServiceFactory().getClientProviderFactory().getKMSClient());
        JWSObject jwsObject = createJwt();
        jwsObject.sign(kmsSigner);

        System.out.println("Signed JWT Data: " + jwsObject.serialize());

        // Verify the JWT signature using the X.509 certificate
        boolean isSignatureValid = verifyJWT(jwsObject.serialize(), certificate.getPublicKey());
        System.out.println("Is JWT signature valid? " + isSignatureValid);
        assert (isSignatureValid);
    }

    @ParameterizedTest
    @Tag("TestToCreateDvaResponseForE2ETest")
    @Tag("Crypto-regression")
    @CsvSource({
        "a06d209caf647292dd8a3b7ef174485633899a470b7164707ec4aa0235072758, true",
        "36f775f0c8601c34491d81025848ddec12e07007004046656233e46844c386ef, true",
        "82f28bae8adc86450b9591f7a445b4f16d2599567b41824b64babab5198db01e, false",
        "2c5a775c9289f5cb8793b9603b661c92a6f92508c67125d2649bbc74b2b5e389, false",
        "cda3b619bde5cec58a5f83bd0de6226d59f21c1e400edd1ee411764b824a3321, false",
        "0e1be782c192f52229a18beec11f4515edb1ec68dbec9ef26fc6268f2a38f74c, false",
        "4e69497f79fe8353e32c1dd194e8e83a8868ea3557a79359452e13f5411891f5, false",
        "518550a4d8629a849669d6cb4d7cdd0a8731d6bec14e57633a38442f5393cc3d, false",
        "62c3c2aa3e9fba134d3e80957ac9d5dbf93a320bbdff3974fb3d4dae07694ee2, false",
        "7550a1b10b2cae1ee3a14d201ec87a74e6a63a81337c9ea7fcf12c38cce442e7, false",
        "33708c0884704e1fb54c76123a2ceb79b6e33a5bef5d73a0ace96e42c14b31d3, false",
        "ba0c83f9111e1ba03fd33abe5d418101c34dbad2b6ccf8e03278d6c82090966f, false",
        "ad3115f790703283bcd6ceffff246bb7f8d19a5cadb75c6a37944ac5c0b4ab4e, false"
    })
    void createDVAResponse(String requestHash, String validDoc) throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // Step 1: Load certificates created by ACM
        String signingCertificatePath = "signingCert-acm.cer";
        X509Certificate signingCertificate = CryptoUtils.loadCertificate(signingCertificatePath);

        String certificatePath = "encryptionCert-acm.cer";
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        when(dvaCryptographyServiceConfiguration.getEncryptionCertThumbprints())
                .thenReturn(KeyCertHelper.makeThumbprint(certificate));
        when(dvaCryptographyServiceConfiguration.getEncryptionCert()).thenReturn(certificate);

        // Step 1: Sign data using private key
        KmsClient kmsClient = new ServiceFactory().getClientProviderFactory().getKMSClient();
        uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner kmsSigner =
                new uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner(
                        SIGNING_KEY_ID, signingCertificate, kmsClient);

        JweKmsDecrypter jweKmsDecrypter = new JweKmsDecrypter(ENC_KEY_ID, kmsClient);

        Map<String, uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner> kmsSigners =
                Map.of(SIGNING_CERT_SHA, kmsSigner);
        Map<String, JweKmsDecrypter> jweKmsDecrypters =
                Map.of(
                        KeyCertHelper.makeThumbprint(certificate).getSha256Thumbprint(),
                        jweKmsDecrypter);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration, kmsSigners, jweKmsDecrypters);
        JWSObject jwsObject =
                dvaCryptographyService.preparePayload(
                        createSuccessDvaResponse(requestHash, Boolean.parseBoolean(validDoc)));

        System.out.println("RequestHash: " + requestHash);
        System.out.println(jwsObject.serialize());
        System.out.println("__________________________________");
        assertNotNull(jwsObject);
    }

    public static String decryptData(String encryptedData, String keyId) throws Exception {
        try (KmsClient kmsClient = new ServiceFactory().getClientProviderFactory().getKMSClient()) {
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
        return new JWSObject(header, claimsSet.toPayload());
    }

    private static DvaResponse createSuccessDvaResponse(String requestHash, boolean validDocument) {
        DvaResponse dvaResponse = new DvaResponse();
        // below is request hash for a test response
        dvaResponse.setRequestHash(requestHash);
        dvaResponse.setValidDocument(validDocument);
        return dvaResponse;
    }
}
