package uk.gov.di.ipv.cri.drivingpermit.api.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
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
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.ISSUING_AUTHORITY_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DCS_RESPONSE_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DCS_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_CREATED;

public class ThirdPartyDocumentGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ObjectMapper objectMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final ConfigurationService configurationService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;
    private static final String OPENID_CHECK_METHOD_IDENTIFIER = "data";
    private static final String IDENTITY_CHECK_POLICY = "published";

    public ThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DcsCryptographyService dcsCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(dcsCryptographyService, "dcsCryptographyService must not be null");
        Objects.requireNonNull(configurationService, "configurationService must not be null");
        Objects.requireNonNull(httpRetryer, "httpRetryer must not be null");

        this.objectMapper = objectMapper;
        this.dcsCryptographyService = dcsCryptographyService;
        this.configurationService = configurationService;
        this.httpRetryer = httpRetryer;
        this.eventProbe = eventProbe;
    }

    public ThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            String endpointUrl,
            SleepHelper sleepHelper,
            DcsCryptographyService dcsCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
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
        this.eventProbe = eventProbe;
    }

    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitData)
            throws IOException, InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException {
        LOGGER.info("Mapping person to third party document check request");

        DcsPayload dcsPayload = objectMapper.convertValue(drivingPermitData, DcsPayload.class);

        IssuingAuthority issuingAuthority;
        try {
            issuingAuthority = IssuingAuthority.valueOf(drivingPermitData.getLicenceIssuer());
            LOGGER.info("Document Issuer {}", issuingAuthority);
            eventProbe.counterMetric(
                    ISSUING_AUTHORITY_PREFIX + issuingAuthority.toString().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }
        LocalDate drivingPermitExpiryDate = drivingPermitData.getExpiryDate();
        String drivingPermitDocumentNumber = drivingPermitData.getDrivingLicenceNumber();
        LocalDate drivingPermitIssueDate = drivingPermitData.getIssueDate();

        String dcsEndpointUri = null;
        switch (issuingAuthority) {
            case DVA:
                dcsEndpointUri = configurationService.getDcsEndpointUri() + "/dva-driving-licence";

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setDriverNumber(drivingPermitDocumentNumber);

                // Note: DateOfIssue is mapped to issueDate in the front end to simplify
                // api handling of that field
                // Here (for the DVA request) it needs to be mapped back to date of issue
                dcsPayload.setDateOfIssue(drivingPermitIssueDate);
                break;
            case DVLA:
                dcsEndpointUri = configurationService.getDcsEndpointUri() + "/driving-licence";

                dcsPayload.setIssueNumber(drivingPermitData.getIssueNumber());

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setLicenceNumber(drivingPermitDocumentNumber);
                dcsPayload.setIssueDate(drivingPermitIssueDate);
                break;
            default:
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }

        JWSObject preparedDcsPayload = preparePayload(dcsPayload);

        String requestBody = preparedDcsPayload.serialize();

        URI endpoint = URI.create(dcsEndpointUri);
        HttpPost request = requestBuilder(endpoint, requestBody);

        eventProbe.counterMetric(THIRD_PARTY_REQUEST_CREATED);

        LOGGER.info("Submitting document check request to third party...");
        CloseableHttpResponse httpResponse = httpRetryer.sendHTTPRequestRetryIfAllowed(request);

        DocumentCheckResult documentCheckResult = responseHandler(httpResponse);

        if (documentCheckResult.isExecutedSuccessfully()) {
            // Data capture for VC
            CheckDetails checkDetails = new CheckDetails();
            checkDetails.setCheckMethod(OPENID_CHECK_METHOD_IDENTIFIER);
            checkDetails.setIdentityCheckPolicy(IDENTITY_CHECK_POLICY);

            if (documentCheckResult.isValid()) {
                // Map ActivityFrom to documentIssueDate (IssueDate / DateOfIssue)
                checkDetails.setActivityFrom(drivingPermitIssueDate.toString());
            }
            documentCheckResult.setCheckDetails(checkDetails);
        }

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

        HttpEntity entity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);

        if (statusCode == 200) {
            LOGGER.info("Third party response code {}", statusCode);

            try {
                if (configurationService.isLogDcsResponse()) {
                    LOGGER.info("DCS response " + responseBody);
                }
                DcsResponse unwrappedDcsResponse =
                        dcsCryptographyService.unwrapDcsResponse(responseBody);
                validateDcsResponse(unwrappedDcsResponse);

                LOGGER.info("Third party response successfully mapped");
                eventProbe.counterMetric(THIRD_PARTY_DCS_RESPONSE_OK);

                DocumentCheckResult documentCheckResult = new DocumentCheckResult();
                documentCheckResult.setExecutedSuccessfully(true);
                documentCheckResult.setTransactionId(unwrappedDcsResponse.getRequestId());
                documentCheckResult.setValid(unwrappedDcsResponse.isValid());

                return documentCheckResult;
            } catch (IpvCryptoException e) {
                // Seen when a signing cert has expired and all message signatures fail verification
                // We need to log this specific error message from the IpvCryptoException for
                // context
                LOGGER.error(e.getMessage(), e);
                eventProbe.counterMetric(THIRD_PARTY_DCS_RESPONSE_TYPE_ERROR);
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_UNWRAP_DCS_RESPONSE);
            }
        } else {

            String responseText = responseBody == null ? "No Text Found" : responseBody;

            LOGGER.error(
                    "DCS replied with HTTP status code {}, response text: {}",
                    statusCode,
                    responseText);

            eventProbe.counterMetric(THIRD_PARTY_DCS_RESPONSE_TYPE_ERROR);

            if (statusCode >= 300 && statusCode <= 399) {
                // Not Seen
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DCS_ERROR_HTTP_30X);
            } else if (statusCode >= 400 && statusCode <= 499) {
                // Seen when a cert has expired
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DCS_ERROR_HTTP_40X);
            } else if (statusCode >= 500 && statusCode <= 599) {
                // Error on DCS side
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DCS_ERROR_HTTP_50X);
            } else {
                // Any other status codes
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DCS_ERROR_HTTP_X);
            }
        }
    }

    private HttpPost requestBuilder(URI endpointUri, String requestBody)
            throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/jose");

        request.setEntity(new StringEntity(requestBody));

        return request;
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
