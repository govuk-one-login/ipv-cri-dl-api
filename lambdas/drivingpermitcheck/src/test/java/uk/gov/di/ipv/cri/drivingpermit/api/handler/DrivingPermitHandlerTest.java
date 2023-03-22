package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.testdata.DocumentCheckVerificationResultDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK;

@ExtendWith(MockitoExtension.class)
class DrivingPermitHandlerTest {
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private EventProbe mockEventProbe;
    @Mock private Context context;
    @Mock private PersonIdentityService personIdentityService;
    @Mock private SessionService mockSessionService;
    @Mock private DataStore dataStore;
    @Mock private ConfigurationService configurationService;
    @Mock private AuditService auditService;
    private DrivingPermitHandler drivingPermitHandler;

    @BeforeEach
    void setup() {
        when(mockServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.drivingPermitHandler =
                new DrivingPermitHandler(
                        mockServiceFactory,
                        mockObjectMapper,
                        mockEventProbe,
                        personIdentityService,
                        mockSessionService,
                        dataStore,
                        configurationService,
                        auditService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        sessionItem.setAttemptCount(0); // No previous attempt
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(auditService)
                .sendAuditEvent(
                        eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class), eq(""));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 1);
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @ParameterizedTest
    @MethodSource("getDocumentVerifiedStatus")
    void handleResponseShouldReturnCorrectResponsesForAttemptOneVerifiedStatus(
            boolean documentVerified)
            throws IOException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);

        // Verified Status
        testDocumentVerificationResult.setVerified(documentVerified);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        sessionItem.setAttemptCount(0); // No previous attempt
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(auditService)
                .sendAuditEvent(
                        eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class), eq(""));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        if (documentVerified) {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 1);
            inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

            assertNotNull(responseEvent);
            assertEquals(200, responseEvent.getStatusCode());
            assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
        } else {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY);
            inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

            assertNotNull(responseEvent);
            assertEquals(200, responseEvent.getStatusCode());
            assertEquals("{\"redirectUrl\":null,\"retry\":true}", responseEvent.getBody());
        }
    }

    @ParameterizedTest
    @MethodSource("getDocumentVerifiedStatus")
    void handleResponseShouldReturnCorrectResponsesForVerifiedStatus(boolean documentVerified)
            throws IOException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);

        // Verified Status
        testDocumentVerificationResult.setVerified(documentVerified);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        sessionItem.setAttemptCount(1); // One previous attempt
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(auditService)
                .sendAuditEvent(
                        eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class), eq(""));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        if (documentVerified) {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 2);
        } else {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED);
        }
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnServerErrorResponseWhenAttemptsIsOverAttemptMax() {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);

        // Unverified
        testDocumentVerificationResult.setVerified(false);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        sessionItem.setAttemptCount(2); // Two previous attempts
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        assertEquals(
                "{\"code\":1026,\"message\":\"Too many retry attempts made\"}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        String errorMessage = "error message";
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();
        DocumentCheckVerificationResult testDocumentVerificationResult =
                new DocumentCheckVerificationResult();
        testDocumentVerificationResult.setExecutedSuccessfully(false);
        testDocumentVerificationResult.setError(errorMessage);
        testDocumentVerificationResult.setContraIndicators(null);
        testDocumentVerificationResult.setValidityScore(0);
        testDocumentVerificationResult.setStrengthScore(0);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        doNothing()
                .when(auditService)
                .sendAuditEvent(
                        eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class), eq(""));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");

        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        final String EXPECTED_ERROR =
                "{\"code\":1025,\"message\":\"Request failed due to a server error\",\"errorSummary\":\"1025: Request failed due to a server error\"}";
        assertEquals(EXPECTED_ERROR, responseEvent.getBody());
    }

    private static boolean[] getDocumentVerifiedStatus() {
        return new boolean[] {true, false};
    }
}
