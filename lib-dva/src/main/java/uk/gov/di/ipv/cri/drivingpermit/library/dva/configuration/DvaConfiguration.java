package uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DRIVING_PERMIT_DVA_ENCRYPTION_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DRIVING_PERMIT_DVA_INTERMEDIATE_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DRIVING_PERMIT_DVA_ROOT_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DRIVING_PERMIT_DVA_SIGNING_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_DRIVING_PERMIT_CRI_SIGNING_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_ENCRYPTION_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_HTTPCLIENT_TLS_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_HTTPCLIENT_TLS_INTER_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_HTTPCLIENT_TLS_ROOT_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_SIGNING_CERT_THUMB;

@ExcludeFromGeneratedCoverageReport
public class DvaConfiguration {

    private final String endpointUri;
    private final String userName;
    private final String password;

    private final Certificate tlsSelfCert;
    private final PrivateKey tlsKey;

    private final Certificate tlsIntermediateCert;
    private final Certificate tlsRootCert;
    // JWS SHA-1 Certificate Thumbprint (Header)
    private final Thumbprints signingCertThumbprints;

    private final Thumbprints encryptionCertThumbprints;

    // DVA JWE (Private Key Reply Decrypt)
    private final PrivateKey encryptionKey;

    // JWE (Public Key)
    private final Certificate encryptionCert;

    // JWS RSA Signing Key
    private final PrivateKey signingKey;

    // JWS (Reply Signature)
    private final Certificate signingCert;

    // cert used in thumbprint generation
    private final Certificate signingThumbprintCert;

    // DVA HELD
    // Used by DVA to sign responses
    private final Certificate dvaHeldSigningCert;

    // Used by DVA to Encrypt responses
    private final Certificate dvaHeldEncryptionCert;

    // DL CRI root Certificate used by DVA to verify issuer
    private final Certificate dvaHeldTlsRootCert;

    // DL CRI intermediate certificate used by DVA to verify issuer
    private final Certificate dvaHeldTlsIntermediateCert;

