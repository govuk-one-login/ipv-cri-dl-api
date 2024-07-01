package uk.gov.di.ipv.cri.drivingpermit.library.dva.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.KmsException;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Set;

public class KmsSigner implements JWSSigner {
    private final KmsClient kmsClient;
    private final String keyId;

    public KmsSigner(String keyId) {
        this.kmsClient = KmsClient.create();
        this.keyId = keyId;
    }

    @Override
    public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
        try {
            // Create the sign request
            SignRequest signRequest =
                    SignRequest.builder()
                            .keyId(keyId)
                            .message(SdkBytes.fromByteArray(signingInput))
                            .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                            .build();

            // Perform the signing operation using KMS
            SignResponse signResult = kmsClient.sign(signRequest);

            // Return the Base64URL-encoded signature
            return Base64URL.encode(signResult.signature().asByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign data with AWS KMS", e);
        }
    }

    public PublicKey getPublicKey() throws Exception {
        try {
            // Create a GetPublicKeyRequest
            GetPublicKeyRequest getPublicKeyRequest =
                    GetPublicKeyRequest.builder().keyId(keyId).build();

            // Call KMS to get the public key
            GetPublicKeyResponse getPublicKeyResponse = kmsClient.getPublicKey(getPublicKeyRequest);

            // Extract the public key in DER format
            ByteBuffer publicKeyBuffer = getPublicKeyResponse.publicKey().asByteBuffer();
            byte[] publicKeyBytes = new byte[publicKeyBuffer.remaining()];
            publicKeyBuffer.get(publicKeyBytes);

            // Convert the DER-encoded public key to a PublicKey object
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory =
                    KeyFactory.getInstance(getPublicKeyResponse.keySpecAsString()); // E.g., "RSA"
            return keyFactory.generatePublic(spec);

        } catch (KmsException e) {
            System.err.println(e.getMessage());
            throw new Exception("Failed to retrieve the public key", e);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public Set<JWSAlgorithm> supportedJWSAlgorithms() {
        return Collections.singleton(JWSAlgorithm.RS256);
    }

    @Override
    public JCAContext getJCAContext() {
        return null;
    }
}
