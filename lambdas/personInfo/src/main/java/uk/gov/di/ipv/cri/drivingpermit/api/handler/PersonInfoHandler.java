package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.exception.SessionExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.SessionNotFoundException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.logging.LoggingSupport;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static uk.gov.di.ipv.cri.common.library.error.ErrorResponse.SESSION_NOT_FOUND;

public class PersonInfoHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // We need this first and static for it to be created as soon as possible during function init
    private static final long FUNCTION_INIT_START_TIME_MILLISECONDS = System.currentTimeMillis();

    private static final Logger LOGGER = LogManager.getLogger();
    private EventProbe eventProbe;

    // For capturing run-time function init metric
    private long functionInitMetricLatchedValue = 0;
    private boolean functionInitMetricCaptured = false;

    private SessionService sessionService;
    private PersonIdentityService personIdentityService;

    @ExcludeFromGeneratedCoverageReport
    public PersonInfoHandler(ServiceFactory serviceFactory) {
        initializeLambdaServices(serviceFactory);
    }

    @ExcludeFromGeneratedCoverageReport
    public PersonInfoHandler() {
        ServiceFactory serviceFactory = new ServiceFactory();

        initializeLambdaServices(serviceFactory);
    }

    private void initializeLambdaServices(ServiceFactory serviceFactory) {
        this.eventProbe = serviceFactory.getEventProbe();

        this.sessionService = serviceFactory.getSessionService();

        this.personIdentityService = serviceFactory.getPersonIdentityService();

        // Runtime/SnapStart function init duration
        functionInitMetricLatchedValue =
                System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
    }

    @Override
    @Metrics(captureColdStart = true)
    @Logging(correlationIdPath = CorrelationIdPathConstants.EVENT_BRIDGE)
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
                        Definitions.LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION,
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

            LOGGER.info("Handling Person Info request");
            final String sessionId = retrieveSessionIdFromHeaders(input.getHeaders());

            // QuerySessionTable to get sessionItem
            LOGGER.info("Extracting session from header sessionId {}", sessionId);
            SessionItem sessionItem = sessionService.validateSessionId(sessionId);
            LOGGER.info(
                    "Persistent Logging keys now attached to sessionId {}",
                    sessionItem.getSessionId());

            // Get context from sessionItem
            String coreContext = sessionItem.getContext();
            LOGGER.info("Context Value = {}", coreContext);

            if (coreContext != null) {
                if (coreContext.equalsIgnoreCase("check_details")) {

                    LOGGER.info("Querying PersonIdentity table with sessionId...");
                    LOGGER.debug("SessionID = {}", sessionId);
                    PersonIdentityDetailed personIdentityDetailed =
                            personIdentityService.getPersonIdentityDetailed(
                                    UUID.fromString(sessionId));
                    if (null == personIdentityDetailed) {
                        String message =
                                String.format(
                                        "Could not retrieve person identity for session %s",
                                        sessionItem.getSessionId());

                        LOGGER.error(message);

                        eventProbe.counterMetric(
                                Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);
                        return ApiGatewayResponseGenerator.proxyJsonResponse(
                                HttpStatusCode.BAD_REQUEST,
                                new CommonExpressOAuthError(
                                        OAuth2Error.INVALID_REQUEST, "Missing Shared Claims"));
                    }

                    LOGGER.info("PersonIdentity Retrieved");
                    eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);

                    return ApiGatewayResponseGenerator.proxyJsonResponse(
                            HttpStatusCode.OK, personIdentityDetailed);
                } else {
                    LOGGER.error("Invalid Context field value - {}", coreContext);
                    eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);
                    return ApiGatewayResponseGenerator.proxyJsonResponse(
                            HttpStatusCode.BAD_REQUEST,
                            new CommonExpressOAuthError(
                                    OAuth2Error.INVALID_REQUEST, "Invalid Context field value"));
                }
            }

            APIGatewayProxyResponseEvent emptyResponse = new APIGatewayProxyResponseEvent();
            emptyResponse.setStatusCode(HttpStatusCode.NO_CONTENT);
            LOGGER.info("Context value null, returning empty response");
            eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
            return emptyResponse;
        } catch (SessionNotFoundException | SessionExpiredException e) {
            LOGGER.error(e.getMessage(), e);
            eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.FORBIDDEN,
                    new CommonExpressOAuthError(
                            OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage()));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);
            // Oauth compliant response
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        } catch (AwsServiceException ex) {
            LOGGER.error(
                    "Exception While handling Lambda {} {}",
                    context.getFunctionName(),
                    ex.getClass());
            eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);

            LOGGER.debug(ex.getMessage(), ex);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        } catch (Exception e) {
            LOGGER.error("Exception while handling lambda {}", e.getClass());
            eventProbe.counterMetric(Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);

            LOGGER.debug(e.getMessage(), e);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        }
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

    public boolean sessionIdIsNotUUID(String sessionId) {
        Pattern uuidRegex =
                Pattern.compile(
                        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        return !uuidRegex.matcher(sessionId).matches();
    }
}
