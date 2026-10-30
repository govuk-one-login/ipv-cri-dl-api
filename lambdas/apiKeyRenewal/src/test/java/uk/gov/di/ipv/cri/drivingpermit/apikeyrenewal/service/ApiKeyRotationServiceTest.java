package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeyDvlaHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeySecretsManagerClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.LIVE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.NO_CHANGE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.UAT;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.ALREADY_CURRENT;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.DISABLED;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.PROMOTED_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.PROMOTED_VERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class ApiKeyRotationServiceTest {

    private static final String SECRET_ID =
            "arn:aws:secretsmanager:eu-west-2:123456789012:secret:/stack-name/DVLA/apiKey-AbC123";
    private static final String VERSION_ID = "rotation-token";
    private static final String TOKEN = "token_value";
    private static final String NEW_API_KEY = "new_api_key"; // pragma: allowlist secret
    private static final String PENDING_API_KEY = "pending_api_key"; // pragma: allowlist secret

    @Mock private ApiKeySecretsManagerClient apiKeySecretsManagerClient;
    @Mock private ApiKeyDvlaHttpClient apiKeyDvlaHttpClient;
    @Mock private TokenRequestService tokenRequestService;
    @Mock private ApiKeyVerificationService apiKeyVerificationService;

    private ApiKeyRotationService service;

    @BeforeEach
    void setUp() {
        service = serviceWithRotationEnabled(true);
    }

    @Test
    void doesNothingWhenRotationIsDisabled() throws Exception {
        assertEquals(DISABLED, serviceWithRotationEnabled(false).rotate(SECRET_ID, VERSION_ID));

        verifyNoInteractions(
                apiKeySecretsManagerClient,
                apiKeyDvlaHttpClient,
                tokenRequestService,
                apiKeyVerificationService);
    }

    @Test
    void onlyClearsCachedTokensWhenThisRotationsKeyIsAlreadyCurrent() throws Exception {
        when(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID)).thenReturn(true);

        assertEquals(ALREADY_CURRENT, service.rotate(SECRET_ID, VERSION_ID));

        verifyCachedTokensCleared();
        verifyNoInteractions(apiKeyDvlaHttpClient, apiKeyVerificationService);
        verify(apiKeySecretsManagerClient, never()).findPending(any(), any());
        verify(apiKeySecretsManagerClient, never()).promoteToCurrent(any(), any());
    }

    @Test
    void stopsWhenThePendingKeyCannotBeRead() {
        when(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID)).thenReturn(false);
        when(apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID))
                .thenThrow(SecretsManagerException.builder().message("read failed").build());

        assertThrows(SecretsManagerException.class, () -> service.rotate(SECRET_ID, VERSION_ID));

        verifyNoInteractions(apiKeyDvlaHttpClient, tokenRequestService, apiKeyVerificationService);
    }

    @Nested
    class WhenNoKeyIsPendingForThisRotation {

        @BeforeEach
        void noKeyIsPending() {
            when(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID)).thenReturn(false);
            when(apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID))
                    .thenReturn(Optional.empty());
        }

        @Test
        void requestsStoresVerifiesAndPromotesANewKey() throws Exception {
            givenDvlaIssues(NEW_API_KEY);

            assertEquals(PROMOTED_VERIFIED, service.rotate(SECRET_ID, VERSION_ID));

            InOrder inOrder =
                    inOrder(
                            apiKeySecretsManagerClient,
                            apiKeyVerificationService,
                            tokenRequestService);
            inOrder.verify(apiKeySecretsManagerClient)
                    .putPending(SECRET_ID, VERSION_ID, NEW_API_KEY);
            inOrder.verify(apiKeyVerificationService).verify(NEW_API_KEY);
            inOrder.verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, VERSION_ID);
            inOrder.verify(tokenRequestService).removeTokenItem(NO_CHANGE);
            inOrder.verify(tokenRequestService).removeTokenItem(UAT);
            inOrder.verify(tokenRequestService).removeTokenItem(LIVE);
        }

        @ParameterizedTest
        @MethodSource(
                "uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service.ApiKeyRotationServiceTest#verificationFailures")
        void promotesTheNewKeyUnverifiedWhenVerificationFails(Exception failure) throws Exception {
            givenDvlaIssues(NEW_API_KEY);
            doThrow(failure).when(apiKeyVerificationService).verify(NEW_API_KEY);

            assertEquals(PROMOTED_UNVERIFIED, service.rotate(SECRET_ID, VERSION_ID));

            verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, VERSION_ID);
            verifyCachedTokensCleared();
        }

        @Test
        void storesNothingWhenDvlaFailsToIssueAKey() throws Exception {
            when(tokenRequestService.requestToken(true, NO_CHANGE)).thenReturn(TOKEN);
            when(apiKeyDvlaHttpClient.requestNewApiKey(TOKEN))
                    .thenThrow(
                            new OAuthErrorResponseException(
                                    500, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT));

            assertThrows(
                    OAuthErrorResponseException.class, () -> service.rotate(SECRET_ID, VERSION_ID));

            verify(apiKeySecretsManagerClient, never()).putPending(any(), any(), any());
            verify(apiKeySecretsManagerClient, never()).promoteToCurrent(any(), any());
            verifyNoInteractions(apiKeyVerificationService);
        }

        @Test
        void doesNotVerifyOrPromoteWhenTheNewKeyCannotBeStored() throws Exception {
            givenDvlaIssues(NEW_API_KEY);
            doThrow(SecretsManagerException.builder().message("put failed").build())
                    .when(apiKeySecretsManagerClient)
                    .putPending(SECRET_ID, VERSION_ID, NEW_API_KEY);

            assertThrows(
                    SecretsManagerException.class, () -> service.rotate(SECRET_ID, VERSION_ID));

            verifyNoInteractions(apiKeyVerificationService);
            verify(apiKeySecretsManagerClient, never()).promoteToCurrent(any(), any());
        }
    }

    @Nested
    class WhenAnEarlierAttemptAtThisRotationStoredAKey {

        @BeforeEach
        void aKeyIsPending() {
            when(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID)).thenReturn(false);
            when(apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID))
                    .thenReturn(Optional.of(PENDING_API_KEY));
        }

        @Test
        void verifiesAndPromotesItWithoutRequestingANewOne() throws Exception {
            assertEquals(PROMOTED_VERIFIED, service.rotate(SECRET_ID, VERSION_ID));

            verifyNoInteractions(apiKeyDvlaHttpClient);
            verify(apiKeySecretsManagerClient, never()).putPending(any(), any(), any());
            verify(apiKeyVerificationService).verify(PENDING_API_KEY);
            verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, VERSION_ID);
        }

        @Test
        void promotesItUnverifiedWhenVerificationFails() throws Exception {
            doThrow(new DVLAMatchUnauthorizedException("rejected"))
                    .when(apiKeyVerificationService)
                    .verify(PENDING_API_KEY);

            assertEquals(PROMOTED_UNVERIFIED, service.rotate(SECRET_ID, VERSION_ID));

            verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, VERSION_ID);
            verifyCachedTokensCleared();
        }

        @Test
        void keepsCachedTokensWhenPromotionFails() {
            doThrow(SecretsManagerException.builder().message("update failed").build())
                    .when(apiKeySecretsManagerClient)
                    .promoteToCurrent(SECRET_ID, VERSION_ID);

            assertThrows(
                    SecretsManagerException.class, () -> service.rotate(SECRET_ID, VERSION_ID));

            verify(tokenRequestService, never()).removeTokenItem(any());
        }
    }

    static List<Exception> verificationFailures() {
        return List.of(
                new OAuthErrorResponseException(500, ERROR_INVOKING_THIRD_PARTY_API_MATCH_ENDPOINT),
                new DVLAMatchUnauthorizedException("rejected"),
                new IllegalStateException("unexpected"));
    }

    private void givenDvlaIssues(String newApiKey) throws Exception {
        when(tokenRequestService.requestToken(true, NO_CHANGE)).thenReturn(TOKEN);
        when(apiKeyDvlaHttpClient.requestNewApiKey(TOKEN)).thenReturn(newApiKey);
    }

    private void verifyCachedTokensCleared() {
        verify(tokenRequestService).removeTokenItem(NO_CHANGE);
        verify(tokenRequestService).removeTokenItem(LIVE);
        verify(tokenRequestService).removeTokenItem(UAT);
    }

    private ApiKeyRotationService serviceWithRotationEnabled(boolean rotationEnabled) {
        return new ApiKeyRotationService(
                new ApiKeyRotationConfig(rotationEnabled),
                apiKeySecretsManagerClient,
                apiKeyDvlaHttpClient,
                tokenRequestService,
                apiKeyVerificationService);
    }
}
