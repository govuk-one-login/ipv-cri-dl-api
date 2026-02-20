package uk.gov.di.ipv.cri.drivingpermit.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.parameters.ssm.SSMProvider;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.util.Map;

public class ParameterStoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterStoreService.class);
    private static final String LOG_MESSAGE_FORMAT = "Method {}, Prefix {}, Path {}, FullPath {}";

    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private final SSMProvider ssmProvider;

    public ParameterStoreService(SSMProvider ssmProvider) {

        this.ssmProvider = ssmProvider;
    }

    public String getParameterValue(ParameterPrefix prefix, String parameterName) {

        String parameterPath =
                String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), parameterName);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getParameterValue",
                prefix.getPrefixValue(),
                parameterName,
                parameterPath);

        return ssmProvider.get(parameterPath);
    }

    // Encrypted
    public String getEncryptedParameterValue(ParameterPrefix prefix, String parameterName) {

        String parameterPath =
                String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), parameterName);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getEncryptedParameterValue",
                prefix.getPrefixValue(),
                parameterName,
                parameterPath);

        return ssmProvider.withDecryption().get(parameterPath);
    }

    public Map<String, String> getAllParametersFromPath(ParameterPrefix prefix, String path) {

        String parametersPath = String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), path);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getAllParametersFromPath",
                prefix.getPrefixValue(),
                path,
                parametersPath);

        return ssmProvider.recursive().getMultiple(parametersPath);
    }

    // Encrypted
    public Map<String, String> getAllParametersFromPathWithDecryption(
            ParameterPrefix prefix, String path) {

        String parametersPath = String.format(PARAMETER_NAME_FORMAT, prefix.getPrefixValue(), path);

        LOGGER.info(
                LOG_MESSAGE_FORMAT,
                "getAllParametersFromPathWithDecryption",
                prefix.getPrefixValue(),
                path,
                parametersPath);

        return ssmProvider.withDecryption().getMultiple(parametersPath);
    }
}
