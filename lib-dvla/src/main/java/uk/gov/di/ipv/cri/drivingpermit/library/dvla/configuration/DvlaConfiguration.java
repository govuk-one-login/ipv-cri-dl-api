package uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration;

import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_API_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_API_KEY_ROTATION_ENABLED;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_API_KEY_PATH;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_MATCH;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_PASSWORD_PATH;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_TOKEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_URL;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_PASSWORD;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_PASSWORD_ROTATION_ENABLED;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_TOKEN_TABLE_NAME;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_USERNAME;

@ExcludeFromGeneratedCoverageReport
public class DvlaConfiguration {

    private final String tokenEndpoint;
    private final String matchEndpoint;
    private final String changePasswordEndpoint;
    private final String changeApiKeyEndpoint;

    private final String apiKey;
    private final String username;
    private final String password;

    private final String tokenTableName;

    private final boolean passwordRotationEnabled;
    private final boolean apiKeyRotationEnabled;

    private final SecretsManagerService secretsManagerService;

    public DvlaConfiguration(
            ParameterStoreService parameterStoreService,
            SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;

        final String endpointUri = parameterStoreService.getParameterValue(DVLA_ENDPOINT_URL);
        this.tokenEndpoint =
                String.format(
                        "%s%s",
                        endpointUri, parameterStoreService.getParameterValue(DVLA_ENDPOINT_TOKEN));
        this.matchEndpoint =
                String.format(
                        "%s%s",
                        endpointUri, parameterStoreService.getParameterValue(DVLA_ENDPOINT_MATCH));
        this.changePasswordEndpoint =
                String.format(
                        "%s%s",
                        endpointUri,
                        parameterStoreService.getParameterValue(DVLA_ENDPOINT_PASSWORD_PATH));
        this.changeApiKeyEndpoint =
                String.format(
                        "%s%s",
                        endpointUri,
                        parameterStoreService.getParameterValue(DVLA_ENDPOINT_API_KEY_PATH));
        this.tokenTableName = parameterStoreService.getStackParameterValue(DVLA_TOKEN_TABLE_NAME);

        this.apiKey = parameterStoreService.getParameterValue(DVLA_API_KEY);
        this.username = parameterStoreService.getParameterValue(DVLA_USERNAME);

        this.passwordRotationEnabled =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(
                                DVLA_PASSWORD_ROTATION_ENABLED));
        this.apiKeyRotationEnabled =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(
                                DVLA_API_KEY_ROTATION_ENABLED));

        this.password = parameterStoreService.getParameterValue(DVLA_PASSWORD);
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getMatchEndpoint() {
        return matchEndpoint;
    }

    public String getChangePasswordEndpoint() {
        return changePasswordEndpoint;
    }

    public String getChangeApiKeyEndpoint() {
        return changeApiKeyEndpoint;
    }

    public String getTokenTableName() {
        return tokenTableName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        if (isPasswordRotationEnabled()) {
            try {
                return secretsManagerService.getStackSecretValue(DVLA_PASSWORD);
            } catch (ResourceNotFoundException e) {
                return password;
            }
        } else {
            return password;
        }
    }

    public String getApiKey() {
        if (isApiKeyRotationEnabled()) {
            try {
                return secretsManagerService.getStackSecretValue(DVLA_API_KEY);
            } catch (ResourceNotFoundException e) {
                return apiKey;
            }
        } else {
            return apiKey;
        }
    }

    public boolean isPasswordRotationEnabled() {
        return passwordRotationEnabled;
    }

    public boolean isApiKeyRotationEnabled() {
        return apiKeyRotationEnabled;
    }
}
