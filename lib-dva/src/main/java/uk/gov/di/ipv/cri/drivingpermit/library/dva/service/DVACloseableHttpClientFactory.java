package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.HttpClientException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ClientFactoryService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class DVACloseableHttpClientFactory {
    public static final String HTTP_CLIENT_PARAMETER_PATH = "DVA/HttpClient";

    public static final String MAP_KEY_TLS_CERT = "tlsCert";
    public static final String MAP_KEY_TLS_KEY = "tlsKey";
    public static final String MAP_KEY_TLS_ROOT_CERT = "tlsRootCertificate-2023-11-13";
    public static final String MAP_KEY_TLS_INT_CERT = "tlsIntermediateCertificate-2023-11-13";

    public DVACloseableHttpClientFactory() {
        /* Intended */
    }

    public CloseableHttpClient getClient(
            ParameterStoreService parameterStoreService,
            ClientFactoryService clientFactoryService,
            boolean tlsOn) {

        try {
            if (tlsOn) {
                Map<String, String> dvaHtpClientCertsKeysMap =
                        parameterStoreService.getAllParametersFromPathWithDecryption(
                                HTTP_CLIENT_PARAMETER_PATH);

                final String base64TLSCertString = dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_CERT);

                final String base64TLSKeyString = dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_KEY);

                final String base64TLSRootCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_ROOT_CERT);

                final String base64TLSIntCertString =
                        dvaHtpClientCertsKeysMap.get(MAP_KEY_TLS_INT_CERT);

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
