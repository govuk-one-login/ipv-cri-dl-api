package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.apache.http.impl.client.CloseableHttpClient;
import software.amazon.awssdk.services.acm.model.ExportCertificateResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.HttpClientException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ClientFactoryService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService.parseAcmCertificate;
import static uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService.parseAcmKey;

public class DVACloseableHttpClientFactory {
    public static final String HTTP_CLIENT_PARAMETER_PATH = "DVA/HttpClient";

    public static final String MAP_KEY_TLS_CERT = "tlsCert";
    public static final String MAP_KEY_TLS_KEY = "tlsKey";
    public static final String MAP_KEY_TLS_ROOT_CERT = "tlsRootCertificate-2023-11-13";
    public static final String MAP_KEY_TLS_INT_CERT = "tlsIntermediateCertificate-2023-11-13";
    public static final String SELF_SIGNED_ROOT_CERT = System.getenv("SELF_SIGNED_ROOT_CERT");

    public DVACloseableHttpClientFactory() {
        /* Intended */
    }

    public CloseableHttpClient getClient(
            ParameterStoreService parameterStoreService,
            ClientFactoryService clientFactoryService,
            AcmCertificateService acmCertificateService,
            boolean tlsOn) {

        try {
            if (tlsOn) {
                Map<String, String> dvaHtpClientCertsKeysMap = new HashMap<>();
                // Comes from DVA
                dvaHtpClientCertsKeysMap.put(MAP_KEY_TLS_ROOT_CERT, SELF_SIGNED_ROOT_CERT);
                dvaHtpClientCertsKeysMap.put(MAP_KEY_TLS_INT_CERT, "");

                ExportCertificateResponse getCertificateResponse =
                        acmCertificateService.exportAcmCertificates();

                String certificate = getCertificateResponse.certificate();
                String certificateKey = getCertificateResponse.privateKey();

                final String base64TLSRootCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_ROOT_CERT);

                final String base64TLSIntCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_INT_CERT);

                String base64TLSCertString = parseAcmCertificate(certificate);
                String base64TLSKeyString = parseAcmKey(certificateKey);

                return clientFactoryService.generateHTTPClientFromExternalApacheHttpClient(
                        base64TLSCertString,
                        base64TLSKeyString,
                        base64TLSRootCertString,
                        base64TLSIntCertString);
            } else {
                return new ClientFactoryService().generatePublicHttpClient();
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