    public DvaConfiguration(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        this.endpointUri =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_ENDPOINT);
        this.userName =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_USERNAME);
        this.password =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_PASSWORD);

        // TLS
        this.tlsSelfCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_HTTPCLIENT_TLS_CERT));
        this.tlsKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(
                                ParameterStoreParameters.DVA_HTTPCLIENT_TLS_KEY));
        this.tlsRootCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_HTTPCLIENT_TLS_ROOT_CERT));
        this.tlsIntermediateCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_HTTPCLIENT_TLS_INTER_CERT));

        // JWS SHA-1 Certificate Thumbprint (Header)
        this.signingThumbprintCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_SIGNING_CERT_THUMB));
        this.signingCertThumbprints =
                KeyCertHelper.makeThumbprint((X509Certificate) signingThumbprintCert);

        Certificate encryptionX509Cert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_ENCRYPTION_CERT));
        this.encryptionCertThumbprints =
                KeyCertHelper.makeThumbprint((X509Certificate) encryptionX509Cert);

        // JWS RSA Signing Key
        this.signingKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(
                                ParameterStoreParameters.DVA_DRIVING_PERMIT_CRI_SIGNING_KEY));

        // JWE (Public Key)
        this.encryptionCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_ENCRYPTION_CERT));

        // JWS (Reply Signature)
        this.signingCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DVA_DRIVING_PERMIT_CRI_SIGNING_CERT));

        // JWE (Private Key Reply Decrypt)
        this.encryptionKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(
                                ParameterStoreParameters.DVA_DRIVING_PERMIT_ENCRYPTION_KEY));

        // *************** DVA HELD USED ONLY FOR CERTEXPIRY LAMBDA**********************

        this.dvaHeldTlsRootCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DRIVING_PERMIT_DVA_ROOT_CERT));

        this.dvaHeldTlsIntermediateCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DRIVING_PERMIT_DVA_INTERMEDIATE_CERT));

        this.dvaHeldSigningCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DRIVING_PERMIT_DVA_SIGNING_CERT));

        this.dvaHeldEncryptionCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                ParameterStoreParameters.DRIVING_PERMIT_DVA_ENCRYPTION_CERT));
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Certificate getTlsSelfCert() {
        return tlsSelfCert;
    }

    public PrivateKey getTlsKey() {
        return tlsKey;
    }

    public Certificate getTlsRootCert() {
        return tlsRootCert;
    }

    public Certificate getTlsIntermediateCert() {
        return tlsIntermediateCert;
    }

    public Thumbprints getSigningCertThumbprints() {
        return signingCertThumbprints;
    }

    public PrivateKey getSigningKey() {
        return signingKey;
    }

    public Certificate getEncryptionCert() {
        return encryptionCert;
    }

    public Certificate getSigningCert() {
        return signingCert;
    }

    public Certificate getSigningThumbprintCert() {
        return signingThumbprintCert;
    }

    public PrivateKey getEncryptionKey() {
        return encryptionKey;
    }

    public Thumbprints getEncryptionCertThumbprints() {
        return encryptionCertThumbprints;
    }

    public Certificate getDvaHeldSigningCert() {
        return dvaHeldSigningCert;
    }

    public Certificate getDvaHeldEncryptionCert() {
        return dvaHeldEncryptionCert;
    }

    public Certificate getDvaHeldTlsRootCert() {
        return dvaHeldTlsRootCert;
    }

    public Certificate getDvaHeldTlsIntermediateCert() {
        return dvaHeldTlsIntermediateCert;
    }

    public Map<String, X509Certificate> getDVACertificates() {
        X509Certificate signingCertExpiry = (X509Certificate) getSigningCert();
        X509Certificate encryptionCertExpiry = (X509Certificate) getEncryptionCert();
        X509Certificate tlsRootCertExpiry = (X509Certificate) getTlsRootCert();
        X509Certificate tlsIntermediateCertExpiry = (X509Certificate) getTlsIntermediateCert();
        X509Certificate tlsCertExpiry = (X509Certificate) getTlsSelfCert();
        X509Certificate signingThumbprintCertExpiry = (X509Certificate) getSigningThumbprintCert();

        // DVA Held
        X509Certificate dvaHeldTlsRootCertExpiry = (X509Certificate) getDvaHeldTlsRootCert();
        X509Certificate dvaHeldTlsIntermediateCertExpiry =
                (X509Certificate) getDvaHeldTlsIntermediateCert();
        X509Certificate dvaHeldSigningCertExpiry = (X509Certificate) getDvaHeldSigningCert();
        X509Certificate dvaHeldEncryptionCertExpiry = (X509Certificate) getDvaHeldEncryptionCert();

        return Map.of(
                DVA_DRIVING_PERMIT_CRI_SIGNING_CERT,
                signingCertExpiry,
                DVA_SIGNING_CERT_THUMB,
                signingThumbprintCertExpiry,
                DVA_ENCRYPTION_CERT,
                encryptionCertExpiry,
                DVA_HTTPCLIENT_TLS_ROOT_CERT,
                tlsRootCertExpiry,
                DVA_HTTPCLIENT_TLS_INTER_CERT,
                tlsIntermediateCertExpiry,
                DVA_HTTPCLIENT_TLS_CERT,
                tlsCertExpiry,
                DRIVING_PERMIT_DVA_ROOT_CERT,
                dvaHeldTlsRootCertExpiry,
                DRIVING_PERMIT_DVA_INTERMEDIATE_CERT,
                dvaHeldTlsIntermediateCertExpiry,
                DRIVING_PERMIT_DVA_SIGNING_CERT,
                dvaHeldSigningCertExpiry,
                DRIVING_PERMIT_DVA_ENCRYPTION_CERT,
                dvaHeldEncryptionCertExpiry);
    }
}
