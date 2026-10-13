package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.SleepHelper;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyVerificationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DvlaFormFields;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class ApiKeyVerificationServiceTest {

    private static final Duration PAUSE = Duration.ofSeconds(20);

    @Mock private TokenRequestService tokenRequestService;
    @Mock private DriverMatchService driverMatchService;
    @Mock private SleepHelper sleepHelper;

    private ApiKeyVerificationService apiKeyVerificationService;

    @BeforeEach
    void setUp() {
        apiKeyVerificationService =
                new ApiKeyVerificationService(
                        new ApiKeyVerificationConfig(PAUSE),
                        tokenRequestService,
                        driverMatchService,
                        sleepHelper);
    }

    @Test
    void pausesThenPerformsADriverMatchWithTheKeyUnderTest() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE)).thenReturn("token_value");

        apiKeyVerificationService.verify("new_api_key");

        ArgumentCaptor<DvlaFormFields> formCaptor = ArgumentCaptor.forClass(DvlaFormFields.class);
        InOrder inOrder = inOrder(sleepHelper, driverMatchService);
        inOrder.verify(sleepHelper).busyWaitMilliseconds(20_000);
        inOrder.verify(driverMatchService)
                .performMatch(
                        formCaptor.capture(),
                        eq("token_value"),
                        eq("new_api_key"),
                        eq(Strategy.NO_CHANGE));
        assertEquals(
                "DECER607085KE9LN",
                ((DrivingPermitForm) formCaptor.getValue()).getDrivingLicenceNumber());
    }

    @Test
    void propagatesAFailedMatch() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE)).thenReturn("token_value");
        doThrow(new OAuthErrorResponseException(401, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
                .when(driverMatchService)
                .performMatch(any(), any(), any(), any());

        assertThrows(
                OAuthErrorResponseException.class,
                () -> apiKeyVerificationService.verify("new_api_key"));
    }
}
