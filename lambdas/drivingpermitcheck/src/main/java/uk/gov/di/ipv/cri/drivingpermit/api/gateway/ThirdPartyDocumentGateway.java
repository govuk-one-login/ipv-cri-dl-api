package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.oauth2.sdk.util.StringUtils;
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
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        LOGGER.info("Mapping person to third party Fraud request");

        DcsPayload dcsPayload = objectMapper.convertValue(drivingPermitData, DcsPayload.class);
        JWSObject preparedDcsPayload = preparePayload(dcsPayload);

        String requestBody = objectMapper.writeValueAsString(preparedDcsPayload);
        URI endpoint = URI.create(configurationService.getDcsEndpointUri());
        HttpRequest request = requestBuilder(endpoint, requestBody);

        LOGGER.info("Submitting fraud check request to third party...");
        HttpResponse<String> httpResponse = httpRetryer.sendHTTPRequestRetryIfAllowed(request);

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

    private DocumentCheckResult responseHandler(HttpResponse<String> httpResponse)
            throws JsonProcessingException, ParseException, JOSEException,
                    OAuthHttpResponseExceptionWithErrorBody, CertificateException {
        int statusCode = httpResponse.statusCode();
        LOGGER.info("Third party response code {}", statusCode);

        if (statusCode == 200) {
            String responseBody = httpResponse.body();
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

    private HttpRequest requestBuilder(URI endpointUri, String requestBody) {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(endpointUri)
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/jose")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
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
