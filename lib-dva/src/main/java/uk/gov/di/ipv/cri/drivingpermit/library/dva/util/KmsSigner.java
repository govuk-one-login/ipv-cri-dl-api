package uk.gov.di.ipv.cri.drivingpermit.library.dva.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

@ExcludeFromGeneratedCoverageReport
public class KmsSigner implements JWSSigner {
    private final KmsClient kmsClient;
    private final String keyId;
    private final X509Certificate dlSigningCertificate;

    public KmsSigner(String keyId, X509Certificate dlSigningCertificate, KmsClient kmsClient) {
        this.kmsClient = kmsClient;
        this.keyId = keyId;
        this.dlSigningCertificate = dlSigningCertificate;
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

    public String getKeyId() {
        return keyId;
    }

    public X509Certificate getDlSigningCertificate() {
        return dlSigningCertificate;
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
