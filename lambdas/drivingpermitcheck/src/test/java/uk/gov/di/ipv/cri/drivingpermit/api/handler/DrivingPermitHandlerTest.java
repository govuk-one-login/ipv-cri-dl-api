package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.di.ipv.cri.drivingpermit.api.service.DrivingPermitServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.testdata.DocumentCheckVerificationResultDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;

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
import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVA;
import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVLA;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_DRIVING_PERMIT_CHECK_USER_REDIRECTED_ATTEMPTS_OVER_MAX;

@ExtendWith(MockitoExtension.class)
class DrivingPermitHandlerTest {
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private EventProbe mockEventProbe;
    @Mock private SessionService mockSessionService;
    @Mock private AuditService mockAuditService;

    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private DataStore<DocumentCheckResultItem> mockDataStore;
    @Mock private ConfigurationService mockConfigurationService;

    @Mock private DrivingPermitServiceFactory mockDrivingPermitServiceFactory;
    @Mock private ThirdPartyAPIServiceFactory mockThirdPartyAPIServiceFactory;
    @Mock private ThirdPartyAPIService mockThirdPartyAPIService;

    @Mock private IdentityVerificationService mockIdentityVerificationService;

    @Mock private Context context;

    // Only used in SelectCorrectThirdPartyAPIService Test
    @Mock private DcsThirdPartyDocumentGateway mockDcsThirdPartyDocumentGateway;
    @Mock private DvaThirdPartyDocumentGateway mockDvaThirdPartyDocumentGateway;
    @Mock private DvlaThirdPartyDocumentGateway mockDvlaThirdPartyDocumentGateway;

    private DrivingPermitHandler drivingPermitHandler;

    private ObjectMapper realObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setup() {

        when(mockDrivingPermitServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
        when(mockDrivingPermitServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
        when(mockDrivingPermitServiceFactory.getSessionService()).thenReturn(mockSessionService);
        when(mockDrivingPermitServiceFactory.getAuditService()).thenReturn(mockAuditService);

        when(mockDrivingPermitServiceFactory.getPersonIdentityService())
                .thenReturn(mockPersonIdentityService);

        when(mockDrivingPermitServiceFactory.getConfigurationService())
                .thenReturn(mockConfigurationService);
        when(mockDrivingPermitServiceFactory.getDataStore()).thenReturn(mockDataStore);

        this.drivingPermitHandler =
                new DrivingPermitHandler(
                        mockDrivingPermitServiceFactory,
                        mockThirdPartyAPIServiceFactory,
                        mockIdentityVerificationService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException, OAuthErrorResponseException {
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
        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        // Choose API
        when(mockConfigurationService.getDvlaDirectEnabled()).thenReturn(true);
        when(mockDvlaThirdPartyDocumentGateway.getServiceName())
                .thenReturn(DvlaThirdPartyDocumentGateway.class.getSimpleName());
        when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                .thenReturn(mockDvlaThirdPartyDocumentGateway);

        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
                .thenReturn(testDocumentVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        when(mockConfigurationService.getDocumentCheckItemExpirationEpoch()).thenReturn(1000L);

        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX + 1);
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);

        verify(mockIdentityVerificationService)
                .verifyIdentity(drivingPermitForm, mockDvlaThirdPartyDocumentGateway);
        verify(mockDataStore).create(documentCheckResultItem);
        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
    }

    @ParameterizedTest
    @MethodSource("getDocumentVerifiedStatus")
    void handleResponseShouldReturnCorrectResponsesForAttemptOneVerifiedStatus(
            boolean documentVerified)
            throws IOException, SqsException, OAuthErrorResponseException {
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
        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(mockConfigurationService.getDvaDirectEnabled()).thenReturn(false);
        when(mockThirdPartyAPIService.getServiceName())
                .thenReturn(DcsThirdPartyDocumentGateway.class.getSimpleName());
        when(mockThirdPartyAPIServiceFactory.getDcsThirdPartyAPIService())
                .thenReturn(mockThirdPartyAPIService);

        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
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
            verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockThirdPartyAPIService);

            assertNotNull(responseEvent);
            assertEquals(200, responseEvent.getStatusCode());
            assertEquals("{\"redirectUrl\":null,\"retry\":false}", responseEvent.getBody());
        } else {
            inOrder.verify(mockEventProbe)
                    .counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY);
            inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
            verify(mockIdentityVerificationService)
                    .verifyIdentity(drivingPermitForm, mockThirdPartyAPIService);

            assertNotNull(responseEvent);
            assertEquals(200, responseEvent.getStatusCode());
            assertEquals("{\"redirectUrl\":null,\"retry\":true}", responseEvent.getBody());
        }
    }

