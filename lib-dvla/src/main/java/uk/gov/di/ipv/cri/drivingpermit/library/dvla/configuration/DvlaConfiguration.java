package uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_API_KEY;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_MATCH;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_TOKEN;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_ENDPOINT_URL;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_PASSWORD;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_TOKEN_TABLE_NAME;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_USERNAME;

@ExcludeFromGeneratedCoverageReport
public class DvlaConfiguration {

    private final String tokenEndpoint;
    private final String matchEndpoint;

    private final String apiKey;
    private final String username;
    private final String password;

    private final String tokenTableName;

    public DvlaConfiguration(ParameterStoreService parameterStoreService) {

        final String endpointUri = parameterStoreService.getParameterValue(DVLA_ENDPOINT_URL);
        this.tokenEndpoint =
                String.format(
                        "%s%s",
                        endpointUri, parameterStoreService.getParameterValue(DVLA_ENDPOINT_TOKEN));
        this.matchEndpoint =
                String.format(
                        "%s%s",
                        endpointUri, parameterStoreService.getParameterValue(DVLA_ENDPOINT_MATCH));

        this.tokenTableName = parameterStoreService.getStackParameterValue(DVLA_TOKEN_TABLE_NAME);

        this.apiKey = parameterStoreService.getParameterValue(DVLA_API_KEY);
        this.username = parameterStoreService.getParameterValue(DVLA_USERNAME);
        this.password = parameterStoreService.getParameterValue(DVLA_PASSWORD);
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getMatchEndpoint() {
        return matchEndpoint;
    }

    public String getTokenTableName() {
        return tokenTableName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKey() {
        return apiKey;
    }
}
