package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.SharedClaims;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentVerificationResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.service.FormDataValidator;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.PersonIdentityDetailedHelperMapper;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVLA;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.TOO_MANY_RETRY_ATTEMPTS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DL_FALL_BACK_EXECUTING;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DL_VERIFICATION_FALLBACK_DEVIATION;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK;

public class DrivingPermitHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String HEADER_DOCUMENT_CHECKING_ROUTE = "document-checking-route";

    private ObjectMapper objectMapper;
    private EventProbe eventProbe;
    private PersonIdentityService personIdentityService;
    private SessionService sessionService;
    private DataStore<DocumentCheckResultItem> dataStore;
    private ConfigurationService configurationService;
    private AuditService auditService;

    private ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory;
    private IdentityVerificationService identityVerificationService;

    public DrivingPermitHandler()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException,
                    HttpException, KeyStoreException, IOException {
        ServiceFactory serviceFactory = new ServiceFactory();
        ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactoryNotAssignedYet =
                new ThirdPartyAPIServiceFactory(serviceFactory);

        IdentityVerificationService identityVerificationServiceNotAssignedYet =
                createIdentityVerificationService(serviceFactory);

        initializeLambdaServices(
                serviceFactory,
                thirdPartyAPIServiceFactoryNotAssignedYet,
                identityVerificationServiceNotAssignedYet);
    }

    public DrivingPermitHandler(
            ServiceFactory serviceFactory,
            ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory,
            IdentityVerificationService identityVerificationService) {
        initializeLambdaServices(
                serviceFactory, thirdPartyAPIServiceFactory, identityVerificationService);
    }

    public void initializeLambdaServices(
            ServiceFactory serviceFactory,
            ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory,
            IdentityVerificationService identityVerificationService) {
        this.objectMapper = serviceFactory.getObjectMapper();
        this.eventProbe = serviceFactory.getEventProbe();
        this.sessionService = serviceFactory.getSessionService();
        this.auditService = serviceFactory.getAuditService();
        this.personIdentityService = serviceFactory.getPersonIdentityService();

        this.configurationService = serviceFactory.getConfigurationService();
        this.dataStore = serviceFactory.getDataStore();
        this.thirdPartyAPIServiceFactory = thirdPartyAPIServiceFactory;
        this.identityVerificationService = identityVerificationService;
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

            // Attempt Start
            final int MAX_ATTEMPTS = configurationService.getMaxAttempts();

            // Stop being called more than MAX_ATTEMPTS
            if (sessionItem.getAttemptCount() > MAX_ATTEMPTS) {

                LOGGER.error(
                        "Attempt count {} is over the max of {}",
                        sessionItem.getAttemptCount(),
                        MAX_ATTEMPTS);

                // Driving Permit Lambda Completed with an Error
                eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

                // TODO change this to a redirect onwards
                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR, TOO_MANY_RETRY_ATTEMPTS);
            }

            DrivingPermitForm drivingPermitFormData =
                    parseDrivingPermitFormRequest(input.getBody());

            ThirdPartyAPIService thirdPartyAPIService =
                    selectThirdPartyAPIService(
                            configurationService.getDvaDirectEnabled(),
                            configurationService.getDvlaDirectEnabled(),
                            headers.get(HEADER_DOCUMENT_CHECKING_ROUTE),
                            drivingPermitFormData.getLicenceIssuer());

            LOGGER.info(
                    "Verifying document details using {}", thirdPartyAPIService.getServiceName());

            DocumentCheckVerificationResult result = null;

            boolean thirdPartyIsDcs =
                    thirdPartyAPIService
                            .getServiceName()
                            .equals(DcsThirdPartyDocumentGateway.class.getSimpleName());

            boolean thirdPartyIsDva =
                    thirdPartyAPIService
                            .getServiceName()
                            .equals(DvaThirdPartyDocumentGateway.class.getSimpleName());

            try {
                result =
                        identityVerificationService.verifyIdentity(
                                drivingPermitFormData, thirdPartyAPIService);
                if (!thirdPartyIsDcs && !thirdPartyIsDva) {
                    LOGGER.info("Checking if verification fallback is required");
                    result = executeFallbackIfDocumentFailedToVerify(result, drivingPermitFormData);
                }
            } catch (Exception e) {
                LOGGER.info("Exception {}, checking if fallback is required", e.getClass());

                if (!thirdPartyIsDcs && !thirdPartyIsDva) {
                    LOGGER.info(
                            "Exception has occurred during fallback window. Executing request with DVAD");
                    result = executeFallbackRequest(drivingPermitFormData);
                } else {
                    throw e;
                }
            }

            result.setAttemptCount(sessionItem.getAttemptCount());

            auditService.sendAuditEvent(
                    AuditEventType.RESPONSE_RECEIVED, new AuditEventContext(headers, sessionItem));

            LOGGER.info("Sending audit event REQUEST_SENT...");
            auditService.sendAuditEvent(
                    AuditEventType.REQUEST_SENT,
                    new AuditEventContext(
                            PersonIdentityDetailedHelperMapper
                                    .drivingPermitFormDataToAuditRestrictedFormat(
                                            drivingPermitFormData),
                            input.getHeaders(),
                            sessionItem));

            saveAttempt(sessionItem, drivingPermitFormData, result);

            boolean canRetry = true;

            if (result.isExecutedSuccessfully() && result.isVerified()) {
                LOGGER.info("Document verified");
                eventProbe.counterMetric(
                        LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX
                                + sessionItem.getAttemptCount());

                canRetry = false;
            } else if (result.getAttemptCount() >= MAX_ATTEMPTS) {
                LOGGER.info(
                        "Ending document verification after {} attempts", result.getAttemptCount());
                eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED);

                canRetry = false;
            } else {
                LOGGER.info("Document not verified at attempt {}", result.getAttemptCount());
                eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY);

                canRetry = true;
            }

            LOGGER.info("CanRetry {}", canRetry);

            DocumentVerificationResponse response = new DocumentVerificationResponse();
            response.setRetry(canRetry);

            // Driving Permit Completed Normally
            eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, response);
        } catch (OAuthErrorResponseException e) {
            // Driving Permit Lambda Completed with an Error
            eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

            // Debug in DEV only as Oauth errors appear in the redirect url
            // This will output the specific error message
            // Note Unit tests expect server error (correctly)
            // and will fail if this is set (during unit tests)
            if (configurationService.isDevEnvironmentOnlyEnhancedDebugSet()) {
                String customOAuth2ErrorDescription = e.getErrorReason();
                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        e.getStatusCode(), // Status Code determined by throw location
                        new CommonExpressOAuthError(
                                OAuth2Error.SERVER_ERROR, customOAuth2ErrorDescription));
            }

            // Non-debug route - standard OAuth2Error.SERVER_ERROR
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    e.getStatusCode(), // Status Code determined by throw location
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        } catch (Exception e) {
            // This is where unexpected exceptions will reach (null pointers etc)
            // Expected exceptions should be caught and thrown as
            // OAuthErrorResponseException
            // We should not log unknown exceptions, due to possibility of PII
            LOGGER.error(
                    "Unhandled Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    e.getClass());

            LOGGER.debug(e.getMessage(), e);

            eventProbe.counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
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
                        PersonIdentityDetailedHelperMapper.mapNamesToCanonicalName(
                                drivingPermitFormData.getForenames(),
                                drivingPermitFormData.getSurname())));

        personIdentityService.savePersonIdentity(sessionItem.getSessionId(), sharedClaims);
        LOGGER.info("person identity saved.");

        final DocumentCheckResultItem documentCheckResultItem =
                mapVerificationResultToResultItem(sessionItem, result, drivingPermitFormData);
        documentCheckResultItem.setExpiry(
                configurationService.getDocumentCheckItemExpirationEpoch());

        LOGGER.info("Saving document check results...");
        dataStore.create(documentCheckResultItem);
        LOGGER.info("document check results saved.");
    }

    private DrivingPermitForm parseDrivingPermitFormRequest(String input)
            throws OAuthErrorResponseException {
        LOGGER.info("Parsing driving permit form data into payload for third party document check");
        try {
            return objectMapper.readValue(input, DrivingPermitForm.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(("Failed to parse payload from input: " + e.getMessage()));
            throw new OAuthErrorResponseException(
                    HttpStatusCode.BAD_REQUEST,
                    uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse
                            .FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }
    }

    private DocumentCheckResultItem mapVerificationResultToResultItem(
            SessionItem sessionItem,
            DocumentCheckVerificationResult result,
            DrivingPermitForm drivingPermitFormData) {
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

        IssuingAuthority issuingAuthority =
                IssuingAuthority.valueOf(drivingPermitFormData.getLicenceIssuer());

        // Common Permit Fields
        documentCheckResultItem.setIssuedBy(drivingPermitFormData.getLicenceIssuer());
        documentCheckResultItem.setExpiryDate(drivingPermitFormData.getExpiryDate().toString());
        documentCheckResultItem.setDocumentNumber(drivingPermitFormData.getDrivingLicenceNumber());
        documentCheckResultItem.setIssueDate(drivingPermitFormData.getIssueDate().toString());

        // DVLA only field(s)
        if (issuingAuthority == DVLA) {
            documentCheckResultItem.setIssueNumber(drivingPermitFormData.getIssueNumber());
        }

        documentCheckResultItem.setTransactionId(result.getTransactionId());

        return documentCheckResultItem;
    }

    private IdentityVerificationService createIdentityVerificationService(
            ServiceFactory serviceFactory) {

        return new IdentityVerificationService(
                new FormDataValidator(), serviceFactory.getEventProbe());
    }

    private DocumentCheckVerificationResult executeFallbackRequest(
            DrivingPermitForm drivingPermitFormData) throws Exception {
        ThirdPartyAPIService fallbackThirdPartyService =
                selectThirdPartyAPIService(
                        false, false, "DCS", drivingPermitFormData.getLicenceIssuer());
        eventProbe.counterMetric(DL_FALL_BACK_EXECUTING);
        return identityVerificationService.verifyIdentity(
                drivingPermitFormData, fallbackThirdPartyService);
    }

    private DocumentCheckVerificationResult executeFallbackIfDocumentFailedToVerify(
            DocumentCheckVerificationResult documentDataVerificationResult,
            DrivingPermitForm drivingPermitFormData)
            throws Exception {
        if (!documentDataVerificationResult.isVerified()
                || (documentDataVerificationResult.getContraIndicators() != null
                        && !documentDataVerificationResult.getContraIndicators().isEmpty())) {
            LOGGER.info(
                    "Document has been marked unverified during fallback window. Executing request with Direct connection");
            documentDataVerificationResult = executeFallbackRequest(drivingPermitFormData);
            if (documentDataVerificationResult.isVerified()) {
                eventProbe.counterMetric(DL_VERIFICATION_FALLBACK_DEVIATION);
                LOGGER.warn(
                        "Document has been verified using DCS that failed verification using Direct connection");
            }
        }
        return documentDataVerificationResult;
    }

    private ThirdPartyAPIService selectThirdPartyAPIService(
            boolean dvaDirectEnabled,
            boolean dvlaDirectEnabled,
            String documentCheckingRoute,
            String licenseIssuer) {

        IssuingAuthority issuingAuthority = IssuingAuthority.valueOf(licenseIssuer);

        boolean direct = "direct".equals(documentCheckingRoute);

        if (direct && (issuingAuthority == IssuingAuthority.DVA) && dvaDirectEnabled) {
            return thirdPartyAPIServiceFactory.getDvaThirdPartyAPIService();
        } else if (direct && (issuingAuthority == IssuingAuthority.DVLA) && dvlaDirectEnabled) {
            return thirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService();
        } else {
            return thirdPartyAPIServiceFactory.getDcsThirdPartyAPIService();
        }
    }
}
