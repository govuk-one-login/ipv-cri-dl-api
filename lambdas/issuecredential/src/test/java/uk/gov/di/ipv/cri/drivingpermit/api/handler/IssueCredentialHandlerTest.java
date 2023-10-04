package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpResponse;
import testdata.DocumentCheckTestDataGenerator;
import testdata.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.VCISSDocumentCheckAuditExtension;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DocumentCheckRetrievalService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.PersonIdentityDetailedHelperMapper;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DRIVING_PERMIT_CI_PREFIX;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class IssueCredentialHandlerTest {
    public static final String SUBJECT = "subject";
    @Mock private Context context;
    @Mock private VerifiableCredentialService mockVerifiableCredentialService;
    @Mock private SessionService mockSessionService;
    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private DocumentCheckRetrievalService mockDocumentCheckRetrievalService;
    @Mock private EventProbe mockEventProbe;
    @Mock private AuditService mockAuditService;

    private IssueCredentialHandler handler;
    @SystemStub private EnvironmentVariables environmentVariables;

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        this.handler =
                new IssueCredentialHandler(
                        mockVerifiableCredentialService,
                        mockSessionService,
                        mockEventProbe,
                        mockAuditService,
                        mockPersonIdentityService,
                        mockDocumentCheckRetrievalService);
    }

    @Test
    void shouldReturn200OkWhenIssueCredentialRequestIsValid() throws JOSEException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));
        setRequestBodyAsPlainJWT(event);

        var personIdentityDetailed =
                PersonIdentityDetailedHelperMapper.drivingPermitFormDataToAuditRestrictedFormat(
                        DrivingPermitFormTestDataGenerator.generate());
        SessionItem sessionItem = new SessionItem();
        DocumentCheckResultItem resultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), DrivingPermitFormTestDataGenerator.generate());

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(personIdentityDetailed);
        when(mockDocumentCheckRetrievalService.getDocumentCheckResult(sessionItem.getSessionId()))
                .thenReturn(resultItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), resultItem, personIdentityDetailed))
                .thenReturn(mock(SignedJWT.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockDocumentCheckRetrievalService)
                .getDocumentCheckResult(sessionItem.getSessionId());
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), resultItem, personIdentityDetailed);
        verify(mockAuditService)
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));
        verify(mockEventProbe).counterMetric(LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK);
        verify(mockEventProbe)
                .counterMetric(DRIVING_PERMIT_CI_PREFIX + resultItem.getContraIndicators().get(0));
        assertEquals(
                ContentType.APPLICATION_JWT.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowJOSEExceptionWhenGenerateVerifiableCredentialIsMalformed()
            throws JOSEException, SqsException, JsonProcessingException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));
        setRequestBodyAsPlainJWT(event);

        var unExpectedJOSEException = new JOSEException("Unexpected JOSE object type: JWSObject");

        var personIdentityDetailed =
                PersonIdentityDetailedHelperMapper.drivingPermitFormDataToAuditRestrictedFormat(
                        DrivingPermitFormTestDataGenerator.generate());

        SessionItem sessionItem = new SessionItem();
        DocumentCheckResultItem resultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), DrivingPermitFormTestDataGenerator.generate());

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(personIdentityDetailed);
        when(mockDocumentCheckRetrievalService.getDocumentCheckResult(sessionItem.getSessionId()))
                .thenReturn(resultItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), resultItem, personIdentityDetailed))
                .thenThrow(unExpectedJOSEException);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockDocumentCheckRetrievalService)
                .getDocumentCheckResult(sessionItem.getSessionId());
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), resultItem, personIdentityDetailed);
        verify(mockEventProbe).counterMetric(LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);
        // There is a CI in the test result, we check we do not record CI metrics for a VC
        // generation Error
        verify(mockEventProbe, never())
                .counterMetric(DRIVING_PERMIT_CI_PREFIX + resultItem.getContraIndicators().get(0));
        verifyNoMoreInteractions(mockVerifiableCredentialService);
        verify(mockAuditService, never())
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));
        Map responseBody = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR.getCode(), responseBody.get("code"));
        assertEquals(
                ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR.getMessage(),
                responseBody.get("message"));
    }

    @Test
    void shouldThrowCredentialRequestExceptionWhenAuthorizationHeaderIsNotSupplied()
            throws SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);
        verify(mockEventProbe).counterMetric(LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);
        verify(mockAuditService, never())
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));
        assertEquals(
                ContentType.APPLICATION_JSON.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowAWSExceptionWhenAServerErrorOccursRetrievingASessionItemWithAccessToken()
            throws JsonProcessingException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));

        setRequestBodyAsPlainJWT(event);

        AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder()
                        .errorCode("")
                        .sdkHttpResponse(
                                SdkHttpResponse.builder()
                                        .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                                        .build())
                        .errorMessage("AWS DynamoDbException Occurred")
                        .build();

        when(mockSessionService.getSessionByAccessToken(accessToken))
                .thenThrow(
                        AwsServiceException.builder()
                                .statusCode(500)
                                .awsErrorDetails(awsErrorDetails)
                                .build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockPersonIdentityService, never()).getPersonIdentityDetailed(UUID.randomUUID());
        verify(mockAuditService, never())
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));
        verify(mockEventProbe).counterMetric(LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);
        verify(mockAuditService, never()).sendAuditEvent((AuditEventType) any());
        String responseBody = new ObjectMapper().readValue(response.getBody(), String.class);
        assertEquals(awsErrorDetails.sdkHttpResponse().statusCode(), response.getStatusCode());
        assertEquals(awsErrorDetails.errorMessage(), responseBody);
    }

    @Test
    void shouldThrowAWSExceptionWhenAServerErrorOccursDuringRetrievingPersonIdentityWithSessionId()
            throws JsonProcessingException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));

        setRequestBodyAsPlainJWT(event);

        AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder()
                        .errorCode("")
                        .sdkHttpResponse(
                                SdkHttpResponse.builder()
                                        .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                                        .build())
                        .errorMessage("AWS DynamoDbException Occurred")
                        .build();

        SessionItem sessionItem = new SessionItem();
        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(sessionItem.getSessionId()))
                .thenThrow(
                        AwsServiceException.builder()
                                .statusCode(500)
                                .awsErrorDetails(awsErrorDetails)
                                .build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockPersonIdentityService).getPersonIdentityDetailed(sessionItem.getSessionId());
        verify(mockAuditService, never())
                .sendAuditEvent(
                        eq(AuditEventType.VC_ISSUED),
                        any(AuditEventContext.class),
                        any(VCISSDocumentCheckAuditExtension.class));
        verify(mockEventProbe).counterMetric(LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);
        String responseBody = new ObjectMapper().readValue(response.getBody(), String.class);
        assertEquals(awsErrorDetails.sdkHttpResponse().statusCode(), response.getStatusCode());
        assertEquals(awsErrorDetails.errorMessage(), responseBody);
    }

    private void setRequestBodyAsPlainJWT(APIGatewayProxyRequestEvent event) {
        String requestJWT =
                new PlainJWT(
                                new JWTClaimsSet.Builder()
                                        .claim(JWTClaimNames.SUBJECT, SUBJECT)
                                        .build())
                        .serialize();

        event.setBody(requestJWT);
    }
}
