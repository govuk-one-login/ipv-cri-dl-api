package uk.gov.di.ipv.cri.drivingpermit.library.dva.util;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.impl.AESCBC;
import com.nimbusds.jose.jca.JWEJCAContext;
import com.nimbusds.jose.util.Base64URL;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.HashSet;
import java.util.Set;

@ExcludeFromGeneratedCoverageReport
public class JweKmsDecrypter implements JWEDecrypter {

    private final KmsClient kmsClient;
    private final String keyId;

    public JweKmsDecrypter(String kmsKeyId, KmsClient kmsClient) {
        this.keyId = kmsKeyId;
        this.kmsClient = kmsClient;
    }

    @Override
    public byte[] decrypt(
            JWEHeader header,
            Base64URL encryptedKey,
            Base64URL iv,
            Base64URL cipherText,
            Base64URL authTag,
            byte[] aad)
            throws JOSEException {

        byte[] encryptedBytes = encryptedKey.decode();

        DecryptRequest decryptRequest =
                DecryptRequest.builder()
                        .keyId(keyId)
                        .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_1)
                        .ciphertextBlob(SdkBytes.fromByteArray(encryptedBytes))
                        .build();
        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
        byte[] decryptedBytes = decryptResponse.plaintext().asByteArray();

        // Step 2: Convert the decrypted CEK to a SecretKey object
        SecretKey cek = new SecretKeySpec(decryptedBytes, "AES");

        JWEJCAContext jwejcaContext = new JWEJCAContext();

        return AESCBC.decryptAuthenticated(
                cek,
                iv.decode(),
                cipherText.decode(),
                aad,
                authTag.decode(),
                jwejcaContext.getProvider(),
                jwejcaContext.getMACProvider());
    }

    @Override
    public Set<JWEAlgorithm> supportedJWEAlgorithms() {
        return new HashSet<>();
    }

    @Override
    public Set<EncryptionMethod> supportedEncryptionMethods() {
        return new HashSet<>();
    }

    @Override
    public JWEJCAContext getJCAContext() {
        return null;
    }
}
