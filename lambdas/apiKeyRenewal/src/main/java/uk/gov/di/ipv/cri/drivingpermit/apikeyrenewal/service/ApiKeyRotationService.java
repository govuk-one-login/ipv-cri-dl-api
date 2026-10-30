package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeyDvlaHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeySecretsManagerClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.util.List;
import java.util.Optional;

import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.LIVE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.NO_CHANGE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.UAT;

public class ApiKeyRotationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyRotationService.class);

    private final ApiKeyRotationConfig config;
    private final ApiKeySecretsManagerClient apiKeySecretsManagerClient;
    private final ApiKeyDvlaHttpClient apiKeyDvlaHttpClient;
    private final TokenRequestService tokenRequestService;
    private final ApiKeyVerificationService apiKeyVerificationService;

    public ApiKeyRotationService(
            ApiKeyRotationConfig config,
            ApiKeySecretsManagerClient apiKeySecretsManagerClient,
            ApiKeyDvlaHttpClient apiKeyDvlaHttpClient,
            TokenRequestService tokenRequestService,
            ApiKeyVerificationService apiKeyVerificationService) {
        this.config = config;
        this.apiKeySecretsManagerClient = apiKeySecretsManagerClient;
        this.apiKeyDvlaHttpClient = apiKeyDvlaHttpClient;
        this.tokenRequestService = tokenRequestService;
        this.apiKeyVerificationService = apiKeyVerificationService;
    }

    public void rotate(String secretId)
            throws OAuthErrorResponseException, JsonProcessingException {
        if (!config.rotationEnabled()) {
            LOGGER.info("API key rotation is disabled, skipping");
            return;
        }

        String pendingApiKey = findOrCreatePendingApiKey(secretId);

        LOGGER.info("Verifying the pending API key");
        apiKeyVerificationService.verify(pendingApiKey);

        apiKeySecretsManagerClient.promoteToCurrent(secretId, pendingApiKey);
        List.of(NO_CHANGE, LIVE, UAT).forEach(tokenRequestService::removeTokenItem);
        LOGGER.info("Promoted the pending API key to current");
    }

    private String findOrCreatePendingApiKey(String secretId)
            throws OAuthErrorResponseException, JsonProcessingException {
        Optional<String> pendingFromPreviousRun = apiKeySecretsManagerClient.findPending(secretId);
        if (pendingFromPreviousRun.isPresent()) {
            LOGGER.info("Reusing the pending API key from a previous run");
            return pendingFromPreviousRun.get();
        }

        LOGGER.info("Requesting a new API key from DVLA");
        String newApiKey =
                apiKeyDvlaHttpClient.requestNewApiKey(
                        tokenRequestService.requestToken(true, NO_CHANGE));
        // DVLA starts retiring the previous key from here
        apiKeySecretsManagerClient.putPending(secretId, newApiKey);
        return newApiKey;
    }
}
