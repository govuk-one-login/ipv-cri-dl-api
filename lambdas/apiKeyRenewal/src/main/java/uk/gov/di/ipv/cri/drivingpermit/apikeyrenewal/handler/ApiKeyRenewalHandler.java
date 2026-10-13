package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.FlushMetrics;
import uk.gov.account.ipv.cri.lime.limeade.util.LoggingSupport;
import uk.gov.account.ipv.cri.lime.limeade.util.timing.SleepHelper;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeyDvlaHttpClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.client.ApiKeySecretsManagerClient;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.exception.ApiKeyRotationException;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyRotationConfig;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.ApiKeyVerificationConfig;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service.ApiKeyRotationService;
import uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.service.ApiKeyVerificationService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;

import java.time.Duration;

import static uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.aws.SecretRotationStep.FINISH_SECRET;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_OK;

public class ApiKeyRenewalHandler implements RequestHandler<SecretsManagerRotationEvent, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyRenewalHandler.class);

    static {
        LoggingSupport.populateLambdaInitLoggerValues();
    }

    private static final Duration PAUSE_BEFORE_MATCH = Duration.ofSeconds(20);

    private final EventProbe eventProbe;
    private final ApiKeyRotationService apiKeyRotationService;

    public ApiKeyRenewalHandler() throws JsonProcessingException {
        ClientProviderFactory clientProviderFactory = new ClientProviderFactory();
        SecretsManagerClient secretsManagerClient = clientProviderFactory.getSecretsManagerClient();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        RequestConfig requestConfig = new HttpRequestConfig().getDefaultRequestConfig();

        eventProbe = new EventProbe();
        HttpRetryer httpRetryer = new HttpRetryer(HttpClients.custom().build(), eventProbe, 0);

        DvlaConfiguration dvlaConfiguration =
                new DvlaConfiguration(
                        new ParameterStoreService(clientProviderFactory.getSSMProvider()),
                        new SecretsManagerService(secretsManagerClient));

        TokenRequestService tokenRequestService =
                new TokenRequestService(
                        dvlaConfiguration,
                        clientProviderFactory.getDynamoDbEnhancedClient(),
                        httpRetryer,
                        requestConfig,
                        objectMapper,
                        eventProbe);

        DriverMatchService driverMatchService =
                new DriverMatchService(
                        dvlaConfiguration, httpRetryer, requestConfig, objectMapper, eventProbe);

        ApiKeyDvlaHttpClient apiKeyDvlaHttpClient =
                new ApiKeyDvlaHttpClient(
                        dvlaConfiguration, httpRetryer, requestConfig, objectMapper, eventProbe);

        ApiKeyVerificationService apiKeyVerificationService =
                new ApiKeyVerificationService(
                        new ApiKeyVerificationConfig(PAUSE_BEFORE_MATCH),
                        tokenRequestService,
                        driverMatchService,
                        new SleepHelper(PAUSE_BEFORE_MATCH.toMillis()));

        apiKeyRotationService =
                new ApiKeyRotationService(
                        new ApiKeyRotationConfig(dvlaConfiguration.isApiKeyRotationEnabled()),
                        new ApiKeySecretsManagerClient(secretsManagerClient),
                        apiKeyDvlaHttpClient,
                        tokenRequestService,
                        apiKeyVerificationService);
    }

    @Logging(clearState = true)
    @FlushMetrics(captureColdStart = true)
    public String handleRequest(SecretsManagerRotationEvent input, Context context) {
        MDC.put("step", input.getStep());
        MDC.put("secret_id", input.getSecretId());
        MDC.put("client_request_token", input.getClientRequestToken());

        if (!FINISH_SECRET.value().equals(input.getStep())) {
            LOGGER.info("Nothing to do for this rotation step");
            return completedOk();
        }

        try {
            LOGGER.info("API key rotation started");
            apiKeyRotationService.rotate(input.getSecretId());
            LOGGER.info("API key rotation completed");
            return completedOk();
        } catch (Exception e) {
            LOGGER.error("API key rotation failed", e);
            eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
            throw new ApiKeyRotationException(e);
        }
    }

    private String completedOk() {
        eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_OK);
        return "Success";
    }
}
