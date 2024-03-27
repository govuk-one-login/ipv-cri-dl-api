package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.testdata.DocumentCheckVerificationResultDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.service.DocumentCheckResultStorageService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TTL_PARAMETER;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_USER_REDIRECTED_ATTEMPTS_OVER_MAX;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DrivingPermitHandlerTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ObjectMapper mockObjectMapper;
    @Mock private EventProbe mockEventProbe;
    @Mock private SessionService mockSessionService;
    @Mock private AuditService mockAuditService;

    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private DocumentCheckResultStorageService mockDocumentCheckResultStorageService;
    @Mock private ParameterStoreService mockParameterStoreService;

    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ThirdPartyAPIServiceFactory mockThirdPartyAPIServiceFactory;

    @Mock private IdentityVerificationService mockIdentityVerificationService;

    @Mock private Context context;

    @Mock private DvaThirdPartyDocumentGateway mockDvaThirdPartyDocumentGateway;
    @Mock private DvlaThirdPartyDocumentGateway mockDvlaThirdPartyDocumentGateway;

    private DrivingPermitHandler drivingPermitHandler;

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");
        // EnvVar feature toggles
        environmentVariables.set("DVA_PERFORMANCE_STUB_IN_USE", "false");
        environmentVariables.set("LOG_DVA_RESPONSE", "false");
        environmentVariables.set("DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG", "false");

        when(mockServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
        when(mockServiceFactory.getSessionService()).thenReturn(mockSessionService);
        when(mockServiceFactory.getAuditService()).thenReturn(mockAuditService);

        when(mockServiceFactory.getPersonIdentityService()).thenReturn(mockPersonIdentityService);
        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);

        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.COMMON_API, DOCUMENT_CHECK_RESULT_TTL_PARAMETER))
                .thenReturn(String.valueOf(1000L));

        when(mockServiceFactory.getDocumentCheckResultStorageService())
                .thenReturn(mockDocumentCheckResultStorageService);

        this.drivingPermitHandler =
                new DrivingPermitHandler(
                        mockServiceFactory,
                        mockThirdPartyAPIServiceFactory,
                        mockIdentityVerificationService);
    }

    @ParameterizedTest
    @CsvSource({"DVA", "DVLA"})
    void handleResponseShouldReturnOkResponseWhenValidInputProvided(String issuingAuthority)
            throws IOException, SqsException, OAuthErrorResponseException {
        String testRequestBody = "request body";
        UUID sessionId = UUID.randomUUID();

        DrivingPermitForm drivingPermitForm =
                DrivingPermitFormTestDataGenerator.generate(
                        IssuingAuthority.valueOf(issuingAuthority));

        DocumentCheckVerificationResult testDocumentVerificationResult =
                DocumentCheckVerificationResultDataGenerator.generate(drivingPermitForm);

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
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        switch (issuingAuthority) {
            case "DVA" -> when(mockThirdPartyAPIServiceFactory.getDvaThirdPartyAPIService())
                    .thenReturn(mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                    .thenReturn(mockDvlaThirdPartyDocumentGateway);
        }

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");

        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe)
                .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 1);
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        switch (issuingAuthority) {
            case "DVA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvlaThirdPartyDocumentGateway);
        }

        verify(mockDocumentCheckResultStorageService)
                .saveDocumentCheckResult(any(DocumentCheckResultItem.class));
        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @ParameterizedTest
    @CsvSource({
        "DVA, false",
        "DVA, true",
        "DVLA, false",
        "DVLA, true",
    })
    void handleResponseShouldReturnCorrectResponsesForAttemptOneVerifiedStatus(
            String issuingAuthority, boolean documentVerified)
            throws IOException, SqsException, OAuthErrorResponseException {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm =
                DrivingPermitFormTestDataGenerator.generate(
                        IssuingAuthority.valueOf(issuingAuthority));

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
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        switch (issuingAuthority) {
            case "DVA" -> when(mockThirdPartyAPIServiceFactory.getDvaThirdPartyAPIService())
                    .thenReturn(mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                    .thenReturn(mockDvlaThirdPartyDocumentGateway);
        }

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION), anyDouble());

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
        verifyNoMoreInteractions(mockEventProbe);

        switch (issuingAuthority) {
            case "DVA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvlaThirdPartyDocumentGateway);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "DVA, false",
        "DVA, true",
        "DVLA, false",
        "DVLA, true",
    })
    void handleResponseShouldReturnCorrectResponsesForVerifiedStatus(
            String issuingAuthority, boolean documentVerified)
            throws IOException, SqsException, OAuthErrorResponseException {
        String testRequestBody = "request body";
        DrivingPermitForm drivingPermitForm =
                DrivingPermitFormTestDataGenerator.generate(
                        IssuingAuthority.valueOf(issuingAuthority));

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
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        switch (issuingAuthority) {
            case "DVA" -> when(mockThirdPartyAPIServiceFactory.getDvaThirdPartyAPIService())
                    .thenReturn(mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                    .thenReturn(mockDvlaThirdPartyDocumentGateway);
        }

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION), anyDouble());

        if (documentVerified) {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 2);
        } else {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED);
        }

        switch (issuingAuthority) {
            case "DVA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvaThirdPartyDocumentGateway);
            case "DVLA" -> verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockDvlaThirdPartyDocumentGateway);
        }

        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @Test
    void handleResponseShouldRedirectUserWhenAttemptsIsOverAttemptMax() {
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

        verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        verify(mockEventProbe)
                .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_USER_REDIRECTED_ATTEMPTS_OVER_MAX);
        verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException, SqsException, OAuthErrorResponseException {
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

        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_DRIVING_PERMIT_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(responseEvent.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.SERVER_ERROR, OAuth2Error.SERVER_ERROR.getDescription());

        // Assert CommonExpress OAuth error format
        assertEquals(
                "oauth_error",
                responseTreeRootNode.fieldNames().next().toString()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
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

    @ParameterizedTest
    @CsvSource({
        "DVA, DvaThirdPartyDocumentGateway",
        "DVLA, DvlaThirdPartyDocumentGateway",
    })
    void shouldSelectCorrectThirdPartyAPIServiceForEachIssuerAndFeatureToggle(
            String licenseIssuer, String expectedServiceName)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        IssuingAuthority issuingAuthority = IssuingAuthority.valueOf(licenseIssuer);

        // This section mocks the gets (if called) from the ThirdPartyAPIServiceFactory
        if (IssuingAuthority.DVA == issuingAuthority) {
            when(mockThirdPartyAPIServiceFactory.getDvaThirdPartyAPIService())
                    .thenReturn(mockDvaThirdPartyDocumentGateway);
        } else if (IssuingAuthority.DVLA == issuingAuthority) {
            when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                    .thenReturn(mockDvlaThirdPartyDocumentGateway);
        } else {
            throw new IllegalThreadStateException();
        }

        DrivingPermitHandler spyHandler;

        // "selectThirdPartyAPIService" is a private helper method  used to configure the
        // CRI with the chosen ThirdPartyAPIService.
        // The following uses reflection to unlock the method and confirm its behaviour
        DrivingPermitHandler spyTarget =
                new DrivingPermitHandler(
                        mockServiceFactory,
                        mockThirdPartyAPIServiceFactory,
                        mockIdentityVerificationService);

        spyHandler = spy(spyTarget);

        Method privateDetermineThirdPartyAPIServiceMethod =
                DrivingPermitHandler.class.getDeclaredMethod(
                        "selectThirdPartyAPIService", String.class);
        privateDetermineThirdPartyAPIServiceMethod.setAccessible(true);

        // Call the private method and capture result
        ThirdPartyAPIService thirdPartyAPIService =
                (ThirdPartyAPIService)
                        privateDetermineThirdPartyAPIServiceMethod.invoke(
                                spyHandler, licenseIssuer);

        assertEquals(expectedServiceName, thirdPartyAPIService.getClass().getSimpleName());
    }
}
