package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.SharedClaims;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentVerificationResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.util.DocumentCheckPersonIdentityDetailedMapper;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermit;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

public class DrivingPermitHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String LAMBDA_NAME = "driving_permit_issue_credential";

    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;
    private final PersonIdentityService personIdentityService;
    private final SessionService sessionService;
    private final DataStore<DocumentCheckResultItem> dataStore;
    private final ConfigurationService configurationService;
    private final AuditService auditService;

    // TODO move this to a parameter store variable
    private static final int MAX_ATTEMPTS = 2;

    public DrivingPermitHandler()
            throws NoSuchAlgorithmException, InvalidKeyException, CertificateException,
                    InvalidKeySpecException, HttpException, KeyStoreException, IOException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ServiceFactory serviceFactory = new ServiceFactory(objectMapper);
        this.eventProbe = new EventProbe();
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.personIdentityService = new PersonIdentityService();
        this.sessionService = new SessionService();
        this.configurationService = serviceFactory.getConfigurationService();
        this.dataStore =
                new DataStore<>(
                        configurationService.getDocumentCheckResultTableName(),
                        DocumentCheckResultItem.class,
                        DataStore.getClient());
        this.auditService = serviceFactory.getAuditService();
    }

    @ExcludeFromGeneratedCoverageReport
    public DrivingPermitHandler(
            ServiceFactory serviceFactory,
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            PersonIdentityService personIdentityService,
            SessionService sessionService,
            DataStore<DocumentCheckResultItem> dataStore,
            ConfigurationService configurationService,
            AuditService auditService) {
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.personIdentityService = personIdentityService;
        this.sessionService = sessionService;
        this.configurationService = configurationService;
        this.dataStore = dataStore;
        this.auditService = auditService;
    }

    @Override
    @Logging(correlationIdPath = CorrelationIdPathConstants.API_GATEWAY_REST)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            LOGGER.info(
                    "Initiating lambda {} version {}",
                    context.getFunctionName(),
                    context.getFunctionVersion());
            Map<String, String> headers = input.getHeaders();
            final String sessionId = headers.get("session_id");
            LOGGER.info("Extracting session from header ID {}", sessionId);
            var sessionItem = sessionService.validateSessionId(sessionId);

            // Attempt Start
            sessionItem.setAttemptCount(sessionItem.getAttemptCount() + 1);

            LOGGER.info("Attempt Number {}", sessionItem.getAttemptCount());

            // Stop being called more than MAX_ATTEMPTS
            if (sessionItem.getAttemptCount() > MAX_ATTEMPTS) {

                LOGGER.error(
                        "Attempt count {} is over the max of {}",
                        sessionItem.getAttemptCount(),
                        MAX_ATTEMPTS);

                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.SERVER_ERROR);
            }

            LOGGER.info("Verifying document details...");
            DrivingPermitForm drivingPermitFormData =
                    parseDrivingPermitFormRequest(input.getBody());
            DocumentCheckVerificationResult result =
                    identityVerificationService.verifyIdentity(drivingPermitFormData);

            result.setAttemptCount(sessionItem.getAttemptCount());

            auditService.sendAuditEvent(
                    AuditEventType.THIRD_PARTY_REQUEST_ENDED,
                    new AuditEventContext(headers, sessionItem),
                    "");

            auditService.sendAuditEvent(
                    AuditEventType.REQUEST_SENT,
                    new AuditEventContext(
                            DocumentCheckPersonIdentityDetailedMapper
                                    .generatePersonIdentityDetailed(drivingPermitFormData),
                            input.getHeaders(),
                            sessionItem));

            saveAttempt(sessionItem, drivingPermitFormData, result);

            boolean canRetry = true;

            if (result.isExecutedSuccessfully() && result.isVerified()) {

                LOGGER.info("Document verified");
                eventProbe.counterMetric(LAMBDA_NAME);

                canRetry = false;
            } else if (result.getAttemptCount() >= MAX_ATTEMPTS) {

                LOGGER.info(
                        "Ending document verification after {} attempts", result.getAttemptCount());
                eventProbe.counterMetric(LAMBDA_NAME);

                canRetry = false;
            } else {
                LOGGER.info("Document not verified at attempt {}", result.getAttemptCount());

                canRetry = true;
            }

            LOGGER.info("CanRetry {}", canRetry);

            DocumentVerificationResponse response = new DocumentVerificationResponse();
            response.setRetry(canRetry);

            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, response);
        } catch (OAuthHttpResponseExceptionWithErrorBody e) {
            LOGGER.error("Encountered error in DCS request : {}", e.getErrorReason());
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    e.getStatusCode(), e.getErrorReason());
        } catch (Exception e) {
            LOGGER.warn("Exception while handling lambda {}", context.getFunctionName());
            eventProbe.log(Level.ERROR, e).counterMetric(LAMBDA_NAME, 0d);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.GENERIC_SERVER_ERROR);
        }
    }

    private void saveAttempt(
            SessionItem sessionItem,
            DrivingPermitForm drivingPermitFormData,
            DocumentCheckVerificationResult result) {

        LOGGER.info("Generating authorization code...");
        sessionService.createAuthorizationCode(sessionItem);

        LOGGER.info("Saving person identity...");
        BirthDate birthDate = new BirthDate();
        birthDate.setValue(drivingPermitFormData.getDateOfBirth());

        SharedClaims sharedClaims = new SharedClaims();
        sharedClaims.setAddresses(drivingPermitFormData.getAddresses());
        sharedClaims.setBirthDates(List.of(birthDate));
        sharedClaims.setNames(
                List.of(
                        DocumentCheckPersonIdentityDetailedMapper.mapNamesToCanonicalName(
                                drivingPermitFormData.getForenames(),
                                drivingPermitFormData.getSurname())));

        personIdentityService.savePersonIdentity(sessionItem.getSessionId(), sharedClaims);
        LOGGER.info("person identity saved.");

        final DocumentCheckResultItem documentCheckResultItem =
                mapVerificationResultToResultItem(sessionItem, result);

        LOGGER.info("Saving document check results...");
        dataStore.create(documentCheckResultItem);
        LOGGER.info("document check results saved.");
    }

    private DrivingPermitForm parseDrivingPermitFormRequest(String input)
            throws OAuthHttpResponseExceptionWithErrorBody {
        LOGGER.info("Parsing passport form data into payload for DCS");
        try {
            return objectMapper.readValue(input, DrivingPermitForm.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(("Failed to parse payload from input: " + e.getMessage()));
            throw new OAuthHttpResponseExceptionWithErrorBody(
                    HttpStatusCode.BAD_REQUEST,
                    uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse
                            .FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }
    }

    private DocumentCheckResultItem mapVerificationResultToResultItem(
            SessionItem sessionItem, DocumentCheckVerificationResult result) {
        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();

        documentCheckResultItem.setSessionId(sessionItem.getSessionId());
        documentCheckResultItem.setContraIndicators(result.getContraIndicators());

        documentCheckResultItem.setStrengthScore(result.getStrengthScore());
        documentCheckResultItem.setValidityScore(result.getValidityScore());
        documentCheckResultItem.setActivityHistoryScore(result.getActivityHistoryScore());

        CheckDetails checkDetails = result.getCheckDetails();

        documentCheckResultItem.setActivityFrom(checkDetails.getActivityFrom());
        documentCheckResultItem.setCheckMethod(checkDetails.getCheckMethod());
        documentCheckResultItem.setIdentityCheckPolicy(checkDetails.getIdentityCheckPolicy());

        DrivingPermit drivingPermit = result.getDrivingPermit();
        documentCheckResultItem.setDocumentNumber(drivingPermit.getDocumentNumber());
        documentCheckResultItem.setIssuedBy(drivingPermit.getIssuedBy());
        documentCheckResultItem.setExpiryDate(drivingPermit.getExpiryDate());

        documentCheckResultItem.setTransactionId(result.getTransactionId());

        return documentCheckResultItem;
    }
}
