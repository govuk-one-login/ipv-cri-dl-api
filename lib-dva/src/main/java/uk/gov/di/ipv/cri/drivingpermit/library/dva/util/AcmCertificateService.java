package uk.gov.di.ipv.cri.drivingpermit.library.dva.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.model.ExportCertificateRequest;
import software.amazon.awssdk.services.acm.model.ExportCertificateResponse;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

@ExcludeFromGeneratedCoverageReport
public class AcmCertificateService {

    private AcmClient acmClient;

    public AcmCertificateService(AcmClient acmClient) {
        this.acmClient = acmClient;
    }

    public ExportCertificateResponse exportAcmTlsCertificates() {
        String tlsCertificateArn = System.getenv("TLS_CERTIFICATE_ARN");
        ExportCertificateResponse getCertificateResponse = exportAcmCertificate(tlsCertificateArn);
        return getCertificateResponse;
    }

    public String exportAcmSigningCertificate() {
        String signingCertificateArn = System.getenv("SIGNING_CERTIFICATE_ARN");
        ExportCertificateResponse getCertificateResponse =
                exportAcmCertificate(signingCertificateArn);
        return getCertificateResponse.certificate();
    }

    private ExportCertificateResponse exportAcmCertificate(String certificateArn) {
        Security.addProvider(new BouncyCastleProvider());

        // Create the DescribeCertificateRequest
        ExportCertificateRequest describeRequest =
                ExportCertificateRequest.builder()
                        .certificateArn(certificateArn)
                        .passphrase(SdkBytes.fromString("password", StandardCharsets.UTF_8))
                        .build();

        // Retrieve the certificate details
        ExportCertificateResponse getCertificateResponse =
                acmClient.exportCertificate(describeRequest);
        return getCertificateResponse;
    }

    public static String parseAcmCertificate(String acmCertificate) {
        Security.addProvider(new BouncyCastleProvider());
        String rawCertificate = acmCertificate.replaceAll("-----BEGIN CERTIFICATE-----", "");
        rawCertificate = rawCertificate.replaceAll("-----END CERTIFICATE-----", "");
        rawCertificate = rawCertificate.replaceAll("\n", "");
        return rawCertificate;
    }

    public static String parseAcmKey(String acmKey) {
        Security.addProvider(new BouncyCastleProvider());

        String rawKey;
        try {
            rawKey = Base64.toBase64String(readPrivateKey(acmKey, "password").getEncoded());
        } catch (IOException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | InvalidKeyException
                | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        return rawKey;
    }

    private static PrivateKey readPrivateKey(String privateKeyContent, String passPhrase)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
                    InvalidKeyException, NoSuchProviderException {
        try (StringReader stringReader = new StringReader(privateKeyContent);
                PEMParser pemParser = new PEMParser(stringReader)) {

            Object object = pemParser.readObject();

            if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
                // Decrypt the PKCS#8 encrypted private key using the passphrase
                PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo =
                        (PKCS8EncryptedPrivateKeyInfo) object;

                // Use the JcePKCSPBEInputDecryptorProviderBuilder to decrypt the private key
                JcePKCSPBEInputDecryptorProviderBuilder decryptorProviderBuilder =
                        new JcePKCSPBEInputDecryptorProviderBuilder();
                PrivateKey decryptedKey =
                        new JcaPEMKeyConverter()
                                .setProvider("BC")
                                .getPrivateKey(
                                        encryptedPrivateKeyInfo.decryptPrivateKeyInfo(
                                                decryptorProviderBuilder
                                                        .setProvider("BC")
                                                        .build(passPhrase.toCharArray())));

                return decryptedKey;
            } else {
                throw new IllegalArgumentException(
                        "The provided key is not in the expected encrypted PKCS#8 format.");
            }
        } catch (PKCSException e) {
            throw new RuntimeException(e);
        }
    }
}
