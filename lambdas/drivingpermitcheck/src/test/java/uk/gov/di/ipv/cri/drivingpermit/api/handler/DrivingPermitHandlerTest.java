package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.util.TestDataCreator;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrivingPermitHandlerTest {
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private EventProbe mockEventProbe;
    @Mock private Context context;
    @Mock private PersonIdentityService personIdentityService;
    @Mock private SessionService sessionService;
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
                        sessionService,
                        dataStore,
                        configurationService,
                        auditService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException, OAuthHttpResponseExceptionWithErrorBody {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm = TestDataCreator.createTestDrivingPermitForm();

        DocumentCheckVerificationResult testDocumentVerificationResult =
                new DocumentCheckVerificationResult();
        testDocumentVerificationResult.setSuccess(true);
        testDocumentVerificationResult.setContraIndicators(new String[] {"A01"});
        testDocumentVerificationResult.setStrengthScore(1);
        testDocumentVerificationResult.setValidityScore(1);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        when(sessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(auditService)
                .sendAuditEvent(
                        eq(AuditEventType.THIRD_PARTY_REQUEST_ENDED),
                        any(AuditEventContext.class),
                        eq(""));

        when(mockIdentityVerificationService.verifyIdentity(drivingPermitForm))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals(
                "{\"success\":true,\"validationErrors\":null,\"error\":null,\"contraIndicators\":[\"A01\"],\"strengthScore\":1,\"validityScore\":1,\"transactionId\":null}",
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
        testDocumentVerificationResult.setSuccess(false);
        testDocumentVerificationResult.setError(errorMessage);
        testDocumentVerificationResult.setContraIndicators(new String[] {});
        testDocumentVerificationResult.setValidityScore(0);
        testDocumentVerificationResult.setStrengthScore(0);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        when(sessionService.validateSessionId(anyString())).thenReturn(sessionItem);

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
                        eq(AuditEventType.THIRD_PARTY_REQUEST_ENDED),
                        any(AuditEventContext.class),
                        eq(""));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        final String EXPECTED_ERROR = "{\"error_description\":\"error message\"}";
        assertEquals(EXPECTED_ERROR, responseEvent.getBody());
    }
}
