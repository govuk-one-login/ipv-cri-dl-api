package uk.gov.di.ipv.cri.drivingpermit.api.service.configuration;

import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Thumbprints;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_DRIVING_PERMIT_CRI_SIGNING_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_DRIVING_PERMIT_CRI_SIGNING_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_DRIVING_PERMIT_ENCRYPTION_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_ENCRYPTION_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_HTTPCLIENT_TLS_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_HTTPCLIENT_TLS_INTER_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_HTTPCLIENT_TLS_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_HTTPCLIENT_TLS_ROOT_CERT;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DCS_SIGNING_CERT_THUMB;

public class DcsConfiguration {

    private final String endpointUri;

    private final Certificate tlsSelfCert;
    private final PrivateKey tlsKey;

    private final Certificate tlsIntermediateCert;
    private final Certificate tlsRootCert;

    // JWS SHA-1 Certificate Thumbprint (Header)
    private final Thumbprints signingCertThumbprints;

    // DCS JWE (Private Key Reply Decrypt)
    private final PrivateKey encryptionKey;

    // JWE (Public Key)
    private final Certificate encryptionCert;

    // JWS RSA Signing Key
    private final PrivateKey signingKey;

    // JWS (Reply Signature)
    private final Certificate signingCert;

    public DcsConfiguration(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        this.endpointUri = parameterStoreService.getParameterValue(DCS_ENDPOINT);

        // TLS
        this.tlsSelfCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(DCS_HTTPCLIENT_TLS_CERT));
        this.tlsKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(DCS_HTTPCLIENT_TLS_KEY));
        this.tlsRootCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(DCS_HTTPCLIENT_TLS_ROOT_CERT));
        this.tlsIntermediateCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(DCS_HTTPCLIENT_TLS_INTER_CERT));

        // JWS SHA-1 Certificate Thumbprint (Header)
        Certificate cert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(DCS_SIGNING_CERT_THUMB));
        this.signingCertThumbprints = KeyCertHelper.makeThumbprint((X509Certificate) cert);

        // JWS RSA Signing Key
        this.signingKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(
                                DCS_DRIVING_PERMIT_CRI_SIGNING_KEY));

        // JWE (Public Key)
        this.encryptionCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(DCS_ENCRYPTION_CERT));

        // JWS (Reply Signature)
        this.signingCert =
                KeyCertHelper.getDecodedX509Certificate(
                        parameterStoreService.getParameterValue(
                                DCS_DRIVING_PERMIT_CRI_SIGNING_CERT));

        // DCS JWE (Private Key Reply Decrypt)
        this.encryptionKey =
                KeyCertHelper.getDecodedPrivateRSAKey(
                        parameterStoreService.getEncryptedParameterValue(
                                DCS_DRIVING_PERMIT_ENCRYPTION_KEY));
    }

    public String getEndpointUri() {
        return endpointUri;
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

    public PrivateKey getEncryptionKey() {
        return encryptionKey;
    }
}
