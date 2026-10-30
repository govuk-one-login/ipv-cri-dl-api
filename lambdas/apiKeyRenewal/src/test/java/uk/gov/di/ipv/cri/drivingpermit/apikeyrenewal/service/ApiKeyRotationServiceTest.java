package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeyDvlaHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeySecretsManagerClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.util.Optional;

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
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class ApiKeyRotationServiceTest {

    private static final String SECRET_ID =
            "arn:aws:secretsmanager:eu-west-2:123456789012:secret:/stack-name/DVLA/apiKey-AbC123";
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
        serviceWithRotationEnabled(false).rotate(SECRET_ID);

        verifyNoInteractions(
                apiKeySecretsManagerClient,
                apiKeyDvlaHttpClient,
                tokenRequestService,
                apiKeyVerificationService);
    }

    @Test
    void stopsWhenThePendingKeyCannotBeRead() {
        when(apiKeySecretsManagerClient.findPending(SECRET_ID))
                .thenThrow(SecretsManagerException.builder().message("read failed").build());

        assertThrows(SecretsManagerException.class, () -> service.rotate(SECRET_ID));

        verifyNoInteractions(apiKeyDvlaHttpClient, tokenRequestService, apiKeyVerificationService);
    }

    @Nested
    class WhenNoKeyIsPending {

        @BeforeEach
        void noKeyIsPending() {
            when(apiKeySecretsManagerClient.findPending(SECRET_ID)).thenReturn(Optional.empty());
        }

        @Test
        void requestsStoresVerifiesAndPromotesANewKey() throws Exception {
            givenDvlaIssues(NEW_API_KEY);

            service.rotate(SECRET_ID);

            InOrder inOrder =
                    inOrder(
                            apiKeySecretsManagerClient,
                            apiKeyVerificationService,
                            tokenRequestService);
            inOrder.verify(apiKeySecretsManagerClient).putPending(SECRET_ID, NEW_API_KEY);
            inOrder.verify(apiKeyVerificationService).verify(NEW_API_KEY);
            inOrder.verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, NEW_API_KEY);
            inOrder.verify(tokenRequestService).removeTokenItem(NO_CHANGE);
            inOrder.verify(tokenRequestService).removeTokenItem(LIVE);
            inOrder.verify(tokenRequestService).removeTokenItem(UAT);
        }

        @Test
        void storesNothingWhenDvlaFailsToIssueAKey() throws Exception {
            when(tokenRequestService.requestToken(true, NO_CHANGE)).thenReturn(TOKEN);
            when(apiKeyDvlaHttpClient.requestNewApiKey(TOKEN))
                    .thenThrow(
                            new OAuthErrorResponseException(
                                    500, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT));

            assertThrows(OAuthErrorResponseException.class, () -> service.rotate(SECRET_ID));

            verify(apiKeySecretsManagerClient, never()).putPending(any(), any());
            verifyNoInteractions(apiKeyVerificationService);
        }

        @Test
        void doesNotVerifyOrPromoteWhenTheNewKeyCannotBeStored() throws Exception {
            givenDvlaIssues(NEW_API_KEY);
            doThrow(SecretsManagerException.builder().message("put failed").build())
                    .when(apiKeySecretsManagerClient)
                    .putPending(SECRET_ID, NEW_API_KEY);

            assertThrows(SecretsManagerException.class, () -> service.rotate(SECRET_ID));

            verifyNoInteractions(apiKeyVerificationService);
            verify(apiKeySecretsManagerClient, never()).promoteToCurrent(any(), any());
        }
    }

    @Nested
    class WhenAKeyIsPendingFromAPreviousRun {

        @BeforeEach
        void aKeyIsPending() {
            when(apiKeySecretsManagerClient.findPending(SECRET_ID))
                    .thenReturn(Optional.of(PENDING_API_KEY));
        }

        @Test
        void verifiesAndPromotesItWithoutRequestingANewOne() throws Exception {
            service.rotate(SECRET_ID);

            verifyNoInteractions(apiKeyDvlaHttpClient);
            verify(apiKeySecretsManagerClient, never()).putPending(any(), any());
            verify(apiKeyVerificationService).verify(PENDING_API_KEY);
            verify(apiKeySecretsManagerClient).promoteToCurrent(SECRET_ID, PENDING_API_KEY);
        }

        // The incident behaviour. Changes with R1 in DVLA_API_KEY_ROTATION_REMEDIATION.md.
        @Test
        void doesNotPromoteItWhenVerificationFails() throws Exception {
            doThrow(
                            new OAuthErrorResponseException(
                                    401, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
                    .when(apiKeyVerificationService)
                    .verify(PENDING_API_KEY);

            assertThrows(OAuthErrorResponseException.class, () -> service.rotate(SECRET_ID));

            verify(apiKeySecretsManagerClient, never()).promoteToCurrent(any(), any());
            verify(tokenRequestService, never()).removeTokenItem(any());
        }

        @Test
        void keepsCachedTokensWhenPromotionFails() {
            doThrow(SecretsManagerException.builder().message("update failed").build())
                    .when(apiKeySecretsManagerClient)
                    .promoteToCurrent(SECRET_ID, PENDING_API_KEY);

            assertThrows(SecretsManagerException.class, () -> service.rotate(SECRET_ID));

            verify(tokenRequestService, never()).removeTokenItem(any());
        }
    }

    private void givenDvlaIssues(String newApiKey) throws Exception {
        when(tokenRequestService.requestToken(true, NO_CHANGE)).thenReturn(TOKEN);
        when(apiKeyDvlaHttpClient.requestNewApiKey(TOKEN)).thenReturn(newApiKey);
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
