package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
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
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.*;

public class ConfigurationService {

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
    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private final String thirdPartyId;
    private final String documentCheckResultTableName;
    private final String contraindicationMappings;
    private final String dcsEndpointUri;
    private final String parameterPrefix;
    private final String commonParameterPrefix;
    private final String dvaEndpointUri;
    private final boolean dvaDirectEnabled;
    private final Certificate dcsSigningCert;
    private final Certificate dcsEncryptionCert;
    private final Certificate drivingPermitTlsSelfCert;
    private final Certificate dcsTlsRootCert;
    private final Certificate dcsIntermediateCert;
    private final PrivateKey drivingPermitEncryptionKey;
    private final PrivateKey drivingPermitCriSigningKey;
    private final PrivateKey drivingPermitTlsKey;
    private final Certificate dvaEncryptionCert;
    private final Certificate dvaSigningCert;
    private final Certificate dvaTlsIntermediateCert;
    private final Certificate dvaTlsRootCert;
    private final Certificate dvaTlsSelfCert;
    private final PrivateKey dvaDrivingPermitEncryptionKey;
    private final PrivateKey dvaDrivingPermitCriSigningKey;
    private final PrivateKey dvaDrivingPermitTlsKey;
    private final Thumbprints signingCertThumbprints;

    private final Clock clock;

    private final long documentCheckItemTtl;
    private final boolean isPerformanceStub;
    private final boolean logDcsResponse;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");

        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }
        this.clock = Clock.systemUTC();

        // ****************************Private Parameters****************************
        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.commonParameterPrefix = System.getenv("COMMON_PARAMETER_NAME_PREFIX");
        this.thirdPartyId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyId"));
        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.dcsEndpointUri = paramProvider.get(getParameterName("dcsEndpoint"));
        this.documentCheckResultTableName =
                paramProvider.get(getParameterName("DocumentCheckResultTableName"));

        // ****************************DCS Parameters****************************

        this.dcsSigningCert = getCertificate(paramProvider, DCS_DRIVING_PERMIT_CRI_SIGNING_CERT);

        this.dcsEncryptionCert = getCertificate(paramProvider, DCS_ENCRYPTION_CERT);

        this.drivingPermitTlsSelfCert = getCertificate(paramProvider, DCS_HTTPCLIENT_TLS_CERT);

        this.dcsTlsRootCert = getCertificate(paramProvider, DCS_HTTPCLIENT_TLS_ROOT_CERT);

        this.dcsIntermediateCert = getCertificate(paramProvider, DCS_HTTPCLIENT_TLS_INTER_CERT);

        this.drivingPermitTlsKey = getPrivateKey(paramProvider, DCS_HTTPCLIENT_TLS_KEY);

        this.drivingPermitEncryptionKey =
                getPrivateKey(paramProvider, DCS_DRIVING_PERMIT_ENCRYPTION_KEY);

        this.drivingPermitCriSigningKey =
                getPrivateKey(paramProvider, DCS_DRIVING_PERMIT_CRI_SIGNING_KEY);

        var cert = getCertificate(paramProvider, DCS_SIGNING_CERT);

        // ****************************DVA Parameters****************************

        this.dvaSigningCert = getCertificate(paramProvider, DVA_DRIVING_PERMIT_CRI_SIGNING_CERT);

        this.dvaEncryptionCert = getCertificate(paramProvider, DVA_ENCRYPTION_CERT);

        this.dvaTlsSelfCert = getCertificate(paramProvider, DVA_HTTPCLIENT_TLS_CERT);

        this.dvaTlsRootCert = getCertificate(paramProvider, DVA_HTTPCLIENT_TLS_ROOT_CERT);

        this.dvaTlsIntermediateCert = getCertificate(paramProvider, DVA_HTTPCLIENT_TLS_INTER_CERT);

        this.dvaDrivingPermitTlsKey = getPrivateKey(paramProvider, DVA_HTTPCLIENT_TLS_KEY);

        this.dvaDrivingPermitEncryptionKey =
                getPrivateKey(paramProvider, DVA_DRIVING_PERMIT_ENCRYPTION_KEY);

        this.dvaDrivingPermitCriSigningKey =
                getPrivateKey(paramProvider, DVA_DRIVING_PERMIT_CRI_SIGNING_KEY);

        var dvaCert = getCertificate(paramProvider, DVA_SIGNING_CERT);

        this.signingCertThumbprints =
                new Thumbprints(
                        getThumbprint((X509Certificate) cert, "SHA-1"),
                        getThumbprint((X509Certificate) cert, "SHA-256"));
        this.documentCheckItemTtl =
                Long.parseLong(paramProvider.get(getCommonParameterName("SessionTtl")));

        this.dvaEndpointUri = paramProvider.get(getParameterName(DVA_ENDPOINT));

        // *****************************Feature Toggles*******************************
        this.dvaDirectEnabled =
                Boolean.parseBoolean(paramProvider.get(getParameterName("dvaDirectEnabled")));
        this.isPerformanceStub =
                Boolean.parseBoolean(paramProvider.get(getParameterName("isPerformanceStub")));
        this.logDcsResponse =
                Boolean.parseBoolean(paramProvider.get(getParameterName("logDcsResponse")));
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
        return String.format(PARAMETER_NAME_FORMAT, parameterPrefix, parameterName);
    }

    private String getCommonParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, commonParameterPrefix, parameterName);
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

    public Certificate getDvaEncryptionCert() {
        return dvaEncryptionCert;
    }

    public Certificate getDvaSigningCert() {
        return dvaSigningCert;
    }

    public PrivateKey getDvaDrivingPermitEncryptionKey() {
        return dvaDrivingPermitEncryptionKey;
    }

    public PrivateKey getDvaDrivingPermitCriSigningKey() {
        return dvaDrivingPermitCriSigningKey;
    }

    public PrivateKey getDvaDrivingPermitTlsKey() {
        return dvaDrivingPermitTlsKey;
    }

    public Certificate getDvaTlsIntermediateCert() {
        return dvaTlsIntermediateCert;
    }

    public Certificate getDvaTlsRootCert() {
        return dvaTlsRootCert;
    }

    public Certificate getDvaTlsSelfCert() {
        return dvaTlsSelfCert;
    }

    public boolean getDvaDirectEnabled() {
        return dvaDirectEnabled;
    }

    public long getDocumentCheckItemExpirationEpoch() {
        return clock.instant().plus(documentCheckItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }

    public boolean isPerformanceStub() {
        return isPerformanceStub;
    }

    public boolean isLogDcsResponse() {
        return logDcsResponse;
    }

    public String getDvaEndpointUri() {
        return dvaEndpointUri;
    }
}
