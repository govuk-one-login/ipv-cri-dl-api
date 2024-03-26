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
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.DvaHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.service.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DVA;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_INVALID_REQUEST_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_REQUEST_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DVA_RESPONSE_TYPE_VALID;

public class DvaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvaThirdPartyDocumentGateway.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final APIResultSource API_RESULT_SOURCE = DVA;
    private final DvaCryptographyService dvaCryptographyService;
    private final RequestHashValidator requestHashValidator;
    private final ObjectMapper objectMapper;
    private final DrivingPermitConfigurationService drivingPermitConfigurationService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;

    private final HttpRetryStatusConfig httpRetryStatusConfig;

    public DvaThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DvaCryptographyService dvaCryptographyService,
            RequestHashValidator requestHashValidator,
            DrivingPermitConfigurationService drivingPermitConfigurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
        this.objectMapper = objectMapper;
        this.dvaCryptographyService = dvaCryptographyService;
        this.requestHashValidator = requestHashValidator;
        this.drivingPermitConfigurationService = drivingPermitConfigurationService;
        this.httpRetryer = httpRetryer;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new DvaHttpRetryStatusConfig();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitData)
            throws OAuthErrorResponseException {
        LOGGER.info("Mapping person to third party document check request");
        DvaPayload dvaPayload = new DvaPayload();

        String drivingPermitFamilyName = drivingPermitData.getSurname();
        List<String> drivingPermitGivenNames = drivingPermitData.getForenames();
        LocalDate drivingPermitDateOfBirth = drivingPermitData.getDateOfBirth();
        LocalDate drivingPermitValidFrom = drivingPermitData.getIssueDate();
        LocalDate drivingPermitValidTo = drivingPermitData.getExpiryDate();
        String drivingPermitDriverLicenceNumber = drivingPermitData.getDrivingLicenceNumber();
        String drivingPermitAddress = drivingPermitData.getPostcode();

        DvaConfiguration dvaConfiguration = drivingPermitConfigurationService.getDvaConfiguration();

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

        if (drivingPermitConfigurationService.isDvaPerformanceStub()) {
            try {
                request.addHeader(
                        "request-hash",
                        new RequestHashValidator.HashFactory()
                                .getHash(
                                        dvaPayload,
                                        drivingPermitConfigurationService.isDvaPerformanceStub()));
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("failed to hash payload successfully for testing");
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.INCORRECT_HASH_VALIDATION_ALGORITHM_ERROR);
            }
        }

        eventProbe.counterMetric(DVA_REQUEST_CREATED.withEndpointPrefix());

        LOGGER.info("Submitting document check request to DVA...");

        DocumentCheckResult documentCheckResult;
        try (CloseableHttpResponse httpResponse =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {
            eventProbe.counterMetric(DVA_REQUEST_SEND_OK.withEndpointPrefix());
            documentCheckResult =
                    responseHandler(dvaPayload, httpResponse, dvaPayload.getRequestId().toString());
        } catch (IOException e) {
            LOGGER.error("IOException executing http request {}", e.getMessage());
            eventProbe.counterMetric(DVA_REQUEST_SEND_ERROR.withEndpointPrefix());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.ERROR_CONTACTING_DVA);
        } catch (ParseException | JOSEException e) {

            LOGGER.error(e.getMessage());
            eventProbe.counterMetric(DVA_RESPONSE_TYPE_INVALID.withEndpointPrefix());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_UNWRAP_DVA_RESPONSE);
        }

        eventProbe.counterMetric(DVA_RESPONSE_TYPE_VALID.withEndpointPrefix());

        documentCheckResult.setApiResultSource(API_RESULT_SOURCE);

        return documentCheckResult;
    }

    private JWSObject preparePayload(DvaPayload dvaPayload) throws OAuthErrorResponseException {
        LOGGER.info("Preparing payload for DVA");
        try {
            return dvaCryptographyService.preparePayload(dvaPayload);
        } catch (JOSEException | JsonProcessingException e) {
            LOGGER.error(("Failed to prepare payload for DVA: " + e.getMessage()));
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_DVA_PAYLOAD);
        }
    }

    private void validateDvaResponse(DvaPayload dvaPayload, DvaResponse dvaResponse)
            throws OAuthErrorResponseException, NoSuchAlgorithmException {
        if (Objects.nonNull(dvaResponse.getRequestHash())) {
            LOGGER.info("Validating DVA Direct response hash");
            if (!requestHashValidator.valid(
                    dvaPayload,
                    dvaResponse.getRequestHash(),
                    drivingPermitConfigurationService.isDvaPerformanceStub())) {
                throw new OAuthErrorResponseException(
                        HttpStatusCode.BAD_REQUEST, ErrorResponse.DVA_D_HASH_VALIDATION_ERROR);
            } else {
                LOGGER.info("Successfully validated DVA Direct response hash");
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
                if (drivingPermitConfigurationService.isLogDvaResponse()) {
                    LOGGER.info("DVA response " + responseBody);
                }
                DvaResponse unwrappedDvaResponse =
                        dvaCryptographyService.unwrapDvaResponse(responseBody);
                validateDvaResponse(dvaPayload, unwrappedDvaResponse);

                LOGGER.info("Third party response successfully mapped");
                eventProbe.counterMetric(
                        DVA_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

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
                eventProbe.counterMetric(DVA_RESPONSE_TYPE_ERROR.withEndpointPrefix());
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

            eventProbe.counterMetric(DVA_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            if (statusCode >= 300 && statusCode <= 399) {
                // Not Seen
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_30X);
            } else if (statusCode >= 400 && statusCode <= 499) {
                if (statusCode == 400) {
                    LOGGER.error(
                            "DVA replied with an InvalidRequestError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(DVA_INVALID_REQUEST_ERROR.withEndpointPrefix());

                    throw new OAuthErrorResponseException(
                            HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.DVA_ERROR_HTTP_400);
                } else if (statusCode == 401) {
                    LOGGER.error(
                            "DVA replied with an UnauthorizedError. Please check the schema and request sent to DVA");
                    eventProbe.counterMetric(DVA_REQUEST_ERROR.withEndpointPrefix());

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
