package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
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
import uk.gov.di.ipv.cri.common.library.exception.SessionExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.SessionNotFoundException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentVerificationResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.service.FormDataValidator;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DrivingPermitConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.RequestSentAuditHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.util.AcmCertificateService;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.logging.LoggingSupport;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.service.DocumentCheckResultStorageService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static uk.gov.di.ipv.cri.common.library.error.ErrorResponse.SESSION_NOT_FOUND;

public class DrivingPermitHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // We need this first and static for it to be created as soon as possible during function init
    private static final long FUNCTION_INIT_START_TIME_MILLISECONDS = System.currentTimeMillis();

    private static final Logger LOGGER = LogManager.getLogger();

    private static final boolean DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG =
            Boolean.parseBoolean(System.getenv("DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG"));

    // Maximum submissions from the front end form
    private static final int MAX_ATTEMPTS = 2;

    private ObjectMapper objectMapper;
    private EventProbe eventProbe;

    private SessionService sessionService;
    private AuditService auditService;

    private PersonIdentityService personIdentityService;
    private DocumentCheckResultStorageService documentCheckResultStorageService;

    private ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory;
    private IdentityVerificationService identityVerificationService;

    private long documentCheckResultItemTtl;

    private String environment;

    // For capturing run-time function init metric
    private long functionInitMetricLatchedValue = 0;
    private boolean functionInitMetricCaptured = false;

    @ExcludeFromGeneratedCoverageReport
    public DrivingPermitHandler()
            throws CertificateException,
                    NoSuchAlgorithmException,
                    InvalidKeySpecException,
                    JsonProcessingException {
        ServiceFactory serviceFactory = new ServiceFactory();

        DrivingPermitConfigurationService drivingPermitConfigurationServiceNotYetAssigned =
                createDrivingPermitConfigurationService(serviceFactory);

        AcmCertificateService acmCertificateService =
                new AcmCertificateService(serviceFactory.getClientProviderFactory().getAcmClient());

        ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactoryNotAssignedYet =
                new ThirdPartyAPIServiceFactory(
                        serviceFactory,
                        drivingPermitConfigurationServiceNotYetAssigned,
                        acmCertificateService);

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

        this.documentCheckResultStorageService =
                serviceFactory.getDocumentCheckResultStorageService();
        this.thirdPartyAPIServiceFactory = thirdPartyAPIServiceFactory;
        this.identityVerificationService = identityVerificationService;

        documentCheckResultItemTtl = Long.parseLong(System.getenv("SESSION_TTL"));

        // Get environment to decide if to output internal state
        // for api test asserts (dev only)
        String tEnvironment = System.getenv("ENVIRONMENT");
        this.environment = tEnvironment == null ? "Not-Set" : tEnvironment;

        // Runtime/SnapStart function init duration
        functionInitMetricLatchedValue =
                System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
    }

    private DrivingPermitConfigurationService createDrivingPermitConfigurationService(
            ServiceFactory serviceFactory) throws JsonProcessingException {

        ClientProviderFactory clientProviderFactory = serviceFactory.getClientProviderFactory();

        ParameterStoreService parameterStoreService = serviceFactory.getParameterStoreService();

        SecretsManagerService secretsManagerService =
                new SecretsManagerService(clientProviderFactory.getSecretsManagerClient());

        return new DrivingPermitConfigurationService(parameterStoreService, secretsManagerService);
    }

    @Override
    @Logging(correlationIdPath = CorrelationIdPathConstants.API_GATEWAY_REST)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            // There is logging before the session read which attaches journey keys
            // We clear these persistent ones now so these not attributed to any previous journey
            LoggingSupport.clearPersistentJourneyKeys();

            LOGGER.info(
                    "Initiating lambda {} version {}",
                    context.getFunctionName(),
                    context.getFunctionVersion());

            // Recorded here as sending metrics during function init may fail depending on lambda
            // config
            if (!functionInitMetricCaptured) {
                eventProbe.counterMetric(
                        Definitions.LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION,
                        functionInitMetricLatchedValue);
                LOGGER.info("Lambda function init duration {}ms", functionInitMetricLatchedValue);
                functionInitMetricCaptured = true;
            }

            // Lambda Lifetime
            long runTimeDuration =
                    System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
            Duration duration = Duration.of(runTimeDuration, ChronoUnit.MILLIS);
            String formattedDuration =
                    String.format(
                            "%d:%02d:%02d",
                            duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            LOGGER.info(
                    "Lambda {}, Lifetime duration {}, {}ms",
                    context.getFunctionName(),
                    formattedDuration,
                    runTimeDuration);

            Map<String, String> headers = input.getHeaders();
            final String sessionId = retrieveSessionIdFromHeaders(headers);

            LOGGER.info("Extracting session from header ID {}", sessionId);
            SessionItem sessionItem = sessionService.validateSessionId(sessionId);
            LOGGER.info("Persistent Logging keys now attached to sessionId {}", sessionId);

            if (null != sessionItem.getContext()) {
                eventProbe.counterMetric(Definitions.CONTEXT_VALUE + sessionItem.getContext());
                LOGGER.info("context value={}", sessionItem.getContext());
            } else {
                eventProbe.counterMetric(Definitions.CONTEXT_VALUE_NULL);
            }

            // Attempt Start
            sessionItem.setAttemptCount(sessionItem.getAttemptCount() + 1);
            LOGGER.info("Attempt Number {}", sessionItem.getAttemptCount());

            // Check we are not "now" above max_attempts to prevent doing another remote API call
            if (sessionItem.getAttemptCount() > MAX_ATTEMPTS) {

                // We do not treat this as a journey fail condition
                // The user has had multiple attempts recorded, we attempt to redirect them on
                LOGGER.warn(
                        "Attempt count {} is over the max of {}",
                        sessionItem.getAttemptCount(),
                        MAX_ATTEMPTS);

                eventProbe.counterMetric(
                        Definitions.LAMBDA_DRIVING_PERMIT_CHECK_USER_REDIRECTED_ATTEMPTS_OVER_MAX);

                APIGatewayProxyResponseEvent responseEvent = generateExitResponseEvent(false);

                // Use the completed OK exit sequence
                return lambdaCompletedOK(responseEvent);
            }

            DrivingPermitForm drivingPermitFormData =
                    parseDrivingPermitFormRequest(input.getBody());

            ThirdPartyAPIService thirdPartyAPIService =
                    selectThirdPartyAPIService(drivingPermitFormData.getLicenceIssuer());

            LOGGER.info(
                    "Verifying document details using {}", thirdPartyAPIService.getServiceName());

            // TestStrategy Logic
            String clientId = sessionItem.getClientId();
            Strategy strategy = Strategy.fromClientIdString(clientId);

            LOGGER.info("IPV Core Client Id {}, Routing set to {}", clientId, strategy);

            DocumentCheckVerificationResult documentCheckVerificationResult =
                    identityVerificationService.verifyIdentity(
                            drivingPermitFormData, thirdPartyAPIService, strategy);

            documentCheckVerificationResult.setAttemptCount(sessionItem.getAttemptCount());

            auditService.sendAuditEvent(
                    AuditEventType.RESPONSE_RECEIVED, new AuditEventContext(headers, sessionItem));

            LOGGER.info("Sending audit event REQUEST_SENT...");
            auditService.sendAuditEvent(
                    AuditEventType.REQUEST_SENT,
                    new AuditEventContext(
                            RequestSentAuditHelper.drivingPermitFormDataToAuditRestrictedFormat(
                                    drivingPermitFormData),
                            input.getHeaders(),
                            sessionItem));

            saveAttempt(sessionItem, drivingPermitFormData, documentCheckVerificationResult);

            boolean canRetry =
                    determineVerificationRetryStatus(
                            sessionItem, documentCheckVerificationResult, MAX_ATTEMPTS);
            LOGGER.info("CanRetry {}", canRetry);

            APIGatewayProxyResponseEvent responseEvent = generateExitResponseEvent(canRetry);

            // Use the completed OK exit sequence
            return lambdaCompletedOK(responseEvent);
        } catch (OAuthErrorResponseException e) {
            // Driving Permit Lambda Completed with an Error
            eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

            CommonExpressOAuthError commonExpressOAuthError;

            if (!DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG) {
                // Standard oauth compliant route
                commonExpressOAuthError = new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR);
            } else {
                // Debug in DEV only as Oauth errors appear in the redirect url
                // This will output the specific error message
                // Note Unit tests expect server error (correctly)
                // and will fail if this is set (during unit tests)
                String customOAuth2ErrorDescription = e.getErrorReason();

                commonExpressOAuthError =
                        new CommonExpressOAuthError(
                                OAuth2Error.SERVER_ERROR, customOAuth2ErrorDescription);
            }

            // Internal error state only sent in dev
            if ("dev".equals(environment)) {
                commonExpressOAuthError.setCriInternalErrorState(e.getErrorResponse());
            }

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    e.getStatusCode(), // Status Code determined by throw location
                    commonExpressOAuthError);
        } catch (SessionNotFoundException | SessionExpiredException e) {
            LOGGER.error(e.getMessage(), e);
            eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.FORBIDDEN,
                    new CommonExpressOAuthError(
                            OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage()));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);
            // Oauth compliant response
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
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

            eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        }
    }

    public boolean sessionIdIsNotUUID(String sessionId) {
        Pattern uuidRegex =
                Pattern.compile(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        return !uuidRegex.matcher(sessionId).matches();
    }

    private void saveAttempt(
            SessionItem sessionItem,
            DrivingPermitForm drivingPermitFormData,
            DocumentCheckVerificationResult result) {

        LOGGER.info("Generating authorization code...");
        sessionService.createAuthorizationCode(sessionItem);
        LOGGER.info("Authorization code generated...");

        LOGGER.info("Saving person identity...");
        BirthDate birthDate = new BirthDate();
        birthDate.setValue(drivingPermitFormData.getDateOfBirth());

        SharedClaims sharedClaims = new SharedClaims();
        sharedClaims.setAddresses(drivingPermitFormData.getAddresses());
        sharedClaims.setBirthDates(List.of(birthDate));
        sharedClaims.setNames(
                List.of(
                        RequestSentAuditHelper.mapNamesToCanonicalName(
                                drivingPermitFormData.getForenames(),
                                drivingPermitFormData.getSurname())));

        personIdentityService.savePersonIdentity(sessionItem.getSessionId(), sharedClaims);
        LOGGER.info("person identity saved.");

        final DocumentCheckResultItem documentCheckResultItem =
                mapVerificationResultToResultItem(sessionItem, result, drivingPermitFormData);

        LOGGER.info("Saving document check results...");
        documentCheckResultStorageService.saveDocumentCheckResult(documentCheckResultItem);
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
        if (issuingAuthority == IssuingAuthority.DVLA) {
            documentCheckResultItem.setIssueNumber(drivingPermitFormData.getIssueNumber());
        }

        documentCheckResultItem.setTransactionId(result.getTransactionId());

        // DocumentCheckResultItem TTL/Expiry
        documentCheckResultItem.setExpiry(
                Clock.systemUTC()
                        .instant()
                        .plus(documentCheckResultItemTtl, ChronoUnit.SECONDS)
                        .getEpochSecond());

        return documentCheckResultItem;
    }

    private IdentityVerificationService createIdentityVerificationService(
            ServiceFactory serviceFactory) {

        return new IdentityVerificationService(
                new FormDataValidator(), serviceFactory.getEventProbe());
    }

    private ThirdPartyAPIService selectThirdPartyAPIService(String licenseIssuer)
            throws OAuthErrorResponseException {

        IssuingAuthority issuingAuthority = IssuingAuthority.valueOf(licenseIssuer);

        switch (issuingAuthority) {
            case DVA -> {
                return thirdPartyAPIServiceFactory.getDvaThirdPartyAPIService();
            }
            case DVLA -> {
                return thirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService();
            }
            default -> {
                LOGGER.error(
                        "Issuer not known {} {} ",
                        issuingAuthority,
                        ErrorResponse.FAILED_TO_SELECT_THIRD_PARTY_API_SERVICE);

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_SELECT_THIRD_PARTY_API_SERVICE);
            }
        }
    }

    // Method used to prevent completed ok paths diverging
    private APIGatewayProxyResponseEvent lambdaCompletedOK(
            APIGatewayProxyResponseEvent responseEvent) {

        // Lambda Complete No Error
        eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

        return responseEvent;
    }

    private boolean determineVerificationRetryStatus(
            SessionItem sessionItem,
            DocumentCheckVerificationResult documentCheckVerificationResult,
            final int MAX_ATTEMPTS) {

        if (documentCheckVerificationResult.isExecutedSuccessfully()
                && documentCheckVerificationResult.isVerified()) {
            LOGGER.info("Document verified");
            eventProbe.counterMetric(
                    Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX
                            + sessionItem.getAttemptCount());

            return false;
        } else if (documentCheckVerificationResult.getAttemptCount() >= MAX_ATTEMPTS) {
            LOGGER.info(
                    "Ending document verification after {} attempts",
                    documentCheckVerificationResult.getAttemptCount());
            eventProbe.counterMetric(
                    Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED);

            return false;
        } else {
            LOGGER.info(
                    "Document not verified at attempt {}",
                    documentCheckVerificationResult.getAttemptCount());
            eventProbe.counterMetric(Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY);

            return true;
        }
    }

    private APIGatewayProxyResponseEvent generateExitResponseEvent(boolean canRetry) {
        DocumentVerificationResponse response = new DocumentVerificationResponse();
        response.setRetry(canRetry);

        return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, response);
    }

    private String retrieveSessionIdFromHeaders(Map<String, String> headers) {

        if (headers == null) {
            throw new SessionNotFoundException("Request had no headers");
        }

        String sessionId = headers.get("session_id");

        if (sessionId == null) {
            throw new SessionNotFoundException("Header session_id not found");
        }

        if (sessionIdIsNotUUID(sessionId)) {
            throw new SessionNotFoundException("Header session_id value not a UUID");
        }

        return sessionId;
    }
}
