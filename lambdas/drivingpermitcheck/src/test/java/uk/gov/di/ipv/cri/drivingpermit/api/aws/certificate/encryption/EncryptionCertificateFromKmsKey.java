package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.encryption;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EncryptionCertificateFromKmsKey {


    public static X509Certificate certificateFromKmsKey(String keyId) throws Exception {

        // Create a self-signed X.509 certificate
        X509Certificate cert = generateSelfSignedCertificate(keyId,"CN=Driving Licence CRI JSON Encryption Dev 25-06-2024");

        return cert;
    }

    public static X509Certificate generateSelfSignedCertificate(String keyId, String subjectDN) throws IOException, GeneralSecurityException {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1);

        Date endDate = calendar.getTime();

        PublicKey publicKey = null;
        EncryptionAlgorithmSpec encryptionAlgorithmSpec = null;
        try (   KmsClient kmsClient = KmsClient.create() ) {
            GetPublicKeyResponse response = kmsClient.getPublicKey(GetPublicKeyRequest.builder().keyId(keyId).build());
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(response.publicKey().asByteArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            publicKey = converter.getPublicKey(spki);
            List<EncryptionAlgorithmSpec> encryptionAlgorithms = response.encryptionAlgorithms();
            if (encryptionAlgorithms != null && !encryptionAlgorithms.isEmpty())
                encryptionAlgorithmSpec = encryptionAlgorithms.get(0);
        }
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, publicKey);

        AwsKmsContentEncryptor contentEncryptor = new AwsKmsContentEncryptor(keyId, encryptionAlgorithmSpec);

        BasicConstraints basicConstraints = new BasicConstraints(true);
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(contentEncryptor));
    }
}