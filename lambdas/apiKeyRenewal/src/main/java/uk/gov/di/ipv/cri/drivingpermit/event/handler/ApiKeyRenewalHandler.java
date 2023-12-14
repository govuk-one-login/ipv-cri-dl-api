package uk.gov.di.ipv.cri.drivingpermit.event.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangeApiKeyService;
import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_ERROR;

public class ApiKeyRenewalHandler implements RequestHandler<SecretsManagerRotationEvent, Void> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final String apiKey;
    private final SecretsManagerClient secretsManagerClient;
    private final ChangeApiKeyService changeApiKeyService;
    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;
    private final EventProbe eventProbe;

    @ExcludeFromGeneratedCoverageReport
    public ApiKeyRenewalHandler() {
        secretsManagerClient =
                SecretsManagerClient.builder()
                        .region(Region.EU_WEST_2)
                        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                        .build();
        ParameterStoreService parameterStoreService =
                new ParameterStoreService(ParamManager.getSsmProvider());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        eventProbe = new EventProbe();
        HttpRetryer httpRetryer = new HttpRetryer(HttpClients.custom().build(), eventProbe, 0);
        DvlaConfiguration dvlaConfiguration = new DvlaConfiguration(parameterStoreService);
        RequestConfig defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();
        String parameterPrefix =
                Optional.ofNullable(System.getenv("PARAMETER_PREFIX"))
                        .orElse(System.getenv("AWS_STACK_NAME"));
        this.apiKey = "/" + parameterPrefix + "/DVLA/apiKey";

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
                        DataStore.getClient(),
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
    }

    public ApiKeyRenewalHandler(
            String apiKey,
            SecretsManagerClient secretsManagerClient,
            ChangeApiKeyService changeApiKeyService,
            TokenRequestService tokenRequestService,
            EventProbe eventProbe,
            DriverMatchService driverMatchService) {
        this.apiKey = apiKey;
        this.secretsManagerClient = secretsManagerClient;
        this.changeApiKeyService = changeApiKeyService;
        this.tokenRequestService = tokenRequestService;
        this.eventProbe = eventProbe;
        this.driverMatchService = driverMatchService;
    }

    @Logging
    @Metrics(captureColdStart = true)
    public Void handleRequest(SecretsManagerRotationEvent input, Context context) {
        LOGGER.info("step {} secretId: {}", input.getStep(), input.getSecretId());

        SecretsManagerRotationStep.of(input.getStep())
                .ifPresent(
                        step -> {
                            try {
                                if (step == SecretsManagerRotationStep.CREATE_SECRET) {
                                    LOGGER.info(
                                            "{} commenced",
                                            SecretsManagerRotationStep.CREATE_SECRET);

                                    /*First step is for the new api token to be generated by DVLA, which will be labelled as 'AWSPENDING'
                                     * and stored in the Secrets Manager as the pending new api-key until it's been tested*/
                                    LOGGER.info("Generating a new api Key");
                                    String newApiKey = callDVLAApi();
                                    try {
                                        getValue(secretsManagerClient, input.getSecretId());
                                    } catch (SecretNotFoundException
                                            | ResourceNotFoundException e) {
                                        LOGGER.info(
                                                "If our value is null, we place the newly generated secret into Secrets Manager");
                                        PutSecretValueRequest secretRequest =
                                                PutSecretValueRequest.builder()
                                                        .secretId(input.getSecretId())
                                                        .secretString(newApiKey)
                                                        .versionStages("AWSPENDING")
                                                        .build();
                                        secretsManagerClient.putSecretValue(secretRequest);
                                    }
                                    LOGGER.info(
                                            "{} step is complete",
                                            SecretsManagerRotationStep.CREATE_SECRET);

                                } else if (step == SecretsManagerRotationStep.TEST_SECRET) {
                                    LOGGER.info(
                                            "{} commenced", SecretsManagerRotationStep.TEST_SECRET);
                                    /*Next step is to verify that the secret has been set in DVLA*/
                                    String testApiKey = getValue(secretsManagerClient, apiKey);
                                    if (testApiKey == null) {
                                        throw new RuntimeException(
                                                "Error as new password value was null when password retrieved from Secrets Manager");
                                    }
                                    driverMatchService.performMatch(
                                            drivingPermitForm(),
                                            tokenRequestService.requestToken(false),
                                            testApiKey);
                                    LOGGER.info(
                                            "{} step is complete",
                                            SecretsManagerRotationStep.TEST_SECRET);

                                } else if (step == SecretsManagerRotationStep.FINISH_SECRET) {
                                    LOGGER.info(
                                            "{} commenced",
                                            SecretsManagerRotationStep.FINISH_SECRET);
                                    /*Final step updates Secrets Manager to set api-key as AWSCURRENT*/
                                    LOGGER.info("Updating Secret in Secrets Manager");
                                    String newApiKey = getValue(secretsManagerClient, apiKey);
                                    if (newApiKey == null) {
                                        throw new RuntimeException(
                                                "Error as new password value was null when password retrieved from Secrets Manager");
                                    }
                                    updateSecret(
                                            input.getSecretId(),
                                            getValue(secretsManagerClient, apiKey));
                                    LOGGER.info(
                                            "{} step is complete",
                                            SecretsManagerRotationStep.FINISH_SECRET);
                                }
                            } catch (OAuthErrorResponseException e) {
                                // api-key Renewal Lambda Completed with an Error
                                eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                LOGGER.error(
                                        "Unhandled Exception while handling lambda {} exception {}",
                                        context.getFunctionName(),
                                        e.getClass());
                                LOGGER.error(e.getMessage(), e);

                                eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
                                throw new RuntimeException(e);
                            }
                        });
        return null;
    }

    private String callDVLAApi() throws OAuthErrorResponseException, JsonProcessingException {

        String authorization = tokenRequestService.requestToken(false);
        return changeApiKeyService.sendApiKeyChangeRequest(apiKey, authorization);
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
            LOGGER.debug(e);
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

        Address address = new Address();
        address.setPostalCode("BA2 5AA");
        address.setAddressCountry(GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        drivingPermit.setAddresses(List.of(address));

        return drivingPermit;
    }
}
