package uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration;

import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_PASSWORD_SECRET;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_TOKEN_TABLE_NAME;

@ExcludeFromGeneratedCoverageReport
public class DvlaConfiguration {

    private static final String DVLA_PARAMETER_PATH = "DVLA";

    private final String tokenEndpoint;
    private final String matchEndpoint;
    private final String changePasswordEndpoint;

    private final String apiKey;
    private final String username;
    private final String password;

    private final String tokenTableName;

    private final boolean passwordRotationEnabled;

    private final SecretsManagerService secretsManagerService;

    public DvlaConfiguration(
            ParameterStoreService parameterStoreService,
            SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;

        Map<String, String> dvlaParameterMap =
                parameterStoreService.getAllParametersFromPath(
                        ParameterPrefix.OVERRIDE, DVLA_PARAMETER_PATH);

        final String endpointUri = dvlaParameterMap.get("endpointUrl");

        this.tokenEndpoint = String.format("%s%s", endpointUri, dvlaParameterMap.get("tokenPath"));
        this.matchEndpoint = String.format("%s%s", endpointUri, dvlaParameterMap.get("matchPath"));
        this.changePasswordEndpoint =
                String.format("%s%s", endpointUri, dvlaParameterMap.get("passwordPath"));

        this.apiKey = dvlaParameterMap.get("apiKey");
        this.username = dvlaParameterMap.get("username");
        this.password = dvlaParameterMap.get("password");

        // Must be a stack param
        this.tokenTableName =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.STACK, DVLA_TOKEN_TABLE_NAME);

        this.passwordRotationEnabled =
                Boolean.parseBoolean(System.getenv("DVLA_PASSWORD_ROTATION_ENABLED"));
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

    public String getTokenTableName() {
        return tokenTableName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        if (isPasswordRotationEnabled()) {
            try {
                return secretsManagerService.getStackSecretValue(DVLA_PASSWORD_SECRET);
            } catch (ResourceNotFoundException e) {
                return password;
            }
        } else {
            return password;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isPasswordRotationEnabled() {
        return passwordRotationEnabled;
    }
}