    @ParameterizedTest
    @MethodSource("getDocumentVerifiedStatus")
    void handleResponseShouldReturnCorrectResponsesForVerifiedStatus(boolean documentVerified)
            throws IOException, SqsException, OAuthErrorResponseException {
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
        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockObjectMapper.readValue(testRequestBody, DrivingPermitForm.class))
                .thenReturn(drivingPermitForm);

        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.RESPONSE_RECEIVED), any(AuditEventContext.class));

        when(mockConfigurationService.getDvaDirectEnabled()).thenReturn(false);
        when(mockThirdPartyAPIService.getServiceName())
                .thenReturn(DcsThirdPartyDocumentGateway.class.getSimpleName());
        when(mockThirdPartyAPIServiceFactory.getDcsThirdPartyAPIService())
                .thenReturn(mockThirdPartyAPIService);

        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(mockIdentityVerificationService.verifyIdentity(
                        any(DrivingPermitForm.class), any(ThirdPartyAPIService.class)))
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
        verify(mockIdentityVerificationService)
                .verifyIdentity(drivingPermitForm, mockThirdPartyAPIService);

        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK);
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
        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

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

        when(mockConfigurationService.getMaxAttempts()).thenReturn(2);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                drivingPermitHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR);

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
        // dvaEnabled, dvlaEnabled, licenseIssuer, expected api

        // Only one direct api is enabled (ensure api feature flags are
        // independent)
        "false, true, DVA, DcsThirdPartyDocumentGateway",
        "true, false, DVLA, DcsThirdPartyDocumentGateway",
        "true, false, DVA, DvaThirdPartyDocumentGateway",
        "false, true, DVLA, DvlaThirdPartyDocumentGateway",

        // Failsafe for no header present
        "false, false, DVA, DcsThirdPartyDocumentGateway",
        "false, false, DVLA, DcsThirdPartyDocumentGateway",
        "true, true, DVA, DvaThirdPartyDocumentGateway",
        "true, true, DVLA, DvlaThirdPartyDocumentGateway",
    })
    void shouldSelectCorrectThirdPartyAPIServiceForEachIssuerAndFeatureToggle(
            boolean dvaEnabled,
            boolean dvlaEnabled,
            String licenseIssuer,
            String expectedServiceName)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        IssuingAuthority issuingAuthority = IssuingAuthority.valueOf(licenseIssuer);

        // This section mocks the gets (if called) from the ThirdPartyAPIServiceFactory
        if (dvaEnabled && (DVA == issuingAuthority)) {
            when(mockThirdPartyAPIServiceFactory.getDvaThirdPartyAPIService())
                    .thenReturn(mockDvaThirdPartyDocumentGateway);
        } else if (dvlaEnabled && (DVLA == issuingAuthority)) {
            when(mockThirdPartyAPIServiceFactory.getDvlaThirdPartyAPIService())
                    .thenReturn(mockDvlaThirdPartyDocumentGateway);
        } else {
            when(mockThirdPartyAPIServiceFactory.getDcsThirdPartyAPIService())
                    .thenReturn(mockDcsThirdPartyDocumentGateway);
        }

        DrivingPermitHandler spyHandler;

        // "selectThirdPartyAPIService" is a private helper method  used to configure the
        // CRI with the chosen ThirdPartyAPIService.
        // The following uses reflection to unlock the method and confirm its behaviour
        DrivingPermitHandler spyTarget =
                new DrivingPermitHandler(
                        mockDrivingPermitServiceFactory,
                        mockThirdPartyAPIServiceFactory,
                        mockIdentityVerificationService);

        spyHandler = spy(spyTarget);

        Method privateDetermineThirdPartyAPIServiceMethod =
                DrivingPermitHandler.class.getDeclaredMethod(
                        "selectThirdPartyAPIService", boolean.class, boolean.class, String.class);
        privateDetermineThirdPartyAPIServiceMethod.setAccessible(true);

        // Call the private method and capture result
        ThirdPartyAPIService thirdPartyAPIService =
                (ThirdPartyAPIService)
                        privateDetermineThirdPartyAPIServiceMethod.invoke(
                                spyHandler, dvaEnabled, dvlaEnabled, licenseIssuer);

        assertEquals(expectedServiceName, thirdPartyAPIService.getClass().getSimpleName());
    }
}
