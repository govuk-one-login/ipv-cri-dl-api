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
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.SleepHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermit;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.LocalDate;
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

    private static final String OPENID_CHECK_METHOD_IDENTIFIER = "data";
    private static final String IDENTITY_CHECK_POLICY = "published";
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

        IssuingAuthority licenceIssuer;
        try {
            licenceIssuer = IssuingAuthority.valueOf(drivingPermitData.getLicenceIssuer());
            LOGGER.info("Document Issuer {}", licenceIssuer);
        } catch (IllegalArgumentException e) {
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }
        LocalDate drivingPermitExpiryDate = drivingPermitData.getExpiryDate();
        String drivingPermitDocumentNumber = drivingPermitData.getDrivingLicenceNumber();

        LocalDate documentIssueDate = null;
        String dcsEndpointUri = null;
        switch (licenceIssuer) {
            case DVA:
                documentIssueDate = drivingPermitData.getDateOfIssue();
                dcsEndpointUri = configurationService.getDcsEndpointUri() + "/dva-driving-licence";

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setDriverNumber(drivingPermitDocumentNumber);
                dcsPayload.setDateOfIssue(documentIssueDate);
                break;
            case DVLA:
                dcsEndpointUri = configurationService.getDcsEndpointUri() + "/driving-licence";
                documentIssueDate = drivingPermitData.getIssueDate();

                dcsPayload.setIssueNumber(drivingPermitData.getIssueNumber());

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setLicenceNumber(drivingPermitDocumentNumber);
                dcsPayload.setIssueDate(documentIssueDate);
                break;
            default:
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }

        JWSObject preparedDcsPayload = preparePayload(dcsPayload);

        String requestBody = preparedDcsPayload.serialize();
        LOGGER.info("JOSE String " + requestBody);
        URI endpoint = URI.create(dcsEndpointUri);
        HttpPost request = requestBuilder(endpoint, requestBody);

        LOGGER.info("Submitting document check request to third party...");
        CloseableHttpResponse httpResponse = httpRetryer.sendHTTPRequestRetryIfAllowed(request);

        DocumentCheckResult documentCheckResult = responseHandler(httpResponse);

        // Data capture for VC
        CheckDetails checkDetails = new CheckDetails();
        checkDetails.setCheckMethod(OPENID_CHECK_METHOD_IDENTIFIER);
        checkDetails.setIdentityCheckPolicy(IDENTITY_CHECK_POLICY);

        if (documentCheckResult.isValid()) {
            // Map ActivityFrom to documentIssueDate (IssueDate / DateOfIssue)
            checkDetails.setActivityFrom(documentIssueDate.toString());
        }
        documentCheckResult.setCheckDetails(checkDetails);

        DrivingPermit permit = new DrivingPermit();
        permit.setIssuedBy(licenceIssuer.toString());
        permit.setDocumentNumber(drivingPermitDocumentNumber);
        permit.setExpiryDate(drivingPermitExpiryDate.toString());
        documentCheckResult.setDrivingPermit(permit);

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
            throws IOException, ParseException, JOSEException, CertificateException,
                    OAuthHttpResponseExceptionWithErrorBody {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info("Third party response code {}", statusCode);

        HttpEntity entity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);

        if (statusCode == 200) {
            DcsResponse unwrappedDcsResponse =
                    dcsCryptographyService.unwrapDcsResponse(responseBody);
            validateDcsResponse(unwrappedDcsResponse);

            DocumentCheckResult documentCheckResult = new DocumentCheckResult();
            documentCheckResult.setExecutedSuccessfully(true);
            documentCheckResult.setTransactionId(unwrappedDcsResponse.getRequestId());
            documentCheckResult.setValid(unwrappedDcsResponse.isValid());

            LOGGER.info("Third party response successfully mapped");
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

            LOGGER.info("Third party response is fail with status code {}", statusCode);

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
