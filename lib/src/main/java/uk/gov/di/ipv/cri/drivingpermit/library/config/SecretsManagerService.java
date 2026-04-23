package uk.gov.di.ipv.cri.drivingpermit.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Optional;

public class SecretsManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretsManagerService.class);

    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private static final String GET_STACK_SECRET_VALUE_LOG_FORMAT = "{} getStackSecretValue";
    private static final String AWS_CURRENT = "AWSCURRENT";

    // Prefixes
    private final String parameterPrefix; // Parameters that can hava prefix override
    private final String stackParameterPrefix; // Parameters that must always be from the stack
    private final String commonParameterPrefix;

    private final SecretsManagerClient secretsManagerClient;

    public SecretsManagerService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;

        this.parameterPrefix =
                Optional.ofNullable(System.getenv("PARAMETER_PREFIX"))
                        .orElse(System.getenv("AWS_STACK_NAME"));

        this.stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        this.commonParameterPrefix = System.getenv("COMMON_PARAMETER_NAME_PREFIX");
    }

    public String getSecretValue(String parameterName) {
        String secretId = String.format(PARAMETER_NAME_FORMAT, parameterPrefix, parameterName);

        GetSecretValueRequest valueRequest =
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .versionStage(AWS_CURRENT)
                        .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

        LOGGER.info(GET_STACK_SECRET_VALUE_LOG_FORMAT, secretId);

        return valueResponse.secretString();
    }

    public String getStackSecretValue(String parameterName) {
        String secretId = String.format(PARAMETER_NAME_FORMAT, stackParameterPrefix, parameterName);

        GetSecretValueRequest valueRequest =
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .versionStage(AWS_CURRENT)
                        .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

        LOGGER.info(GET_STACK_SECRET_VALUE_LOG_FORMAT, secretId);

        return valueResponse.secretString();
    }

    public String getCommonSecretValue(String parameterName) {

        String secretId =
                String.format(PARAMETER_NAME_FORMAT, commonParameterPrefix, parameterName);

        GetSecretValueRequest valueRequest =
                GetSecretValueRequest.builder()
                        .secretId(secretId)
                        .versionStage(AWS_CURRENT)
                        .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

        LOGGER.info(GET_STACK_SECRET_VALUE_LOG_FORMAT, secretId);

        return valueResponse.secretString();
    }
}
