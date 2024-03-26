package uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;

@ExcludeFromGeneratedCoverageReport
public class DvaConfiguration {

    private final String endpointUri;
    private final String userName;
    private final String password;

    public DvaConfiguration(ParameterStoreService parameterStoreService) {

        this.endpointUri =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_ENDPOINT);
        this.userName =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_USERNAME);
        this.password =
                parameterStoreService.getParameterValue(ParameterStoreParameters.DVA_PASSWORD);
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
