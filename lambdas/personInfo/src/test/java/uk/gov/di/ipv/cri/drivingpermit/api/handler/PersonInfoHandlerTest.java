package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.common.contenttype.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.library.service.DocumentCheckResultStorageService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.util.PersonIdentityDetailedTestDataGenerator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class PersonInfoHandlerTest {
    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    public static final String SUBJECT = "subject";

    @Mock ConfigurationService mockCommonLibConfigurationService;

    @Mock ServiceFactory mockServiceFactory;

    @Mock private EventProbe mockEventProbe;

    @Mock private SessionService mockSessionService;

    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private DocumentCheckResultStorageService mockDocumentCheckResultStorageService;

    @Mock private Context context;

    private PersonInfoHandler handler;

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        mockServiceFactoryBehaviour();

        this.handler = new PersonInfoHandler(mockServiceFactory);
    }

    @Test
    void shouldReturn200OkWhenPersonInfoRequestIsValid() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        PersonIdentityDetailed savedPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate("DVLA");

        SessionItem sessionItem = new SessionItem();
        sessionItem.setContext("check_details");

        when(mockSessionService.getSession(sessionId)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(savedPersonIdentityDetailed);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSession(sessionId);
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(
                ContentType.APPLICATION_JSON.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn204NoContentWhenContextIsIncorrectValue() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        PersonIdentityDetailed savedPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate("DVLA");

        SessionItem sessionItem = new SessionItem();
        sessionItem.setContext("international_address");

        when(mockSessionService.getSession(sessionId)).thenReturn(sessionItem);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSession(sessionId);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(HttpStatusCode.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldReturn204NoContentWhenContextIsNull() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        String sessionId = UUID.randomUUID().toString();
        event.withHeaders(Map.of("session_id", sessionId));

        PersonIdentityDetailed savedPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate("DVLA");

        SessionItem sessionItem = new SessionItem();
        when(mockSessionService.getSession(sessionId)).thenReturn(sessionItem);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSession(sessionId);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_PERSON_INFO_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_PERSON_INFO_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(HttpStatusCode.NO_CONTENT, response.getStatusCode());
    }

    private void mockServiceFactoryBehaviour() {
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);

        when(mockServiceFactory.getSessionService()).thenReturn(mockSessionService);

        when(mockServiceFactory.getPersonIdentityService()).thenReturn(mockPersonIdentityService);
    }
}
