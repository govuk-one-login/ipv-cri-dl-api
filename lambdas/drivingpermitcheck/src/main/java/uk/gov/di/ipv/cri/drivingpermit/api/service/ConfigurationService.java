package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SSMProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.Thumbprints;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger();

    static class KeyStoreParams {
        private String keyStore;
        private String keyStorePassword;

        public String getKeyStore() {
            return keyStore;
        }

        public void setKeyStore(String keyStore) {
            this.keyStore = keyStore;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }
    }

    private static final String KEY_FORMAT = "/%s/credentialIssuers/driving-permit/%s";

    private final String thirdPartyId;
    private final String documentCheckResultTableName;
    private final String contraindicationMappings;
    private final String dcsEndpointUri;
    private final String parameterPrefix;
    private final String oneLoginEnabled;
    private final Certificate dcsSigningCert;
    private final Certificate dcsEncryptionCert;
    private final Certificate drivingPermitTlsSelfCert;
    private final Certificate dcsTlsRootCert;
    private final Certificate dcsIntermediateCert;

    private final PrivateKey drivingPermitEncryptionKey;
    private final PrivateKey drivingPermitCriSigningKey;
    private final PrivateKey drivingPermitTlsKey;

    private final Thumbprints signingCertThumbprints;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");

        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        // ****************************Private Parameters****************************
        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.thirdPartyId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyId"));
        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.dcsEndpointUri = paramProvider.get(getParameterName("dcsEndpoint"));
        this.documentCheckResultTableName =
                paramProvider.get(getParameterName("DocumentCheckResultTableName"));

        this.dcsSigningCert = getCertificate(paramProvider, "signingCertForDrivingPermitToVerify");
        this.dcsEncryptionCert =
                getCertificate(paramProvider, "encryptionCertForDrivingPermitToEncrypt");

        this.drivingPermitTlsSelfCert = getCertificate(paramProvider, "tlsCert");

        this.dcsTlsRootCert = getCertificate(paramProvider, "tlsRootCertificate");

        this.dcsIntermediateCert = getCertificate(paramProvider, "tlsIntermediateCertificate");

        this.drivingPermitTlsKey = getPrivateKey(paramProvider, "tlsKey");

        this.drivingPermitEncryptionKey =
                getPrivateKey(paramProvider, "encryptionKeyForDrivingPermitToDecrypt");
        this.drivingPermitCriSigningKey =
                getPrivateKey(paramProvider, "signingKeyForDrivingPermitToSign");

        var cert = getCertificate(paramProvider, "signingCertForDcsToVerify");
        this.signingCertThumbprints =
                new Thumbprints(
                        getThumbprint((X509Certificate) cert, "SHA-1"),
                        getThumbprint((X509Certificate) cert, "SHA-256"));
        // *****************************Feature Toggles*******************************

        String oneLoginEnabledFlag;
        try {
            // This value will be set in the template, this is only as a fail safe.
            oneLoginEnabledFlag = paramProvider.get(getParameterName("oneLoginEnabled"));
        } catch (Exception e) {
            LOGGER.info("Failed to get oneLoginEnabled parameter in store, defaulting to false");
            oneLoginEnabledFlag = "false";
        }
        this.oneLoginEnabled = oneLoginEnabledFlag;

        // *********************************Secrets***********************************

    }

    private PrivateKey getPrivateKey(ParamProvider paramProvider, String parameterName)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SSMProvider ssmProvider = (SSMProvider) paramProvider;

        byte[] binaryKey =
                Base64.getDecoder()
                        .decode(ssmProvider.withDecryption().get(getParameterName(parameterName)));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binaryKey);
        return factory.generatePrivate(privateKeySpec);
    }

    private Certificate getCertificate(ParamProvider paramProvider, String parameterName)
            throws CertificateException {
        SSMProvider ssmProvider = (SSMProvider) paramProvider;
        byte[] binaryCertificate =
                Base64.getDecoder()
                        .decode(ssmProvider.withDecryption().get(getParameterName(parameterName)));
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(new ByteArrayInputStream(binaryCertificate));
    }

    public String getThumbprint(X509Certificate cert, String hashAlg)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance(hashAlg);
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public String getDocumentCheckResultTableName() {
        return documentCheckResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }

    public Certificate getDcsSigningCert() {
        return dcsSigningCert;
    }

    public PrivateKey getDrivingPermitEncryptionKey() {
        return drivingPermitEncryptionKey;
    }

    public Thumbprints getSigningCertThumbprints() {
        return signingCertThumbprints;
    }

    public PrivateKey getDrivingPermitCriSigningKey() {
        return drivingPermitCriSigningKey;
    }

    public Certificate getDcsEncryptionCert() {
        return dcsEncryptionCert;
    }

    public String getDcsEndpointUri() {
        return dcsEndpointUri;
    }

    public Certificate getDrivingPermitTlsSelfCert() {
        return drivingPermitTlsSelfCert;
    }

    public Certificate getDcsTlsRootCert() {
        return dcsTlsRootCert;
    }

    public Certificate getDcsIntermediateCert() {
        return dcsIntermediateCert;
    }

    public PrivateKey getDrivingPermitTlsKey() {
        return drivingPermitTlsKey;
    }
}
