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
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class ApiKeyVerificationServiceTest {

    private static final ApiKeyVerificationConfig CONFIG =
            new ApiKeyVerificationConfig(Duration.ofSeconds(20), Duration.ofSeconds(60), 5);

    @Mock private TokenRequestService tokenRequestService;
    @Mock private DriverMatchService driverMatchService;
    @Mock private SleepHelper sleepHelper;

    private ApiKeyVerificationService apiKeyVerificationService;

    @BeforeEach
    void setUp() {
        apiKeyVerificationService =
                new ApiKeyVerificationService(
                        CONFIG, tokenRequestService, driverMatchService, sleepHelper);
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
        inOrder.verifyNoMoreInteractions();
        assertEquals(
                "DECER607085KE9LN",
                ((DrivingPermitForm) formCaptor.getValue()).getDrivingLicenceNumber());
    }

    @Test
    void retriesWithBackoffUntilTheMatchSucceeds() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE)).thenReturn("token_value");
        when(driverMatchService.performMatch(any(), any(), any(), any()))
                .thenThrow(matchUnavailable())
                .thenThrow(keyRejected())
                .thenReturn(null); // success

        apiKeyVerificationService.verify("new_api_key");

        InOrder inOrder = inOrder(sleepHelper, driverMatchService);
        for (int waitMs : new int[] {20_000, 40_000, 60_000}) {
            inOrder.verify(sleepHelper).busyWaitMilliseconds(waitMs);
            inOrder.verify(driverMatchService)
                    .performMatch(
                            any(), eq("token_value"), eq("new_api_key"), eq(Strategy.NO_CHANGE));
        }
        inOrder.verifyNoMoreInteractions();
        verify(tokenRequestService, times(1)).requestToken(true, Strategy.NO_CHANGE);
    }

    @Test
    void givesUpAfterTheLastAttemptAndRethrowsItsFailure() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE)).thenReturn("token_value");
        DVLAMatchUnauthorizedException lastFailure = keyRejected();
        when(driverMatchService.performMatch(any(), any(), any(), any()))
                .thenThrow(keyRejected())
                .thenThrow(matchUnavailable())
                .thenThrow(keyRejected())
                .thenThrow(keyRejected())
                .thenThrow(lastFailure);

        DVLAMatchUnauthorizedException thrown =
                assertThrows(
                        DVLAMatchUnauthorizedException.class,
                        () -> apiKeyVerificationService.verify("new_api_key"));

        assertSame(lastFailure, thrown);
        InOrder inOrder = inOrder(sleepHelper, driverMatchService);
        for (int waitMs : new int[] {20_000, 40_000, 60_000, 60_000, 60_000}) {
            inOrder.verify(sleepHelper).busyWaitMilliseconds(waitMs);
            inOrder.verify(driverMatchService).performMatch(any(), any(), any(), any());
        }
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void doesNotRetryAnUnexpectedFailure() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE)).thenReturn("token_value");
        IllegalStateException failure = new IllegalStateException("unexpected");
        doThrow(failure).when(driverMatchService).performMatch(any(), any(), any(), any());

        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> apiKeyVerificationService.verify("new_api_key"));

        assertSame(failure, thrown);
        verify(driverMatchService, times(1)).performMatch(any(), any(), any(), any());
    }

    @Test
    void doesNotAttemptAMatchWhenTheTokenRequestFails() throws Exception {
        when(tokenRequestService.requestToken(true, Strategy.NO_CHANGE))
                .thenThrow(matchUnavailable());

        assertThrows(
                OAuthErrorResponseException.class,
                () -> apiKeyVerificationService.verify("new_api_key"));

        verifyNoInteractions(sleepHelper);
        verify(driverMatchService, never()).performMatch(any(), any(), any(), any());
    }

    private static OAuthErrorResponseException matchUnavailable() {
        return new OAuthErrorResponseException(500, ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT);
    }

    private static DVLAMatchUnauthorizedException keyRejected() {
        return new DVLAMatchUnauthorizedException("rejected");
    }
}
