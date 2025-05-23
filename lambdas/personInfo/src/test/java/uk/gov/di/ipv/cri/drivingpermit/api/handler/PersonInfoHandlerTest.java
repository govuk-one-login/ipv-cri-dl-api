package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.util.PersonIdentityDetailedTestDataGenerator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.common.library.error.ErrorResponse.SESSION_NOT_FOUND;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class PersonInfoHandlerTest {
    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock ServiceFactory mockServiceFactory;

    @Mock private EventProbe mockEventProbe;

    @Mock private SessionService mockSessionService;

    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private Context context;

    private PersonInfoHandler handler;

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        mockServiceFactoryBehaviour();

        this.handler = new PersonInfoHandler(mockServiceFactory);
    }

    @Test
    void shouldReturn200OkWhenPersonInfoRequestIsValid() throws JsonProcessingException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        PersonIdentityDetailed savedPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate("DVLA");

        SessionItem sessionItem = new SessionItem();
        sessionItem.setContext("check_details");

        when(mockSessionService.validateSessionId(sessionId)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(savedPersonIdentityDetailed);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).validateSessionId(sessionId);
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(response.getBody());
        JsonNode drivingPermitNode = responseTreeRootNode.get("drivingPermit");

        assertEquals(
                savedPersonIdentityDetailed.getDrivingPermits().get(0).getPersonalNumber(),
                drivingPermitNode.get(0).get("personalNumber").textValue()); // error description

        assertEquals(
                ContentType.APPLICATION_JSON.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn400NoContentWhenContextIsIncorrectValue() throws JsonProcessingException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        SessionItem sessionItem = new SessionItem();
        sessionItem.setContext("international_address");

        when(mockSessionService.validateSessionId(sessionId)).thenReturn(sessionItem);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).validateSessionId(sessionId);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_ERROR);
        verifyNoMoreInteractions(mockEventProbe);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(response.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.INVALID_REQUEST, "Invalid Context field value");

        assertNotNull(response);
        assertNotNull(responseTreeRootNode);
        assertNotNull(oauthErrorNode);
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());

        assertEquals("oauth_error", responseTreeRootNode.fieldNames().next()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
    }

    @Test
    void shouldReturn204NoContentWhenContextIsNull() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        SessionItem sessionItem = new SessionItem();
        when(mockSessionService.validateSessionId(sessionId)).thenReturn(sessionItem);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).validateSessionId(sessionId);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(HttpStatusCode.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void handleResponseShouldReturnForbiddenResponseSessionIdIsInvalid()
            throws JsonProcessingException {
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        Map<String, String> headers = new HashMap<>();
        headers.put("session_id", "invalid");

        when(mockRequestEvent.getHeaders()).thenReturn(headers);

        APIGatewayProxyResponseEvent responseEvent =
                handler.handleRequest(mockRequestEvent, context);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(responseEvent.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage());

        assertNotNull(responseEvent);
        assertNotNull(responseTreeRootNode);
        assertNotNull(oauthErrorNode);
        assertEquals(HttpStatusCode.FORBIDDEN, responseEvent.getStatusCode());

        assertEquals("oauth_error", responseTreeRootNode.fieldNames().next()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
    }

    @Test
    void handleResponseShouldReturnForbiddenResponseInputHeadersAreMissing()
            throws JsonProcessingException {
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        Map<String, String> headers = null;

        when(mockRequestEvent.getHeaders()).thenReturn(headers);

        APIGatewayProxyResponseEvent responseEvent =
                handler.handleRequest(mockRequestEvent, context);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(responseEvent.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage());

        assertNotNull(responseEvent);
        assertNotNull(responseTreeRootNode);
        assertNotNull(oauthErrorNode);
        assertEquals(HttpStatusCode.FORBIDDEN, responseEvent.getStatusCode());

        assertEquals("oauth_error", responseTreeRootNode.fieldNames().next()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
    }

    private void mockServiceFactoryBehaviour() {
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);

        when(mockServiceFactory.getSessionService()).thenReturn(mockSessionService);

        when(mockServiceFactory.getPersonIdentityService()).thenReturn(mockPersonIdentityService);
    }
}
