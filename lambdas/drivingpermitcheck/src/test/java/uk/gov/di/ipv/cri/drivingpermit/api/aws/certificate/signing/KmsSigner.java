package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing;

import com.nimbusds.jose.util.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class KmsSigner {
    private final KmsClient kmsClient;
    private final String keyId;

    public KmsSigner(String keyId) {
        this.kmsClient = KmsClient.create();
        this.keyId = keyId;
    }

    public byte[] sign(byte[] data) {
        try {
        // Create the sign request
        SignRequest signRequest = SignRequest.builder()
                .keyId(keyId)
                .message(SdkBytes.fromByteArray(data))
                .messageType(MessageType.RAW)
                .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                .build();

        // Sign the message using KMS
        SignResponse signResponse = kmsClient.sign(signRequest);

        // Get the signature from the sign result
        ByteBuffer signatureBuffer = signResponse.signature().asByteBuffer();
        byte[] signatureBytes = new byte[signatureBuffer.remaining()];
        signatureBuffer.get(signatureBytes);

        // Encode the signature in Base64URL
        //String base64URLSignature = Base64.encode(signatureBytes).toString();
        return signatureBytes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign data with AWS KMS", e);
        }
    }

    public String getKeyId() {
        return keyId;
    }
}
