package uk.gov.di.ipv.cri.drivingpermit.api.service.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;

public class ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Clock clock;

    // Feature toggles
    private final boolean dvaDirectEnabled;
    private final boolean dvlaDirectEnabled;
    private final boolean isPerformanceStub;
    private final boolean logThirdPartyResponse;

    private final String documentCheckResultTableName;
    private final long documentCheckItemTtl;

    private final String contraindicationMappings;

    private final DcsConfiguration dcsConfiguration;
    private final DvaConfiguration dvaConfiguration;

    public ConfigurationService(ParameterStoreService parameterStoreService)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

        this.clock = Clock.systemUTC();

        // ****************************Private Parameters****************************

        this.contraindicationMappings =
                parameterStoreService.getParameter("contraindicationMappings");

        this.documentCheckResultTableName =
                parameterStoreService.getStackParameter("DocumentCheckResultTableName");

        this.documentCheckItemTtl =
                Long.parseLong(parameterStoreService.getCommonParameterName("SessionTtl"));

        // *****************************Feature Toggles*******************************

        this.dvaDirectEnabled =
                Boolean.parseBoolean(parameterStoreService.getParameter("dvaDirectEnabled"));

        this.dvlaDirectEnabled =
                Boolean.parseBoolean(parameterStoreService.getParameter("dvlaDirectEnabled"));

        this.isPerformanceStub =
                Boolean.parseBoolean(parameterStoreService.getParameter("isPerformanceStub"));

        this.logThirdPartyResponse =
                Boolean.parseBoolean(parameterStoreService.getParameter("logDcsResponse"));

        // **************************** DCS ****************************

        dcsConfiguration = new DcsConfiguration(parameterStoreService);

        // **************************** DVA ****************************

        dvaConfiguration = new DvaConfiguration(parameterStoreService);

        // **************************** DVLA ****************************

        // TODO dvlaConfiguration = new DvlaConfiguration(parameterStoreService);
    }

    public String getDocumentCheckResultTableName() {
        return documentCheckResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public boolean isPerformanceStub() {
        return isPerformanceStub;
    }

    public boolean isLogThirdPartyResponse() {
        return logThirdPartyResponse;
    }

    public boolean getDvaDirectEnabled() {
        return dvaDirectEnabled;
    }

    public boolean getDvlaDirectEnabled() {
        return dvlaDirectEnabled;
    }

    public DcsConfiguration getDcsConfiguration() {
        return dcsConfiguration;
    }

    public DvaConfiguration getDvaConfiguration() {
        return dvaConfiguration;
    }

    public long getDocumentCheckItemExpirationEpoch() {
        return clock.instant().plus(documentCheckItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }
}
