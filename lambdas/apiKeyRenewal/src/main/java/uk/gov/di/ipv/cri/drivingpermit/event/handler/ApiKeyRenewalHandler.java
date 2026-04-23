package uk.gov.di.ipv.cri.drivingpermit.event.handler;

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
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.FlushMetrics;
import uk.gov.account.ipv.cri.lime.limeade.strategy.Strategy;
import uk.gov.account.ipv.cri.lime.limeade.util.LoggingSupport;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangeApiKeyService;
import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.event.util.PauseHelper;
import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_OK;

public class ApiKeyRenewalHandler implements RequestHandler<SecretsManagerRotationEvent, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyRenewalHandler.class);

    static {
        LoggingSupport.populateLambdaInitLoggerValues();
    }

    public static final int SLEEP_DURATION_MS = 20000;
    private final SecretsManagerClient secretsManagerClient;
    private final ChangeApiKeyService changeApiKeyService;
    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;
    private final EventProbe eventProbe;
    private final DvlaConfiguration dvlaConfiguration;
    private final PauseHelper pauseHelper;

    @ExcludeFromGeneratedCoverageReport
    public ApiKeyRenewalHandler() throws JsonProcessingException {
        ClientProviderFactory clientProviderFactory = new ClientProviderFactory();

        secretsManagerClient = clientProviderFactory.getSecretsManagerClient();
        ParameterStoreService parameterStoreService =
                new ParameterStoreService(clientProviderFactory.getSSMProvider());
        SecretsManagerService secretsManagerService =
                new SecretsManagerService(secretsManagerClient);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        eventProbe = new EventProbe();
        HttpRetryer httpRetryer = new HttpRetryer(HttpClients.custom().build(), eventProbe, 0);
        dvlaConfiguration = new DvlaConfiguration(parameterStoreService, secretsManagerService);
        RequestConfig defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();

        changeApiKeyService =
                new ChangeApiKeyService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
        tokenRequestService =
                new TokenRequestService(
                        dvlaConfiguration,
                        clientProviderFactory.getDynamoDbEnhancedClient(),
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
        driverMatchService =
                new DriverMatchService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
        pauseHelper = new PauseHelper(SLEEP_DURATION_MS);
    }

    public ApiKeyRenewalHandler(
            SecretsManagerClient secretsManagerClient,
            ChangeApiKeyService changeApiKeyService,
            TokenRequestService tokenRequestService,
            DriverMatchService driverMatchService,
            EventProbe eventProbe,
            DvlaConfiguration dvlaConfiguration,
            PauseHelper pauseHelper) {
        this.secretsManagerClient = secretsManagerClient;
        this.changeApiKeyService = changeApiKeyService;
        this.tokenRequestService = tokenRequestService;
        this.driverMatchService = driverMatchService;
        this.eventProbe = eventProbe;
        this.dvlaConfiguration = dvlaConfiguration;
        this.pauseHelper = pauseHelper;
    }

    @Logging(clearState = true)
    @FlushMetrics(captureColdStart = true)
    public String handleRequest(SecretsManagerRotationEvent input, Context context) {

        try {
            LOGGER.info("step {} secretId: {}", input.getStep(), input.getSecretId());

            Optional<SecretsManagerRotationStep> nullableStep =
                    SecretsManagerRotationStep.of(input.getStep());

            if (nullableStep.isEmpty()
                    || nullableStep.get() != SecretsManagerRotationStep.FINISH_SECRET) {
                return completedOk();
            }
            String pendingNewApiKey = executeCreateSecretStep(input.getSecretId());
            executeTestSecretStep(pendingNewApiKey);
            executeFinishSecretStep(input.getSecretId(), pendingNewApiKey);
            return completedOk();
        } catch (InterruptedException e) {
            LOGGER.error("API key rotation interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.error(
                    "API key rotation failed in lambda {}: {}",
                    context.getFunctionName(),
                    e.getMessage(),
                    e);
            eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
            throw new RuntimeException(e);
        }
    }

    private String completedOk() {
        // Lambda Complete No Error
        eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_OK);
        return "Success";
    }

    private String executeCreateSecretStep(String secretId)
            throws OAuthErrorResponseException, JsonProcessingException {
        logStepCommenced(SecretsManagerRotationStep.CREATE_SECRET);
        String pendingNewApiKey = retrieveOrCreateApiKey(secretId);
        logStepCompleted(SecretsManagerRotationStep.CREATE_SECRET);
        return pendingNewApiKey;
    }

    private String retrieveOrCreateApiKey(String secretId)
            throws OAuthErrorResponseException, JsonProcessingException {
        try {
            String apiKeyFromPreviousRun = getValue(secretsManagerClient, secretId);
            LOGGER.info("Found existing API Key in Secrets Manager, advancing to test");
            return apiKeyFromPreviousRun;
        } catch (SecretNotFoundException | ResourceNotFoundException _) {
            return createNewApiKeyIfEnabled(secretId);
        }
    }

    private String createNewApiKeyIfEnabled(String secretId)
            throws OAuthErrorResponseException, JsonProcessingException {
        if (!dvlaConfiguration.isApiKeyRotationEnabled()) {
            return null;
        }

        LOGGER.info("'AWSPENDING' value is null, requesting a new API Key from DVLA");
        String pendingNewApiKey = callDVLAApi();
        // After this point, previous apiKey is no longer valid with new token
        LOGGER.debug("The new API Key is {}", pendingNewApiKey);

        PutSecretValueRequest secretRequest =
                PutSecretValueRequest.builder()
                        .secretId(secretId)
                        .secretString(pendingNewApiKey)
                        .versionStages("AWSPENDING")
                        .build();
        secretsManagerClient.putSecretValue(secretRequest);
        LOGGER.info("Secret has been stored in AWS successfully");

        return pendingNewApiKey;
    }

    private void executeTestSecretStep(String pendingNewApiKey)
            throws OAuthErrorResponseException, InterruptedException {
        logStepCommenced(SecretsManagerRotationStep.TEST_SECRET);

        if (dvlaConfiguration.isApiKeyRotationEnabled() && pendingNewApiKey != null) {
            Strategy strategy =
                    Strategy.NO_CHANGE; // Setting to default until test strategy approach agreed
            pauseHelper.pause();
            driverMatchService.performMatch(
                    drivingPermitForm(),
                    tokenRequestService.requestToken(true, strategy),
                    pendingNewApiKey,
                    strategy);
        }

        logStepCompleted(SecretsManagerRotationStep.TEST_SECRET);
    }

    private void executeFinishSecretStep(String secretId, String pendingNewApiKey) {
        logStepCommenced(SecretsManagerRotationStep.FINISH_SECRET);

        if (dvlaConfiguration.isApiKeyRotationEnabled()) {
            updateSecret(secretId, pendingNewApiKey);
            LOGGER.info("Updating Secret in Secrets Manager");
            tokenRequestService.removeTokenItem(Strategy.NO_CHANGE);
            tokenRequestService.removeTokenItem(Strategy.LIVE);
            tokenRequestService.removeTokenItem(Strategy.UAT);
        }

        logStepCompleted(SecretsManagerRotationStep.FINISH_SECRET);
    }

    private String callDVLAApi() throws OAuthErrorResponseException, JsonProcessingException {

        String tokenValue = tokenRequestService.requestToken(true, Strategy.NO_CHANGE);
        return changeApiKeyService.sendApiKeyChangeRequest(
                dvlaConfiguration.getApiKey(), tokenValue);
    }

    private void updateSecret(String secretId, String newApiKey) {
        try {
            LOGGER.info("Creating a request for Secrets Manager");
            UpdateSecretRequest secretRequest =
                    UpdateSecretRequest.builder()
                            .secretId(secretId)
                            .secretString(newApiKey)
                            .build();
            LOGGER.info("Secret Manager Request created");

            secretsManagerClient.updateSecret(secretRequest);
            LOGGER.info("Secrets Manager Request Sent");

        } catch (SecretsManagerException e) {
            LOGGER.error(e.awsErrorDetails().errorMessage(), e);
            LOGGER.debug("Error: ", e);
            throw e;
        }
    }

    public static String getValue(SecretsManagerClient secretsClient, String secretId) {

        try {
            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSPENDING")
                            .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            if (valueResponse == null || valueResponse.secretString() == null) {
                throw new SecretNotFoundException(
                        "Error as new api key value was null when api key retrieved from Secrets Manager");
            }
            LOGGER.info("The AWSPENDING api-key has been retrieved from Secrets Manager");
            return valueResponse.secretString();

        } catch (SecretsManagerException e) {
            LOGGER.error("Get value method returned Exception {}", e.getClass().getSimpleName());
            LOGGER.debug("Error: ", e);
            throw e;
        }
    }

    public DrivingPermitForm drivingPermitForm() {
        DrivingPermitForm drivingPermit = new DrivingPermitForm();
        drivingPermit.setLicenceIssuer("DVLA");

        drivingPermit.setForenames(List.of("KENNETH"));
        drivingPermit.setSurname("DECERQUEIRA");

        drivingPermit.setDateOfBirth(LocalDate.of(1965, 7, 8));

        drivingPermit.setIssueDate(LocalDate.of(2018, 4, 19));
        drivingPermit.setExpiryDate(LocalDate.of(2042, 10, 1));
        drivingPermit.setDrivingLicenceNumber("DECER607085KE9LN");
        drivingPermit.setIssueNumber("16");

        Address address = new Address();
        address.setPostalCode("BA2 5AA");
        address.setAddressCountry(GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        drivingPermit.setAddresses(List.of(address));

        return drivingPermit;
    }

    private void logStepCommenced(SecretsManagerRotationStep step) {
        LOGGER.info("{} commenced", step);
    }

    private void logStepCompleted(SecretsManagerRotationStep step) {
        LOGGER.info("{}  step is complete ", step);
    }
}
