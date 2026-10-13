package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.SleepHelper;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyTestIdentity;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyVerificationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

public class ApiKeyVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyVerificationService.class);
    private static final Strategy STRATEGY =
            Strategy.NO_CHANGE; // Setting to default until test strategy approach agreed

    private final ApiKeyVerificationConfig config;
    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;
    private final SleepHelper sleepHelper;

    public ApiKeyVerificationService(
            ApiKeyVerificationConfig config,
            TokenRequestService tokenRequestService,
            DriverMatchService driverMatchService,
            SleepHelper sleepHelper) {
        this.config = config;
        this.tokenRequestService = tokenRequestService;
        this.driverMatchService = driverMatchService;
        this.sleepHelper = sleepHelper;
    }

    public void verify(String apiKey) throws OAuthErrorResponseException {
        LOGGER.info(
                "Suspending for {}ms before performing driver match",
                config.pauseBeforeMatch().toMillis());
        sleepHelper.busyWaitMilliseconds(Math.toIntExact(config.pauseBeforeMatch().toMillis()));

        driverMatchService.performMatch(
                ApiKeyTestIdentity.drivingPermitForm(),
                tokenRequestService.requestToken(true, STRATEGY),
                apiKey,
                STRATEGY);
    }
}
