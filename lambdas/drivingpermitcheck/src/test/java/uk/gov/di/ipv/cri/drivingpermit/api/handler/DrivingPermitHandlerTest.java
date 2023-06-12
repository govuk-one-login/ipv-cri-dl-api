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
import software.amazon.awssdk.services.sqs.SqsClient;
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
import uk.gov.di.ipv.cri.drivingpermit.api.service.CRIServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.CommonServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.testdata.DocumentCheckVerificationResultDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK;

@ExtendWith(MockitoExtension.class)
class DrivingPermitHandlerTest {
    @Mock private CommonServiceFactory mockCommonServiceFactory;
    @Mock private CRIServiceFactory mockCRIServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private EventProbe mockEventProbe;
    @Mock private Context context;
    @Mock private PersonIdentityService personIdentityService;
    @Mock private SessionService mockSessionService;
    @Mock private DataStore dataStore;
    @Mock private ConfigurationService configurationService;
    @Mock private AuditService auditService;
    @Mock private SqsClient sqsClient;
    private DrivingPermitHandler drivingPermitHandler;

    @BeforeEach
    void setup() {
        when(mockCRIServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.drivingPermitHandler =
                new DrivingPermitHandler(
                        mockCommonServiceFactory,
                        mockCRIServiceFactory,
                        personIdentityService,
                        mockSessionService,
                        dataStore);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        UUID sessionId = UUID.randomUUID();

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);
        DocumentCheckResultItem documentCheckResultItem =
                generateDocCheckResultItem(
                        sessionId, drivingPermitForm, testDocumentVerificationResult);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", sessionId.toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(sessionId);
        sessionItem.setAttemptCount(0); // No previous attempt
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        when(configurationService.getDocumentCheckItemExpirationEpoch()).thenReturn(1000L);

        when(mockCommonServiceFactory.getEventProbe()).thenReturn(new EventProbe());
        when(mockCommonServiceFactory.getAuditService()).thenReturn(auditService);

        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 1);
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

        verify(dataStore).create(documentCheckResultItem);
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
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        when(mockCommonServiceFactory.getEventProbe()).thenReturn(new EventProbe());
        when(mockCommonServiceFactory.getAuditService()).thenReturn(auditService);

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
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        when(mockCommonServiceFactory.getEventProbe()).thenReturn(new EventProbe());
        when(mockCommonServiceFactory.getAuditService()).thenReturn(auditService);
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
        when(mockCommonServiceFactory.getEventProbe()).thenReturn(new EventProbe());
        when(mockCommonServiceFactory.getAuditService()).thenReturn(auditService);
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

        verify(auditService, never())
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        verify(auditService, never())
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        when(mockCommonServiceFactory.getEventProbe()).thenReturn(new EventProbe());
        when(mockCommonServiceFactory.getAuditService()).thenReturn(auditService);

        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        final String EXPECTED_ERROR =
                "{\"code\":1025,\"message\":\"Request failed due to a server error\",\"errorSummary\":\"1025: Request failed due to a server error\"}";
        assertEquals(EXPECTED_ERROR, responseEvent.getBody());
    }

    private DocumentCheckResultItem generateDocCheckResultItem(
            UUID sessionId,
            DrivingPermitForm drivingPermitForm,
            DocumentCheckVerificationResult testDocumentVerificationResult) {
        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();
        documentCheckResultItem.setDocumentNumber(drivingPermitForm.getDrivingLicenceNumber());
        documentCheckResultItem.setCheckMethod(
                testDocumentVerificationResult.getCheckDetails().getCheckMethod());
        documentCheckResultItem.setActivityFrom(drivingPermitForm.getIssueDate().toString());
        documentCheckResultItem.setExpiry(1000L);
        documentCheckResultItem.setExpiryDate(drivingPermitForm.getExpiryDate().toString());
        documentCheckResultItem.setIdentityCheckPolicy(
                testDocumentVerificationResult.getCheckDetails().getIdentityCheckPolicy());
        documentCheckResultItem.setIssueDate(drivingPermitForm.getIssueDate().toString());
        documentCheckResultItem.setIssuedBy("DVLA");
        documentCheckResultItem.setActivityHistoryScore(
                testDocumentVerificationResult.getActivityHistoryScore());
        documentCheckResultItem.setIssueNumber(drivingPermitForm.getIssueNumber());
        documentCheckResultItem.setStrengthScore(testDocumentVerificationResult.getStrengthScore());
        documentCheckResultItem.setValidityScore(testDocumentVerificationResult.getValidityScore());
        documentCheckResultItem.setSessionId(sessionId);
        documentCheckResultItem.setContraIndicators(List.of("A01"));
        return documentCheckResultItem;
    }

    private static boolean[] getDocumentVerifiedStatus() {
        return new boolean[] {true, false};
    }
}
