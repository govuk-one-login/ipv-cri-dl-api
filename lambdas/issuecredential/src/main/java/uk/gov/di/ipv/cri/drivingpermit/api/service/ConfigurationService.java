package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TABLE_NAME;

public class ConfigurationService {

    private final String documentCheckResultTableName;
    private final String parameterPrefix;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");
        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.documentCheckResultTableName =
                paramProvider.get(getParameterName(DOCUMENT_CHECK_RESULT_TABLE_NAME));
    }

    public String getDocumentCheckResultTableName() {
        return documentCheckResultTableName;
    }

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }
}
