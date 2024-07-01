package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.encryption;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.DefaultCMSSignatureEncryptionAlgorithmFinder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AwsKmsContentEncryptor implements ContentSigner {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final String keyId;
    final EncryptionAlgorithmSpec encryptionAlgorithmSpec;
    final AlgorithmIdentifier encryptionAlgorithm;

    public AwsKmsContentEncryptor(String keyId, EncryptionAlgorithmSpec encryptionAlgorithmSpec) {
        this.keyId = keyId;
        this.encryptionAlgorithmSpec = encryptionAlgorithmSpec;
        String encryptionAlgorithmName = encryptionAlgorithmNameBySpec.get(encryptionAlgorithmSpec);
        if (encryptionAlgorithmName == null)
            throw new IllegalArgumentException("Unknown signature algorithm " + encryptionAlgorithmSpec);
        this.encryptionAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);

    }

    @Override
    public byte[] getSignature() {
        try (   KmsClient kmsClient = KmsClient.create() ) {
            EncryptRequest encryptRequest = EncryptRequest.builder()
                    .encryptionAlgorithm(encryptionAlgorithmSpec)
                    .keyId(keyId)
                    .plaintext(SdkBytes.fromByteArray("ab".getBytes(StandardCharsets.UTF_8)))
                    .build();
            EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);
            SdkBytes signatureSdkBytes = encryptResponse.ciphertextBlob();
            return signatureSdkBytes.asByteArray();
        } finally {
            outputStream.reset();
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return encryptionAlgorithm;
    }

    final static Map<EncryptionAlgorithmSpec, String> encryptionAlgorithmNameBySpec;

    static {
        encryptionAlgorithmNameBySpec = new HashMap<>();
        encryptionAlgorithmNameBySpec.put(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_1, "RSAES_OAEP_SHA_1");
        encryptionAlgorithmNameBySpec.put(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256, "RSAES_OAEP_SHA_256");
    }
}