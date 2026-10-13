package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.exception.ApiKeyRotationException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service.ApiKeyRotationService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_OK;

@ExtendWith({SystemStubsExtension.class, MockitoExtension.class})
class ApiKeyRenewalHandlerTest {

    private static final String SECRET_ID =
            "arn:aws:secretsmanager:eu-west-2:123456789012:secret:/stack-name/DVLA/apiKey-AbC123";

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ApiKeyRotationService apiKeyRotationService;
    @Mock private EventProbe eventProbe;
    @Mock private Context context;

    private ApiKeyRenewalHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        environmentVariables.set("POWERTOOLS_METRICS_NAMESPACE", "StackName");

        try (var _ = mockConstruction(ClientProviderFactory.class);
                var _ = mockConstruction(DvlaConfiguration.class);
                var _ = mockConstruction(TokenRequestService.class);
                var _ = mockConstruction(DriverMatchService.class);
                var _ =
                        mockConstruction(
                                ApiKeyRotationService.class,
                                withSettings().defaultAnswer(delegatesTo(apiKeyRotationService)));
                var _ =
                        mockConstruction(
                                EventProbe.class,
                                withSettings().defaultAnswer(delegatesTo(eventProbe)))) {
            handler = new ApiKeyRenewalHandler();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"createSecret", "setSecret", "testSecret", "notAStep"})
    void succeedsWithoutRotatingOnStepsOtherThanFinishSecret(String step) {
        assertEquals("Success", handler.handleRequest(event(step), context));

        verifyNoInteractions(apiKeyRotationService);
        verify(eventProbe).counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_OK);
    }

    @Test
    void rotatesTheSecretOnFinishStep() throws Exception {
        assertEquals("Success", handler.handleRequest(event("finishSecret"), context));

        verify(apiKeyRotationService).rotate(SECRET_ID);
        verify(eventProbe).counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(eventProbe);
    }

    @Test
    void recordsAnErrorAndRethrowsWhenRotationFails() throws Exception {
        RuntimeException failure = new RuntimeException("rotation failed");
        doThrow(failure).when(apiKeyRotationService).rotate(SECRET_ID);
        SecretsManagerRotationEvent finishSecret = event("finishSecret");

        ApiKeyRotationException thrown =
                assertThrows(
                        ApiKeyRotationException.class,
                        () -> handler.handleRequest(finishSecret, context));

        assertSame(failure, thrown.getCause());
        verify(eventProbe).counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
        verifyNoMoreInteractions(eventProbe);
    }

    private static SecretsManagerRotationEvent event(String step) {
        SecretsManagerRotationEvent event = new SecretsManagerRotationEvent();
        event.setSecretId(SECRET_ID);
        event.setClientRequestToken("rotation-token");
        event.setStep(step);
        return event;
    }
}
