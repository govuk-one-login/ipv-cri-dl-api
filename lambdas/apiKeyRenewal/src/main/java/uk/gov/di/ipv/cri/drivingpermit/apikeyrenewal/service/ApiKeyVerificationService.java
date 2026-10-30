package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.SleepHelper;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyTestIdentity;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyVerificationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.time.Duration;

public class ApiKeyVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyVerificationService.class);
    private static final Strategy STRATEGY =
            Strategy.NO_CHANGE; // setting to default until test strategy approach agreed

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
        String tokenValue = tokenRequestService.requestToken(true, STRATEGY);
        Duration delay = config.initialDelay();

        for (int attempt = 1; ; attempt++) {
            LOGGER.info(
                    "Suspending for {}ms before driver match attempt {}",
                    delay.toMillis(),
                    attempt);
            sleepHelper.busyWaitMilliseconds(Math.toIntExact(delay.toMillis()));

            try {
                driverMatchService.performMatch(
                        ApiKeyTestIdentity.drivingPermitForm(), tokenValue, apiKey, STRATEGY);
                LOGGER.atInfo()
                        .setMessage("Verified the pending API key")
                        .addKeyValue("attempt", attempt)
                        .log();
                return;
            } catch (OAuthErrorResponseException | DVLAMatchUnauthorizedException e) {
                if (attempt >= config.maxAttempts()) {
                    throw e;
                }
                LOGGER.atWarn()
                        .setMessage("Driver match with the pending API key failed, retrying")
                        .addKeyValue("attempt", attempt)
                        .addKeyValue("max_attempts", config.maxAttempts())
                        .setCause(e)
                        .log();
                delay = nextDelay(delay);
            }
        }
    }

    private Duration nextDelay(Duration delay) {
        Duration doubled = delay.multipliedBy(2);
        return doubled.compareTo(config.maxDelay()) < 0 ? doubled : config.maxDelay();
    }
}
