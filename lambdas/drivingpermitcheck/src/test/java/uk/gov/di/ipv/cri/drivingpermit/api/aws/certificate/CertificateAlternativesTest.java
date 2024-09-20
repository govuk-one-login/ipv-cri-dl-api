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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils.CryptoUtils;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.JweKmsDecrypter;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

// @Disabled
@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class CertificateAlternativesTest {

    public static final String SIGNING_KEY_ID = System.getenv("SIGNING_KEY_ID");
    public static final String ENC_KEY_ID = System.getenv("ENC_KEY_ID");

    @Mock private DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration;

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    public void setup() {
        environmentVariables.set("SQS_AUDIT_EVENT_PREFIX", "PREFIX_CRI");
        environmentVariables.set(
                "COMMON_PARAMETER_NAME_PREFIX", "driving-permit-common-cri-api-local");
        environmentVariables.set("SQS_AUDIT_EVENT_QUEUE_URL", "arn-for-sqs");
        environmentVariables.set("HAS_CA", true);
    }

    @Test
    @Tag("TestKmsEncryption")
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

    @Test
    @Tag("TestToCreateDvaResponseForE2ETest")
    void createDVAResponse() throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // Step 1: Load certificates created by ACM
        String signingCertificatePath = "signingCert-acm.cer";
        X509Certificate signingCertificate = CryptoUtils.loadCertificate(signingCertificatePath);

        String certificatePath = "encryptionCert-acm.cer";
        X509Certificate certificate = CryptoUtils.loadCertificate(certificatePath);

        when(dvaCryptographyServiceConfiguration.getEncryptionCertThumbprints())
                .thenReturn(new Thumbprints("sha1-encryption", "sha256-encryption"));
        when(dvaCryptographyServiceConfiguration.getEncryptionCert()).thenReturn(certificate);
        when(dvaCryptographyServiceConfiguration.getHasCA()).thenReturn("true");

        // Step 1: Sign data using private key
        KmsClient kmsClient = new ServiceFactory().getClientProviderFactory().getKMSClient();
        uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner kmsSigner =
                new uk.gov.di.ipv.cri.drivingpermit.library.dva.util.KmsSigner(
                        SIGNING_KEY_ID, signingCertificate, kmsClient);

        JweKmsDecrypter jweKmsDecrypter = new JweKmsDecrypter(ENC_KEY_ID, kmsClient);

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(
                        dvaCryptographyServiceConfiguration, kmsSigner, jweKmsDecrypter);
        JWSObject jwsObject = dvaCryptographyService.preparePayload(createSuccessDvaResponse());

        System.out.println(jwsObject.serialize());
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

    private static DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        // below is request hash for a test response
        dvaResponse.setRequestHash(
                "5d9ecf62b4a0f2782bfa9353f578d62f6ed9c10d3fd10a6f0a408d49702bde06");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }
}
