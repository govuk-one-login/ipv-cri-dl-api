package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.SleepHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Objects;

public class ThirdPartyDocumentGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ObjectMapper objectMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final ConfigurationService configurationService;
    private final HttpRetryer httpRetryer;

    public static final String HTTP_300_REDIRECT_MESSAGE =
            "Redirection Message returned from Document Check Response, Status Code - ";
    public static final String HTTP_400_CLIENT_REQUEST_ERROR =
            "Client Request Error returned from Document Check Response, Status Code - ";
    public static final String HTTP_500_SERVER_ERROR =
            "Server Error returned from Document Check Response, Status Code - ";

    public static final String HTTP_UNHANDLED_ERROR =
            "Unhandled HTTP Response from Document Check Response, Status Code - ";

    public ThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DcsCryptographyService dcsCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(dcsCryptographyService, "dcsCryptographyService must not be null");
        Objects.requireNonNull(configurationService, "configurationService must not be null");
        Objects.requireNonNull(httpRetryer, "httpRetryer must not be null");

        this.objectMapper = objectMapper;
        this.dcsCryptographyService = dcsCryptographyService;
        this.configurationService = configurationService;
        this.httpRetryer = httpRetryer;
    }

    public ThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            String endpointUrl,
            SleepHelper sleepHelper,
            DcsCryptographyService dcsCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(dcsCryptographyService, "dcsCryptographyService must not be null");
        Objects.requireNonNull(configurationService, "configurationService must not be null");
        Objects.requireNonNull(httpRetryer, "httpRetryer must not be null");
        if (StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("endpointUrl must be specified");
        }
        Objects.requireNonNull(sleepHelper, "sleepHelper must not be null");
        this.objectMapper = objectMapper;
        this.dcsCryptographyService = dcsCryptographyService;
        this.configurationService = configurationService;
        this.httpRetryer = httpRetryer;
    }

    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitData)
            throws IOException, InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException {
        LOGGER.info("Mapping person to third party document check request");

        DcsPayload dcsPayload = objectMapper.convertValue(drivingPermitData, DcsPayload.class);

        String licenceIssuer = drivingPermitData.getLicenceIssuer();

        if ("DVA".equals(licenceIssuer)) {
            dcsPayload.setDateOfIssue(drivingPermitData.getDateOfIssue());
            dcsPayload.setDriverNumber(drivingPermitData.getDrivingLicenceNumber());
        } else {
            dcsPayload.setIssueDate(drivingPermitData.getIssueDate());
            dcsPayload.setIssueNumber(drivingPermitData.getIssueNumber());
            dcsPayload.setLicenceNumber(drivingPermitData.getDrivingLicenceNumber());
        }
        LOGGER.info("dcsPayload " + objectMapper.writeValueAsString(dcsPayload));

        JWSObject preparedDcsPayload = preparePayload(dcsPayload);

        String requestBody = preparedDcsPayload.serialize();
        LOGGER.info("JOSE String " + requestBody);
        URI endpoint = URI.create(configurationService.getDcsEndpointUri());
        HttpPost request = requestBuilder(endpoint, requestBody);

        LOGGER.info("Submitting document check request to third party...");
        CloseableHttpResponse httpResponse = httpRetryer.sendHTTPRequestRetryIfAllowed(request);

        DocumentCheckResult documentCheckResult = responseHandler(httpResponse);

        return documentCheckResult;
    }

    private JWSObject preparePayload(DcsPayload dcsPayload)
            throws OAuthHttpResponseExceptionWithErrorBody {
        LOGGER.info("Preparing payload for DCS");
        try {
            return dcsCryptographyService.preparePayload(dcsPayload);
        } catch (CertificateException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | JOSEException
                | JsonProcessingException e) {
            LOGGER.error(("Failed to prepare payload for DCS: " + e.getMessage()));
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_DCS_PAYLOAD);
        }
    }

    private void validateDcsResponse(DcsResponse dcsResponse)
            throws OAuthHttpResponseExceptionWithErrorBody {
        if (dcsResponse.isError()) {
            String errorMessage = dcsResponse.getErrorMessage().toString();
            LOGGER.error("DCS encountered an error: {}", errorMessage);
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DCS_RETURNED_AN_ERROR);
        }
    }

    private DocumentCheckResult responseHandler(CloseableHttpResponse httpResponse)
            throws IOException, ParseException, JOSEException,
                    OAuthHttpResponseExceptionWithErrorBody, CertificateException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info("Third party response code {}", statusCode);

        HttpEntity entity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);
        LOGGER.info("Third party response {}", responseBody);

        if (statusCode == 200) {
            // HttpEntity entity = httpResponse.getEntity();
            // String responseBody = EntityUtils.toString(entity);

            DcsResponse unwrappedDcsResponse =
                    dcsCryptographyService.unwrapDcsResponse(responseBody);
            validateDcsResponse(unwrappedDcsResponse);

            DocumentCheckResult documentCheckResult = new DocumentCheckResult();
            documentCheckResult.setExecutedSuccessfully(true);
            documentCheckResult.setTransactionId(unwrappedDcsResponse.getRequestId());
            documentCheckResult.setValid(unwrappedDcsResponse.isValid());

            return documentCheckResult;
        } else {
            DocumentCheckResult documentCheckResult = new DocumentCheckResult();
            documentCheckResult.setExecutedSuccessfully(false);

            if (statusCode >= 300 && statusCode <= 399) {
                documentCheckResult.setErrorMessage(HTTP_300_REDIRECT_MESSAGE + statusCode);
            } else if (statusCode >= 400 && statusCode <= 499) {
                documentCheckResult.setErrorMessage(HTTP_400_CLIENT_REQUEST_ERROR + statusCode);
            } else if (statusCode >= 500 && statusCode <= 599) {
                documentCheckResult.setErrorMessage(HTTP_500_SERVER_ERROR + statusCode);
            } else {
                documentCheckResult.setErrorMessage(HTTP_UNHANDLED_ERROR + statusCode);
            }

            return documentCheckResult;
        }
    }

    private HttpPost requestBuilder(URI endpointUri, String requestBody)
            throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/jose");

        request.setEntity(new StringEntity(requestBody));

        return request;
    }

    private boolean isInvalidSignature(JWSObject jwsObject) throws JOSEException {
        RSASSAVerifier rsassaVerifier =
                new RSASSAVerifier(
                        (RSAPublicKey) configurationService.getDcsSigningCert().getPublicKey());
        return !jwsObject.verify(rsassaVerifier);
    }

    public JWSObject decrypt(JWEObject encrypted) {
        try {
            RSADecrypter rsaDecrypter =
                    new RSADecrypter(configurationService.getDrivingPermitEncryptionKey());
            encrypted.decrypt(rsaDecrypter);

            return JWSObject.parse(encrypted.getPayload().toString());
        } catch (ParseException | JOSEException exception) {
            throw new IpvCryptoException(
                    String.format("Cannot Decrypt DCS Payload: %s", exception.getMessage()));
        }
    }
}
