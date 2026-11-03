package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretVersionStageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeySecretsManagerClientTest {

    private static final String SECRET_ID =
            "arn:aws:secretsmanager:eu-west-2:123456789012:secret:/stack-name/DVLA/apiKey-AbC123";
    private static final String VERSION_ID = "rotation-token";
    private static final String CURRENT_VERSION_ID = "current-version";

    @Mock private SecretsManagerClient secretsManagerClient;

    @Captor private ArgumentCaptor<Consumer<GetSecretValueRequest.Builder>> getSecretValueRequest;
    @Captor private ArgumentCaptor<Consumer<PutSecretValueRequest.Builder>> putSecretValueRequest;
    @Captor private ArgumentCaptor<Consumer<DescribeSecretRequest.Builder>> describeSecretRequest;

    @Captor
    private ArgumentCaptor<Consumer<UpdateSecretVersionStageRequest.Builder>>
            updateSecretVersionStageRequest;

    private ApiKeySecretsManagerClient apiKeySecretsManagerClient;

    @BeforeEach
    void setUp() {
        apiKeySecretsManagerClient = new ApiKeySecretsManagerClient(secretsManagerClient);
    }

    @Test
    void findsThePendingKeyForTheVersion() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenReturn(GetSecretValueResponse.builder().secretString("pending").build());

        assertEquals(
                Optional.of("pending"),
                apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID));
        assertEquals(
                GetSecretValueRequest.builder()
                        .secretId(SECRET_ID)
                        .versionId(VERSION_ID)
                        .versionStage("AWSPENDING")
                        .build(),
                GetSecretValueRequest.builder()
                        .applyMutation(getSecretValueRequest.getValue())
                        .build());
    }

    @Test
    void findsNothingWhenTheVersionHasNoPendingValue() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenThrow(ResourceNotFoundException.builder().message("not found").build());

        assertEquals(
                Optional.empty(), apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID));
    }

    @Test
    void findsNothingWhenThePendingVersionHasNoValue() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenReturn(GetSecretValueResponse.builder().build());

        assertEquals(
                Optional.empty(), apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID));
    }

    @Test
    void propagatesOtherSecretsManagerFailures() {
        when(secretsManagerClient.getSecretValue(getSecretValueRequest.capture()))
                .thenThrow(SecretsManagerException.builder().message("access denied").build());

        assertThrows(
                SecretsManagerException.class,
                () -> apiKeySecretsManagerClient.findPending(SECRET_ID, VERSION_ID));
    }

    @Test
    void storesTheKeyAsPendingUnderTheVersion() {
        apiKeySecretsManagerClient.putPending(SECRET_ID, VERSION_ID, "new_api_key");

        verify(secretsManagerClient).putSecretValue(putSecretValueRequest.capture());
        assertEquals(
                PutSecretValueRequest.builder()
                        .secretId(SECRET_ID)
                        .clientRequestToken(VERSION_ID)
                        .secretString("new_api_key")
                        .versionStages("AWSPENDING")
                        .build(),
                PutSecretValueRequest.builder()
                        .applyMutation(putSecretValueRequest.getValue())
                        .build());
    }

    @Test
    void reportsTheVersionAsCurrentWhenItHoldsTheCurrentStage() {
        givenVersionStages(Map.of(VERSION_ID, List.of("AWSCURRENT", "AWSPENDING")));

        assertTrue(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID));
        assertEquals(
                DescribeSecretRequest.builder().secretId(SECRET_ID).build(),
                DescribeSecretRequest.builder()
                        .applyMutation(describeSecretRequest.getValue())
                        .build());
    }

    @Test
    void reportsTheVersionAsNotCurrentWhenItIsOnlyPending() {
        givenVersionStages(
                Map.of(
                        CURRENT_VERSION_ID, List.of("AWSCURRENT"),
                        VERSION_ID, List.of("AWSPENDING")));

        assertFalse(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID));
    }

    @Test
    void reportsTheVersionAsNotCurrentWhenItIsUnknown() {
        givenVersionStages(Map.of(CURRENT_VERSION_ID, List.of("AWSCURRENT")));

        assertFalse(apiKeySecretsManagerClient.isCurrent(SECRET_ID, VERSION_ID));
    }

    @Test
    void movesTheCurrentStageFromTheCurrentVersionToTheNewOne() {
        givenVersionStages(
                Map.of(
                        CURRENT_VERSION_ID, List.of("AWSCURRENT"),
                        VERSION_ID, List.of("AWSPENDING")));

        apiKeySecretsManagerClient.promoteToCurrent(SECRET_ID, VERSION_ID);

        verify(secretsManagerClient)
                .updateSecretVersionStage(updateSecretVersionStageRequest.capture());
        assertEquals(
                UpdateSecretVersionStageRequest.builder()
                        .secretId(SECRET_ID)
                        .versionStage("AWSCURRENT")
                        .moveToVersionId(VERSION_ID)
                        .removeFromVersionId(CURRENT_VERSION_ID)
                        .build(),
                UpdateSecretVersionStageRequest.builder()
                        .applyMutation(updateSecretVersionStageRequest.getValue())
                        .build());
    }

    private void givenVersionStages(Map<String, List<String>> versionIdsToStages) {
        when(secretsManagerClient.describeSecret(describeSecretRequest.capture()))
                .thenReturn(
                        DescribeSecretResponse.builder()
                                .versionIdsToStages(versionIdsToStages)
                                .build());
    }
}
