package uk.gov.di.ipv.cri.drivingpermit.api.service.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;

public class DrivingPermitConfigurationService {

    // Feature toggles
    private final boolean isDvaPerformanceStub;
    private final boolean logDvaResponse;

    private final DvaConfiguration dvaConfiguration;
    private final DvlaConfiguration dvlaConfiguration;

    public DrivingPermitConfigurationService(
            ParameterStoreService parameterStoreService,
            SecretsManagerService secretsManagerService)
            throws JsonProcessingException {

        // ****************************Private Parameters****************************

        // *****************************Feature Toggles*******************************

        this.isDvaPerformanceStub =
                Boolean.parseBoolean(System.getenv("DVA_PERFORMANCE_STUB_IN_USE"));
        this.logDvaResponse = Boolean.parseBoolean(System.getenv("LOG_DVA_RESPONSE"));

        // **************************** DVA ****************************

        dvaConfiguration = new DvaConfiguration(parameterStoreService);

        // **************************** DVLA ****************************

        dvlaConfiguration = new DvlaConfiguration(parameterStoreService, secretsManagerService);
    }

    public boolean isDvaPerformanceStub() {
        return isDvaPerformanceStub;
    }

    public boolean isLogDvaResponse() {
        return logDvaResponse;
    }

    public DvaConfiguration getDvaConfiguration() {
        return dvaConfiguration;
    }

    public DvlaConfiguration getDvlaConfiguration() {
        return dvlaConfiguration;
    }
}
