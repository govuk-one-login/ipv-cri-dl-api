package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeySecretsManagerClientTest {

    private static final String SECRET_ID =
            "arn:aws:secretsmanager:eu-west-2:123456789012:secret:/stack-name/DVLA/apiKey-AbC123";

    @Mock private SecretsManagerClient secretsManagerClient;

    @Captor private ArgumentCaptor<Consumer<GetSecretValueRequest.Builder>> getSecretValueRequest;
    @Captor private ArgumentCaptor<Consumer<PutSecretValueRequest.Builder>> putSecretValueRequest;
    @Captor private ArgumentCaptor<Consumer<UpdateSecretRequest.Builder>> updateSecretRequest;

    private ApiKeySecretsManagerClient apiKeySecretsManagerClient;

    @BeforeEach
    void setUp() {
        apiKeySecretsManagerClient = new ApiKeySecretsManagerClient(secretsManagerClient);
    }

    @Test
    void findsThePendingKey() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenReturn(GetSecretValueResponse.builder().secretString("pending").build());

        assertEquals(Optional.of("pending"), apiKeySecretsManagerClient.findPending(SECRET_ID));
        assertEquals(
                GetSecretValueRequest.builder()
                        .secretId(SECRET_ID)
                        .versionStage("AWSPENDING")
                        .build(),
                GetSecretValueRequest.builder()
                        .applyMutation(getSecretValueRequest.getValue())
                        .build());
    }

    @Test
    void findsNothingWhenThereIsNoPendingVersion() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenThrow(ResourceNotFoundException.builder().message("not found").build());

        assertEquals(Optional.empty(), apiKeySecretsManagerClient.findPending(SECRET_ID));
    }

    @Test
    void findsNothingWhenThePendingVersionHasNoValue() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenReturn(GetSecretValueResponse.builder().build());

        assertEquals(Optional.empty(), apiKeySecretsManagerClient.findPending(SECRET_ID));
    }

    @Test
    void propagatesOtherSecretsManagerFailures() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenThrow(SecretsManagerException.builder().message("access denied").build());

        assertThrows(
                SecretsManagerException.class,
                () -> apiKeySecretsManagerClient.findPending(SECRET_ID));
    }

    @Test
    void storesTheKeyAsPending() {
        apiKeySecretsManagerClient.putPending(SECRET_ID, "new_api_key");

        verify(secretsManagerClient).putSecretValue(putSecretValueRequest.capture());
        assertEquals(
                PutSecretValueRequest.builder()
                        .secretId(SECRET_ID)
                        .secretString("new_api_key")
                        .versionStages("AWSPENDING")
                        .build(),
                PutSecretValueRequest.builder()
                        .applyMutation(putSecretValueRequest.getValue())
                        .build());
    }

    @Test
    void promotesTheKeyToCurrent() {
        apiKeySecretsManagerClient.promoteToCurrent(SECRET_ID, "new_api_key");

        verify(secretsManagerClient).updateSecret(updateSecretRequest.capture());
        assertEquals(
                UpdateSecretRequest.builder()
                        .secretId(SECRET_ID)
                        .secretString("new_api_key")
                        .build(),
                UpdateSecretRequest.builder()
                        .applyMutation(updateSecretRequest.getValue())
                        .build());
    }
}
