package uk.gov.di.ipv.cri.drivingpermit.certreminder.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DVACloseableHttpClientFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.KeyCertHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class CertExpiryReminderConfig {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, X509Certificate> certMap = new HashMap<>();

    @ExcludeFromGeneratedCoverageReport
    private CertExpiryReminderConfig() {
        /* intended private */
    }

    public CertExpiryReminderConfig(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        readDVAHTTPClientCerts(parameterStoreService);

        readDVACryptoCerts(parameterStoreService);

        readDvaHeldCerts(parameterStoreService);
    }

    private void readDVAHTTPClientCerts(ParameterStoreService parameterStoreService)
            throws CertificateException {

        LOGGER.info("Reading DVA HTTP Client Certs");

        Map<String, String> dvaHtpClientCertsKeysMap =
                parameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE,
                        DVACloseableHttpClientFactory.HTTP_CLIENT_PARAMETER_PATH);

        // TLS CERT
        X509Certificate tlsCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaHtpClientCertsKeysMap.get(
                                DVACloseableHttpClientFactory.MAP_KEY_TLS_CERT));
        certMap.put(DVACloseableHttpClientFactory.MAP_KEY_TLS_CERT, tlsCertExpiry);

        X509Certificate tlsRootCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaHtpClientCertsKeysMap.get(
                                DVACloseableHttpClientFactory.MAP_KEY_TLS_ROOT_CERT));
        certMap.put(DVACloseableHttpClientFactory.MAP_KEY_TLS_ROOT_CERT, tlsRootCertExpiry);

        // INTER CERT
        X509Certificate tlsIntermediateCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(
                        dvaHtpClientCertsKeysMap.get(
                                DVACloseableHttpClientFactory.MAP_KEY_TLS_INT_CERT));
        certMap.put(DVACloseableHttpClientFactory.MAP_KEY_TLS_INT_CERT, tlsIntermediateCertExpiry);
    }

    private void readDVACryptoCerts(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        LOGGER.info("Reading DVA Crypto Certs");
        DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration =
                new DvaCryptographyServiceConfiguration(parameterStoreService);

        X509Certificate signingCertExpiry =
                (X509Certificate) dvaCryptographyServiceConfiguration.getSigningCert();
        certMap.put(
                DvaCryptographyServiceConfiguration
                        .MAP_KEY_SIGNING_CERT_FOR_DRIVING_PERMIT_TO_VERIFY,
                signingCertExpiry);

        X509Certificate encryptionCertExpiry =
                (X509Certificate) dvaCryptographyServiceConfiguration.getEncryptionCert();
        certMap.put(
                DvaCryptographyServiceConfiguration
                        .MAP_KEY_ENCRYPTION_CERT_FOR_DRIVING_PERMIT_TO_ENCRYPT,
                encryptionCertExpiry);

        X509Certificate signingThumbprintCertExpiry =
                (X509Certificate) dvaCryptographyServiceConfiguration.getSigningThumbprintCert();
        certMap.put("signingThumbprintCert", signingThumbprintCertExpiry);
    }

    private void readDvaHeldCerts(ParameterStoreService parameterStoreService)
            throws CertificateException {

        LOGGER.info("Reading DVA Held Certs");
        Map<String, String> dvaHeldCertMap =
                parameterStoreService.getAllParametersFromPathWithDecryption(
                        ParameterPrefix.OVERRIDE, "DVA/heldByDVA");

        X509Certificate dvaHeldTlsRootCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(dvaHeldCertMap.get("tlsRootCert"));
        certMap.put("dvaHeldTlsRootCert", dvaHeldTlsRootCertExpiry);

        X509Certificate dvaHeldSigningCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(dvaHeldCertMap.get("dvaSigningCert"));
        certMap.put("dvaHeldSigningCert", dvaHeldSigningCertExpiry);

        X509Certificate dvaHeldEncryptionCertExpiry =
                KeyCertHelper.getDecodedX509Certificate(dvaHeldCertMap.get("dvaEncryptionCert"));
        certMap.put("dvaHeldEncryptionCert", dvaHeldEncryptionCertExpiry);
    }

    public Map<String, X509Certificate> getDVACertificates() {
        return certMap;
    }
}
