package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.apache.http.impl.client.CloseableHttpClient;
import software.amazon.awssdk.services.acm.model.ExportCertificateResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaCryptographyServiceConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.HttpClientException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ApacheHTTPClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService.parseAcmCertificate;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService.parseAcmKey;

public class DVACloseableHttpClientFactory {
    public static final String HTTP_CLIENT_PARAMETER_PATH = "DVA/HttpClient";

    public static final String MAP_KEY_TLS_CERT = "tlsCert-03-07-2024";
    public static final String MAP_KEY_TLS_KEY = "tlsKey-03-07-2024";
    public static final String MAP_KEY_TLS_ROOT_CERT = "tlsRootCertificate-2023-11-13";
    public static final String MAP_KEY_TLS_INT_CERT = "tlsIntermediateCertificate-2023-11-13";

    public DVACloseableHttpClientFactory() {
        /* Intended */
    }

    public CloseableHttpClient getClient(
            DvaCryptographyServiceConfiguration dvaCryptographyServiceConfiguration,
            ParameterStoreService parameterStoreService,
            ApacheHTTPClientFactoryService apacheHTTPClientFactoryService,
            AcmCertificateService acmCertificateService,
            boolean tlsOn) {

        try {
            if (tlsOn) {
                Map<String, String> dvaHtpClientCertsKeysMap =
                        parameterStoreService.getAllParametersFromPathWithDecryption(
                                ParameterPrefix.OVERRIDE, HTTP_CLIENT_PARAMETER_PATH);

                String base64TLSCertString = dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_CERT);
                String base64TLSKeyString = dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_KEY);

                // Comes from DVA
                final String base64TLSRootCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_ROOT_CERT);

                final String base64TLSIntCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_INT_CERT);

                String hasCA = dvaCryptographyServiceConfiguration.getHasCA();

                if (Boolean.parseBoolean(hasCA)) {
                    ExportCertificateResponse getCertificateResponse =
                            acmCertificateService.exportAcmTlsCertificates();

                    String certificate = getCertificateResponse.certificate();
                    String certificateKey = getCertificateResponse.privateKey();

                    base64TLSCertString = parseAcmCertificate(certificate);
                    base64TLSKeyString = parseAcmKey(certificateKey);
                }

                return apacheHTTPClientFactoryService
                        .generateHTTPClientFromExternalApacheHttpClient(
                                base64TLSCertString,
                                base64TLSKeyString,
                                base64TLSRootCertString,
                                base64TLSIntCertString);
            } else {
                return apacheHTTPClientFactoryService.generatePublicHttpClient();
            }
        } catch (NoSuchAlgorithmException
                | InvalidKeySpecException
                | CertificateException
                | KeyStoreException
                | IOException
                | UnrecoverableKeyException
                | KeyManagementException e) {
            throw new HttpClientException(e);
        }
    }
}
