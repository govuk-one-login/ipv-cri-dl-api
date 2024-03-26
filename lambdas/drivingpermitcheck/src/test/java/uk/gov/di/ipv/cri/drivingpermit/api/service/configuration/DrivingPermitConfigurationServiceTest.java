package uk.gov.di.ipv.cri.drivingpermit.api.service.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class DrivingPermitConfigurationServiceTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ParameterStoreService mockParameterStoreService;

    @Mock private SecretsManagerService secretsManagerService;

    private DrivingPermitConfigurationService drivingPermitConfigurationService;

    @Test
    void shouldCreateDrivingPermitConfigurationService() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");
        // EnvVar feature toggles
        environmentVariables.set("DVA_PERFORMANCE_STUB_IN_USE", "false");
        environmentVariables.set("LOG_DVA_RESPONSE", "false");
        environmentVariables.set("DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG", "false");

        drivingPermitConfigurationService =
                new DrivingPermitConfigurationService(
                        mockParameterStoreService, secretsManagerService);

        assertNotNull(drivingPermitConfigurationService);

        boolean expectedDvaPerformanceStub =
                Boolean.parseBoolean(
                        environmentVariables.getVariables().get("DVA_PERFORMANCE_STUB_IN_USE"));
        assertEquals(
                expectedDvaPerformanceStub,
                drivingPermitConfigurationService.isDvaPerformanceStub());

        boolean expectedisLogDvaResponse =
                Boolean.parseBoolean(environmentVariables.getVariables().get("LOG_DVA_RESPONSE"));
        assertEquals(
                expectedisLogDvaResponse, drivingPermitConfigurationService.isLogDvaResponse());

        assertNotNull(drivingPermitConfigurationService.getDvaConfiguration());
        assertNotNull(drivingPermitConfigurationService.getDvlaConfiguration());
    }
}
