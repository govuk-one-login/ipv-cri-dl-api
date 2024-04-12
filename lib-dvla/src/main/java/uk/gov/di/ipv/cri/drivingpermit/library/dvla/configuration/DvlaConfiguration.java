package uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String tokenEndpoint;
    private final String matchEndpoint;
    private final String changePasswordEndpoint;

    private final String apiKey;
    private final String username;
    private final String password;

    private final String tokenTableName;

    private final boolean passwordRotationEnabled;

    private final SecretsManagerService secretsManagerService;

    // TestDataStrategy mvp updates
    private final Map<String, String> endpointURLs;

    private final String tokenPath;
    private final String matchPath;
    private final String changePasswordPath;

    public DvlaConfiguration(
            ParameterStoreService parameterStoreService,
            SecretsManagerService secretsManagerService)
            throws JsonProcessingException {
        this.secretsManagerService = secretsManagerService;

        Map<String, String> dvlaParameterMap =
                parameterStoreService.getAllParametersFromPath(
                        ParameterPrefix.OVERRIDE, DVLA_PARAMETER_PATH);

        final String endpointUri = dvlaParameterMap.get("endpointUrl");
        ////////////////////////// TestStrategyMVP////////////////////////////////////
        this.endpointURLs = constructParameterMap(dvlaParameterMap.get("testStrategy/endpointUrl"));
        this.tokenPath = dvlaParameterMap.get("tokenPath");
        this.matchPath = dvlaParameterMap.get("matchPath");
        this.changePasswordPath = dvlaParameterMap.get("passwordPath");
        /////////////////////////////////////////////////////////////////////////////

        this.tokenEndpoint = String.format("%s%s", endpointUri, tokenPath);
        this.matchEndpoint = String.format("%s%s", endpointUri, matchPath);
        this.changePasswordEndpoint = String.format("%s%s", endpointUri, changePasswordPath);

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

    public Map<String, String> getEndpointURLs() {
        return endpointURLs;
    }

    public String getTokenPath() {
        return tokenPath;
    }

    public String getMatchPath() {
        return matchPath;
    }

    public String getChangePasswordPath() {
        return changePasswordPath;
    }

    private Map<String, String> constructParameterMap(String parameterValue)
            throws JsonProcessingException {
        if (null == parameterValue) {
            // null check is for testing DrivingPermitConfiguration creation purposes ONLY
            return Map.of(
                    "STUB",
                    "unassignedStubEndpoint",
                    "UAT",
                    "unassignedUatEndpoint",
                    "LIVE",
                    "unassignedLiveEndpoint");
        } else {
            return objectMapper.readValue(parameterValue, Map.class);
        }
    }
}
