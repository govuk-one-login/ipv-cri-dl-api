package uk.gov.di.ipv.cri.drivingpermit.library.helpers;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyCertHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    @ExcludeFromGeneratedCoverageReport
    private KeyCertHelper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static X509Certificate getDecodedX509Certificate(String base64String)
            throws CertificateException {
        byte[] binaryCertificate = Base64.getDecoder().decode(base64String);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate)
                factory.generateCertificate(new ByteArrayInputStream(binaryCertificate));
    }

    public static PrivateKey getDecodedPrivateRSAKey(String base64String)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] binaryKey = Base64.getDecoder().decode(base64String);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binaryKey);
        return factory.generatePrivate(privateKeySpec);
    }

    public static Thumbprints makeThumbprint(X509Certificate certificate)
            throws CertificateEncodingException, NoSuchAlgorithmException {
        LOGGER.info("THUMBPRINT FROM OUTGOING CERT = " + getCertThumbprint(certificate, "SHA-1") + " and " + getCertThumbprint(certificate, "SHA-256"));
        return new Thumbprints(
                getCertThumbprint(certificate, "SHA-1"), getCertThumbprint(certificate, "SHA-256"));
    }

    private static String getCertThumbprint(X509Certificate cert, String hashAlg)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance(hashAlg);
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
