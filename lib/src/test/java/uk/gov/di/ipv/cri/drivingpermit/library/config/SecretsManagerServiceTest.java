package uk.gov.di.ipv.cri.drivingpermit.library.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class SecretsManagerServiceTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private SecretsManagerClient mockSecretsManagerClient;

    private void stubSecretResponse(String secretValue) {
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(GetSecretValueResponse.builder().secretString(secretValue).build());
    }

    private GetSecretValueRequest captureRequest() {
        ArgumentCaptor<GetSecretValueRequest> captor =
                ArgumentCaptor.forClass(GetSecretValueRequest.class);
        verify(mockSecretsManagerClient).getSecretValue(captor.capture());
        return captor.getValue();
    }

    @Test
    void getSecretValueUsesParameterPrefix() {
        environmentVariables.set("PARAMETER_PREFIX", "my-prefix");
        environmentVariables.set("AWS_STACK_NAME", "my-stack");
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", "common-prefix");

        stubSecretResponse("secret-value");

        SecretsManagerService service = new SecretsManagerService(mockSecretsManagerClient);
        String result = service.getSecretValue("my-param");

        assertEquals("secret-value", result);
        GetSecretValueRequest request = captureRequest();
        assertEquals("/my-prefix/my-param", request.secretId());
        assertEquals("AWSCURRENT", request.versionStage());
    }

    @Test
    void getSecretValueFallsBackToStackNameWhenParameterPrefixNotSet() {
        environmentVariables.set("AWS_STACK_NAME", "my-stack");
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", "common-prefix");

        stubSecretResponse("fallback-secret");

        SecretsManagerService service = new SecretsManagerService(mockSecretsManagerClient);
        String result = service.getSecretValue("my-param");

        assertEquals("fallback-secret", result);
        GetSecretValueRequest request = captureRequest();
        assertEquals("/my-stack/my-param", request.secretId());
    }

    @Test
    void getStackSecretValueUsesAwsStackName() {
        environmentVariables.set("PARAMETER_PREFIX", "my-prefix");
        environmentVariables.set("AWS_STACK_NAME", "my-stack");
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", "common-prefix");

        stubSecretResponse("stack-secret");

        SecretsManagerService service = new SecretsManagerService(mockSecretsManagerClient);
        String result = service.getStackSecretValue("stack-param");

        assertEquals("stack-secret", result);
        GetSecretValueRequest request = captureRequest();
        assertEquals("/my-stack/stack-param", request.secretId());
        assertEquals("AWSCURRENT", request.versionStage());
    }

    @Test
    void getCommonSecretValueUsesCommonParameterPrefix() {
        environmentVariables.set("PARAMETER_PREFIX", "my-prefix");
        environmentVariables.set("AWS_STACK_NAME", "my-stack");
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", "common-prefix");

        stubSecretResponse("common-secret");

        SecretsManagerService service = new SecretsManagerService(mockSecretsManagerClient);
        String result = service.getCommonSecretValue("common-param");

        assertEquals("common-secret", result);
        GetSecretValueRequest request = captureRequest();
        assertEquals("/common-prefix/common-param", request.secretId());
        assertEquals("AWSCURRENT", request.versionStage());
    }
}
