package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

public class CryptoUtils {

    public static X509Certificate loadCertificate(String certificatePath) throws Exception {
        try (InputStream inStream = new FileInputStream(certificatePath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        }
    }

    public static PublicKey extractPublicKey(X509Certificate certificate) {
        return certificate.getPublicKey();
    }

    public static String encryptData(String data, PublicKey publicKey) throws Exception {
        AlgorithmParameters parameters =
                AlgorithmParameters.getInstance("OAEP", new BouncyCastleProvider());
        AlgorithmParameterSpec specification =
                new OAEPParameterSpec(
                        "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        parameters.init(specification);
        Cipher cipher =
                Cipher.getInstance(
                        "RSA/ECB/OAEPWithSHA-256AndMGF1Padding", new BouncyCastleProvider());

        cipher.init(Cipher.ENCRYPT_MODE, publicKey, parameters);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
