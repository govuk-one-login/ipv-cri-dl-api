package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DVA_INVALID_REQUEST_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DVA_RESPONSE_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DVA_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_DVA_UNAUTHORIZED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.THIRD_PARTY_REQUEST_CREATED;

public class DvaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvaThirdPartyDocumentGateway.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger();
    private DvaCryptographyService dvaCryptographyService;
    private final ObjectMapper objectMapper;
    private final ConfigurationService configurationService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;
    private static final String OPENID_CHECK_METHOD_IDENTIFIER = "data";
    private static final String IDENTITY_CHECK_POLICY = "published";

    public DvaThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DvaCryptographyService dvaCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(dvaCryptographyService, "dvaCryptographyService must not be null");
        Objects.requireNonNull(configurationService, "configurationService must not be null");
        Objects.requireNonNull(httpRetryer, "httpRetryer must not be null");

        this.objectMapper = objectMapper;
        this.dvaCryptographyService = dvaCryptographyService;
        this.configurationService = configurationService;
        this.httpRetryer = httpRetryer;
        this.eventProbe = eventProbe;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitData)
            throws InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException, IOException,
                    NoSuchAlgorithmException {
        LOGGER.info("Mapping person to third party document check request");
        DvaPayload dvaPayload = new DvaPayload();

        String drivingPermitFamilyName = drivingPermitData.getSurname();
        List<String> drivingPermitGivenNames = drivingPermitData.getForenames();
        LocalDate drivingPermitDateOfBirth = drivingPermitData.getDateOfBirth();
        LocalDate drivingPermitValidFrom = drivingPermitData.getIssueDate();
        LocalDate drivingPermitValidTo = drivingPermitData.getExpiryDate();
        String drivingPermitDriverLicenceNumber = drivingPermitData.getDrivingLicenceNumber();
        String drivingPermitAddress = drivingPermitData.getPostcode();

        String dvaEndpointUri = null;

        // Note: dva direct request fields have different names/mappings to the
        // drivingPermitForm
        // the below is for mapping the form fields into the correct dva field names

        dvaEndpointUri = configurationService.getDvaEndpointUri() + "/api/ukverify";
        dvaPayload.setRequestId(UUID.randomUUID());
        dvaPayload.setSurname(drivingPermitFamilyName);
        dvaPayload.setForenames(drivingPermitGivenNames);
        dvaPayload.setDateOfBirth(drivingPermitDateOfBirth);

        dvaPayload.setExpiryDate(drivingPermitValidTo);
        dvaPayload.setIssuerId("DVA");
        dvaPayload.setDriverLicenceNumber(drivingPermitDriverLicenceNumber);
        dvaPayload.setPostcode(drivingPermitAddress);

        // Note: DateOfIssue is mapped to issueDate in the front end to simplify
        // api handling of that field
        // Here (for the DVA request) it needs to be mapped back to date of issue
        dvaPayload.setDateOfIssue(drivingPermitValidFrom);

        JWSObject preparedDvaPayload = preparePayload(dvaPayload);

        String requestBody = preparedDvaPayload.serialize();

        URI endpoint = URI.create(dvaEndpointUri);
        HttpPost request = requestBuilder(endpoint, requestBody);

        eventProbe.counterMetric(THIRD_PARTY_REQUEST_CREATED);

        LOGGER.info("Submitting document check request to DVA...");

        DocumentCheckResult documentCheckResult;
        try (CloseableHttpResponse httpResponse =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request)) {
            documentCheckResult =
                    responseHandler(httpResponse, dvaPayload.getRequestId().toString());
        }

        if (documentCheckResult.isExecutedSuccessfully()) {
            // Data capture for VC
            CheckDetails checkDetails = new CheckDetails();
            checkDetails.setCheckMethod(OPENID_CHECK_METHOD_IDENTIFIER);
            checkDetails.setIdentityCheckPolicy(IDENTITY_CHECK_POLICY);

            if (documentCheckResult.isValid()) {
                // Map ActivityFrom to documentIssueDate (IssueDate / DateOfIssue)
                checkDetails.setActivityFrom(drivingPermitValidFrom.toString());
            }
            documentCheckResult.setCheckDetails(checkDetails);
        }

        return documentCheckResult;
    }

    public JWSObject decrypt(JWEObject encrypted) {
        try {
            RSADecrypter rsaDecrypter =
                    new RSADecrypter(configurationService.getDrivingPermitEncryptionKey());
            encrypted.decrypt(rsaDecrypter);

            return JWSObject.parse(encrypted.getPayload().toString());
        } catch (ParseException | JOSEException exception) {
            throw new IpvCryptoException(
                    String.format("Cannot Decrypt DVA Payload: %s", exception.getMessage()));
        }
    }

    private JWSObject preparePayload(DvaPayload dvaPayload)
            throws OAuthHttpResponseExceptionWithErrorBody {
        LOGGER.info("Preparing payload for DVA");
        try {
            return dvaCryptographyService.preparePayload(dvaPayload);
        } catch (CertificateException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | JOSEException
                | JsonProcessingException e) {
            LOGGER.error(("Failed to prepare payload for DVA: " + e.getMessage()));
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_DVA_PAYLOAD);
        }
    }

    private void validateDvaResponse(DvaResponse dvaResponse)
            throws OAuthHttpResponseExceptionWithErrorBody {
        if (Objects.nonNull(dvaResponse.getRequestHash())) {
            // RequestHash check
        } else {
            LOGGER.error("DVA returned an incomplete response");
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.BAD_GATEWAY, ErrorResponse.DVA_RETURNED_AN_INCOMPLETE_RESPONSE);
        }
    }

    private DocumentCheckResult responseHandler(
            CloseableHttpResponse httpResponse, String requestId)
            throws IOException, ParseException, JOSEException, CertificateException,
                    OAuthHttpResponseExceptionWithErrorBody {
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        HttpEntity entity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);

        LOGGER.info("Third party response code {}", statusCode);

        if (statusCode == 200) {
            try {
                if (configurationService.isLogThirdPartyResponse()) {
                    LOGGER.info("DVA response " + responseBody);
                }
                DvaResponse unwrappedDvaResponse =
                        dvaCryptographyService.unwrapDvaResponse(responseBody);
                validateDvaResponse(unwrappedDvaResponse);

                LOGGER.info("Third party response successfully mapped");
                eventProbe.counterMetric(THIRD_PARTY_DVA_RESPONSE_OK);

                DocumentCheckResult documentCheckResult = new DocumentCheckResult();
                documentCheckResult.setTransactionId(requestId);
                documentCheckResult.setExecutedSuccessfully(true);
                documentCheckResult.setValid(unwrappedDvaResponse.isValidDocument());

                return documentCheckResult;
            } catch (IpvCryptoException e) {
                // Seen when a signing cert has expired and all message signatures fail verification
                // We need to log this specific error message from the IpvCryptoException for
                // context
                LOGGER.error(e.getMessage(), e);
                eventProbe.counterMetric(THIRD_PARTY_DVA_RESPONSE_TYPE_ERROR);
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_UNWRAP_DVA_RESPONSE);
            }
        } else {

            String responseText = responseBody == null ? "No Text Found" : responseBody;

            LOGGER.error(
                    "DVA replied with HTTP status code {}, response text: {}",
                    statusCode,
                    responseText);

            eventProbe.counterMetric(THIRD_PARTY_DVA_RESPONSE_TYPE_ERROR);

            if (statusCode >= 300 && statusCode <= 399) {
                // Not Seen
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_30X);
            } else if (statusCode >= 400 && statusCode <= 499) {
                if (statusCode == 400) {
                    LOGGER.error(
                            "DVA replied with an InvalidRequestError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(THIRD_PARTY_DVA_INVALID_REQUEST_ERROR);

                    throw new OAuthHttpResponseExceptionWithErrorBody(
                            HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_400);
                } else if (statusCode == 401) {
                    LOGGER.error(
                            "DVA replied with an UnauthorizedError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(THIRD_PARTY_DVA_UNAUTHORIZED_ERROR);

                    throw new OAuthHttpResponseExceptionWithErrorBody(
                            HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_401);
                }
                // Seen when a cert has expired
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_40X);
            } else if (statusCode >= 500 && statusCode <= 599) {
                // Error on DCS side
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_50X);
            } else {
                // Any other status codes
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_X);
            }
        }
    }

    private HttpPost requestBuilder(URI endpointUri, String requestBody)
            throws UnsupportedEncodingException {
        String user = configurationService.getDvaUserName();
        String pass = configurationService.getDvaPassword();
        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/jose");
        // basic auth
        request.addHeader("Authorization", getBasicAuthenticationHeader(user, pass));
        request.setEntity(new StringEntity(requestBody));

        return request;
    }

    private String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
