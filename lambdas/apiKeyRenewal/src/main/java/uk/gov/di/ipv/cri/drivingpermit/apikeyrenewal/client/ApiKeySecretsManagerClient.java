package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.aws.SecretVersionStage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApiKeySecretsManagerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeySecretsManagerClient.class);

    private final SecretsManagerClient secretsManagerClient;

    public ApiKeySecretsManagerClient(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public Optional<String> findPending(String secretId, String versionId) {
        try {
            return Optional.ofNullable(
                    secretsManagerClient
                            .getSecretValue(
                                    getRequest ->
                                            getRequest
                                                    .secretId(secretId)
                                                    .versionId(versionId)
                                                    .versionStage(
                                                            SecretVersionStage.PENDING.value()))
                            .secretString());
        } catch (ResourceNotFoundException _) {
            LOGGER.info(
                    "No {} API key found for this rotation", SecretVersionStage.PENDING.value());
            return Optional.empty();
        }
    }

    public void putPending(String secretId, String versionId, String apiKey) {
        secretsManagerClient.putSecretValue(
                putRequest ->
                        putRequest
                                .secretId(secretId)
                                .clientRequestToken(versionId)
                                .secretString(apiKey)
                                .versionStages(SecretVersionStage.PENDING.value()));
        LOGGER.info("Stored the new API key as {}", SecretVersionStage.PENDING.value());
    }

    public boolean isCurrent(String secretId, String versionId) {
        return versionStages(secretId)
                .getOrDefault(versionId, List.of())
                .contains(SecretVersionStage.CURRENT.value());
    }

    public void promoteToCurrent(String secretId, String versionId) {
        String currentVersionId =
                versionStages(secretId).entrySet().stream()
                        .filter(
                                version ->
                                        version.getValue()
                                                .contains(SecretVersionStage.CURRENT.value()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

        secretsManagerClient.updateSecretVersionStage(
                updateRequest ->
                        updateRequest
                                .secretId(secretId)
                                .versionStage(SecretVersionStage.CURRENT.value())
                                .moveToVersionId(versionId)
                                .removeFromVersionId(currentVersionId));
        LOGGER.info("Moved {} to the new API key version", SecretVersionStage.CURRENT.value());
    }

    private Map<String, List<String>> versionStages(String secretId) {
        return secretsManagerClient
                .describeSecret(describeRequest -> describeRequest.secretId(secretId))
                .versionIdsToStages();
    }
}
