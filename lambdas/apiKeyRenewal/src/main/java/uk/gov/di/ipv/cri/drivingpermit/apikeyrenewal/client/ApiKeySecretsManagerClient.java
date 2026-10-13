package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.aws.SecretVersionStage;

import java.util.Optional;

public class ApiKeySecretsManagerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeySecretsManagerClient.class);

    private final SecretsManagerClient secretsManagerClient;

    public ApiKeySecretsManagerClient(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public Optional<String> findPending(String secretId) {
        try {
            return Optional.ofNullable(
                    secretsManagerClient
                            .getSecretValue(
                                    getRequest ->
                                            getRequest
                                                    .secretId(secretId)
                                                    .versionStage(
                                                            SecretVersionStage.PENDING.value()))
                            .secretString());
        } catch (ResourceNotFoundException _) {
            LOGGER.info(
                    "No {} API key found in Secrets Manager", SecretVersionStage.PENDING.value());
            return Optional.empty();
        }
    }

    public void putPending(String secretId, String apiKey) {
        secretsManagerClient.putSecretValue(
                putRequest ->
                        putRequest
                                .secretId(secretId)
                                .secretString(apiKey)
                                .versionStages(SecretVersionStage.PENDING.value()));
        LOGGER.info("Stored the new API key as {}", SecretVersionStage.PENDING.value());
    }

    public void promoteToCurrent(String secretId, String apiKey) {
        secretsManagerClient.updateSecret(
                updateRequest -> updateRequest.secretId(secretId).secretString(apiKey));
        LOGGER.info("Updated the current API key in Secrets Manager");
    }
}
