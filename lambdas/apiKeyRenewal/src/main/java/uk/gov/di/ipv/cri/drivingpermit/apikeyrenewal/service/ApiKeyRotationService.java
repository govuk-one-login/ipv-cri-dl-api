package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeyDvlaHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeySecretsManagerClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationConfig;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.util.List;
import java.util.Optional;

import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.LIVE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.NO_CHANGE;
import static uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy.UAT;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.ALREADY_CURRENT;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.DISABLED;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.PROMOTED_UNVERIFIED;
import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationOutcome.PROMOTED_VERIFIED;

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

    public ApiKeyRotationOutcome rotate(String secretId, String versionId)
            throws OAuthErrorResponseException, JsonProcessingException {
        if (!config.rotationEnabled()) {
            LOGGER.info("API key rotation is disabled, skipping");
            return DISABLED;
        }

        if (apiKeySecretsManagerClient.isCurrent(secretId, versionId)) {
            LOGGER.info("The API key for this rotation is already current");
            clearCachedTokens();
            return ALREADY_CURRENT;
        }

        String pendingApiKey = findOrCreatePendingApiKey(secretId, versionId);
        boolean verified = tryVerify(pendingApiKey);

        apiKeySecretsManagerClient.promoteToCurrent(secretId, versionId);
        clearCachedTokens();
        LOGGER.info("Promoted the pending API key to current");
        return verified ? PROMOTED_VERIFIED : PROMOTED_UNVERIFIED;
    }

    private String findOrCreatePendingApiKey(String secretId, String versionId)
            throws OAuthErrorResponseException, JsonProcessingException {
        Optional<String> pendingFromThisRotation =
                apiKeySecretsManagerClient.findPending(secretId, versionId);
        if (pendingFromThisRotation.isPresent()) {
            LOGGER.info("Reusing the pending API key from an earlier attempt at this rotation");
            return pendingFromThisRotation.get();
        }

        LOGGER.info("Requesting a new API key from DVLA");
        String newApiKey =
                apiKeyDvlaHttpClient.requestNewApiKey(
                        tokenRequestService.requestToken(true, NO_CHANGE));
        // DVLA starts retiring the previous key from here
        apiKeySecretsManagerClient.putPending(secretId, versionId, newApiKey);
        return newApiKey;
    }

    private boolean tryVerify(String pendingApiKey) {
        LOGGER.info("Verifying the pending API key");
        try {
            apiKeyVerificationService.verify(pendingApiKey);
            return true;
        } catch (OAuthErrorResponseException | RuntimeException e) {
            LOGGER.atError()
                    .setMessage("Could not verify the pending API key, promoting it unverified")
                    .setCause(e)
                    .log();
            return false;
        }
    }

    private void clearCachedTokens() {
        List.of(NO_CHANGE, UAT, LIVE).forEach(tokenRequestService::removeTokenItem);
    }
}
