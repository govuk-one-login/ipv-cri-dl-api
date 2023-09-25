package uk.gov.di.ipv.cri.drivingpermit.api.service.configuration;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.CONTRAINDICATION_MAPPINGS;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TABLE_NAME;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DOCUMENT_CHECK_RESULT_TTL_PARAMETER;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVA_DIRECT_ENABLED;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.DVLA_DIRECT_ENABLED;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.IS_DCS_PERFORMANCE_STUB;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.IS_DVA_PERFORMANCE_STUB;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.IS_DVLA_PERFORMANCE_STUB;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.LOG_DCS_RESPONSE;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.LOG_DVA_RESPONSE;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters.MAXIMUM_ATTEMPT_COUNT;

public class ConfigurationService {

    private final Clock clock;

    // Feature toggles
    private final boolean devEnvironmentOnlyEnhancedDebug;
    private final boolean dvaDirectEnabled;
    private final boolean dvlaDirectEnabled;
    private final boolean isDcsPerformanceStub;
    private final boolean isDvaPerformanceStub;
    private final boolean isDvlaPerformanceStub;
    private final boolean logDcsResponse;
    private final boolean logDvaResponse;

    private final String documentCheckResultTableName;
    private final long documentCheckItemTtl;

    private final int maxAttempts;

    private final String contraindicationMappings;

    private final DcsConfiguration dcsConfiguration;
    private final DvaConfiguration dvaConfiguration;
    private final DvlaConfiguration dvlaConfiguration;

    public ConfigurationService(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        this.clock = Clock.systemUTC();

        // ****************************Private Parameters****************************

        this.contraindicationMappings =
                parameterStoreService.getParameterValue(CONTRAINDICATION_MAPPINGS);

        this.documentCheckResultTableName =
                parameterStoreService.getStackParameterValue(DOCUMENT_CHECK_RESULT_TABLE_NAME);

        this.documentCheckItemTtl =
                Long.parseLong(
                        parameterStoreService.getCommonParameterValue(
                                DOCUMENT_CHECK_RESULT_TTL_PARAMETER));

        this.maxAttempts =
                Integer.parseInt(
                        parameterStoreService.getStackParameterValue(MAXIMUM_ATTEMPT_COUNT));

        // *****************************Feature Toggles*******************************

        this.devEnvironmentOnlyEnhancedDebug =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(
                                DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG));

        this.dvaDirectEnabled =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(DVA_DIRECT_ENABLED));

        this.dvlaDirectEnabled =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(DVLA_DIRECT_ENABLED));

        this.isDcsPerformanceStub =
                Boolean.parseBoolean(
                        parameterStoreService.getParameterValue(IS_DCS_PERFORMANCE_STUB));
        this.isDvaPerformanceStub =
                Boolean.parseBoolean(
                        parameterStoreService.getParameterValue(IS_DVA_PERFORMANCE_STUB));
        this.isDvlaPerformanceStub =
                Boolean.parseBoolean(
                        parameterStoreService.getParameterValue(IS_DVLA_PERFORMANCE_STUB));

        this.logDcsResponse =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(LOG_DCS_RESPONSE));

        this.logDvaResponse =
                Boolean.parseBoolean(
                        parameterStoreService.getStackParameterValue(LOG_DVA_RESPONSE));

        // **************************** DCS ****************************

        dcsConfiguration = new DcsConfiguration(parameterStoreService);

        // **************************** DVA ****************************

        dvaConfiguration = new DvaConfiguration(parameterStoreService);

        // **************************** DVLA ****************************

        dvlaConfiguration = new DvlaConfiguration(parameterStoreService);
    }

    public String getDocumentCheckResultTableName() {
        return documentCheckResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isDevEnvironmentOnlyEnhancedDebugSet() {
        return devEnvironmentOnlyEnhancedDebug;
    }

    public boolean isDcsPerformanceStub() {
        return isDcsPerformanceStub;
    }

    public boolean isDvaPerformanceStub() {
        return isDvaPerformanceStub;
    }

    public boolean isDvlaPerformanceStub() {
        return isDvlaPerformanceStub;
    }

    public boolean getDvaDirectEnabled() {
        return dvaDirectEnabled;
    }

    public boolean getDvlaDirectEnabled() {
        return dvlaDirectEnabled;
    }

    public boolean isLogDcsResponse() {
        return logDcsResponse;
    }

    public boolean isLogDvaResponse() {
        return logDvaResponse;
    }

    public DcsConfiguration getDcsConfiguration() {
        return dcsConfiguration;
    }

    public DvaConfiguration getDvaConfiguration() {
        return dvaConfiguration;
    }

    public DvlaConfiguration getDvlaConfiguration() {
        return dvlaConfiguration;
    }

    public long getDocumentCheckItemExpirationEpoch() {
        return clock.instant().plus(documentCheckItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }
}
