package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
    private final DvaCryptographyService dvaCryptographyService;
    private final RequestHashValidator requestHashValidator;
    private final ObjectMapper objectMapper;
    private final ConfigurationService configurationService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;
    private static final String OPENID_CHECK_METHOD_IDENTIFIER = "data";
    private static final String IDENTITY_CHECK_POLICY = "published";

    public DvaThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DvaCryptographyService dvaCryptographyService,
            RequestHashValidator requestHashValidator,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
        this.objectMapper = objectMapper;
        this.dvaCryptographyService = dvaCryptographyService;
        this.requestHashValidator = requestHashValidator;
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
            throws OAuthErrorResponseException, ParseException, JOSEException {
        LOGGER.info("Mapping person to third party document check request");
        DvaPayload dvaPayload = new DvaPayload();

        String drivingPermitFamilyName = drivingPermitData.getSurname();
        List<String> drivingPermitGivenNames = drivingPermitData.getForenames();
        LocalDate drivingPermitDateOfBirth = drivingPermitData.getDateOfBirth();
        LocalDate drivingPermitValidFrom = drivingPermitData.getIssueDate();
        LocalDate drivingPermitValidTo = drivingPermitData.getExpiryDate();
        String drivingPermitDriverLicenceNumber = drivingPermitData.getDrivingLicenceNumber();
        String drivingPermitAddress = drivingPermitData.getPostcode();

        DvaConfiguration dvaConfiguration = configurationService.getDvaConfiguration();

        String dvaEndpointUri = null;

        // Note: dva direct request fields have different names/mappings to the
        // drivingPermitForm
        // the below is for mapping the form fields into the correct dva field names

        dvaEndpointUri = dvaConfiguration.getEndpointUri() + "/api/ukverify";
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
        String username = dvaConfiguration.getUserName();
        String password = dvaConfiguration.getPassword();
        HttpPost request = requestBuilder(endpoint, username, password, requestBody);

        eventProbe.counterMetric(THIRD_PARTY_REQUEST_CREATED);

        LOGGER.info("Submitting document check request to DVA...");

        DocumentCheckResult documentCheckResult;
        try (CloseableHttpResponse httpResponse =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request)) {
            documentCheckResult =
                    responseHandler(dvaPayload, httpResponse, dvaPayload.getRequestId().toString());
        } catch (IOException e) {
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.ERROR_CONTACTING_DVA);
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

    private JWSObject preparePayload(DvaPayload dvaPayload) throws OAuthErrorResponseException {
        LOGGER.info("Preparing payload for DVA");
        try {
            return dvaCryptographyService.preparePayload(dvaPayload);
        } catch (CertificateException | JOSEException | JsonProcessingException e) {
            LOGGER.error(("Failed to prepare payload for DVA: " + e.getMessage()));
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_DVA_PAYLOAD);
        }
    }

    private void validateDvaResponse(DvaPayload dvaPayload, DvaResponse dvaResponse)
            throws OAuthErrorResponseException, NoSuchAlgorithmException {
        if (Objects.nonNull(dvaResponse.getRequestHash())) {
            LOGGER.error("Validating DVA Direct response hash");
            if (!requestHashValidator.valid(
                    dvaPayload,
                    dvaResponse.getRequestHash(),
                    configurationService.isDvaPerformanceStub())) {
                throw new OAuthErrorResponseException(
                        HttpStatusCode.BAD_REQUEST, ErrorResponse.DVA_D_HASH_VALIDATION_ERROR);
            } else {
                LOGGER.error("Successfully validated DVA Direct response hash");
            }
        } else {
            LOGGER.error("DVA returned an incomplete response");
            throw new OAuthErrorResponseException(
                    HttpStatusCode.BAD_GATEWAY, ErrorResponse.DVA_RETURNED_AN_INCOMPLETE_RESPONSE);
        }
    }

    private DocumentCheckResult responseHandler(
            DvaPayload dvaPayload, CloseableHttpResponse httpResponse, String requestId)
            throws IOException, ParseException, JOSEException, OAuthErrorResponseException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        HttpEntity entity = httpResponse.getEntity();
        String responseBody = EntityUtils.toString(entity);

        LOGGER.info("Third party response code {}", statusCode);

        if (statusCode == 200) {
            try {
                if (configurationService.isLogDvaResponse()) {
                    LOGGER.info("DVA response " + responseBody);
                }
                DvaResponse unwrappedDvaResponse =
                        dvaCryptographyService.unwrapDvaResponse(responseBody);
                validateDvaResponse(dvaPayload, unwrappedDvaResponse);

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
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_UNWRAP_DVA_RESPONSE);
            } catch (NoSuchAlgorithmException e) {
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.INCORRECT_HASH_VALIDATION_ALGORITHM_ERROR);
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
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_30X);
            } else if (statusCode >= 400 && statusCode <= 499) {
                if (statusCode == 400) {
                    LOGGER.error(
                            "DVA replied with an InvalidRequestError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(THIRD_PARTY_DVA_INVALID_REQUEST_ERROR);

                    throw new OAuthErrorResponseException(
                            HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_400);
                } else if (statusCode == 401) {
                    LOGGER.error(
                            "DVA replied with an UnauthorizedError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(THIRD_PARTY_DVA_UNAUTHORIZED_ERROR);

                    throw new OAuthErrorResponseException(
                            HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_401);
                }
                // Seen when a cert has expired
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_40X);
            } else if (statusCode >= 500 && statusCode <= 599) {
                // Error on DCS side
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_50X);
            } else {
                // Any other status codes
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_X);
            }
        }
    }

    private HttpPost requestBuilder(
            URI endpointUri, String userName, String password, String requestBody) {
        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/jose");
        // basic auth
        request.addHeader("Authorization", getBasicAuthenticationHeader(userName, password));
        request.setEntity(new StringEntity(requestBody, ContentType.DEFAULT_TEXT));

        return request;
    }

    private String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
