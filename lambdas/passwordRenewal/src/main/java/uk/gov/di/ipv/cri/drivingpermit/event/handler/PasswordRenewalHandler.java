package uk.gov.di.ipv.cri.drivingpermit.event.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
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
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangePasswordService;
import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.UnauthorisedException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

import java.util.Optional;

import static org.passay.AllowedCharacterRule.ERROR_CODE;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_PASSWORD_RENEWAL_CHECK_COMPLETED_ERROR;

public class PasswordRenewalHandler implements RequestHandler<SecretsManagerRotationEvent, String> {

    private static final Logger LOGGER = LogManager.getLogger();
    public final String password;
    private final SecretsManagerClient secretsManagerClient;
    private final ChangePasswordService changePasswordService;
    private final TokenRequestService tokenRequestService;
    private final EventProbe eventProbe;
    private final DvlaConfiguration dvlaConfiguration;

    @ExcludeFromGeneratedCoverageReport
    public PasswordRenewalHandler() {
        secretsManagerClient =
                SecretsManagerClient.builder()
                        .region(Region.EU_WEST_2)
                        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                        .build();
        ParameterStoreService parameterStoreService =
                new ParameterStoreService((ParamManager.getSsmProvider()));
        SecretsManagerService secretsManagerService =
                new SecretsManagerService(secretsManagerClient);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        eventProbe = new EventProbe();
        HttpRetryer httpRetryer = new HttpRetryer(HttpClients.custom().build(), eventProbe, 0);
        dvlaConfiguration = new DvlaConfiguration(parameterStoreService, secretsManagerService);
        RequestConfig defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();
        this.password = "/" + System.getenv("AWS_STACK_NAME") + "/DVLA/password";
        changePasswordService =
                new ChangePasswordService(
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
    }

    public PasswordRenewalHandler(
            String password,
            SecretsManagerClient secretsManagerClient,
            ChangePasswordService changePasswordService,
            TokenRequestService tokenRequestService,
            EventProbe eventProbe,
            DvlaConfiguration dvlaConfiguration) {
        this.password = password;
        this.secretsManagerClient = secretsManagerClient;
        this.changePasswordService = changePasswordService;
        this.tokenRequestService = tokenRequestService;
        this.eventProbe = eventProbe;
        this.dvlaConfiguration = dvlaConfiguration;
    }

    @Logging
    @Metrics(captureColdStart = true)
    public String handleRequest(SecretsManagerRotationEvent input, Context context) {
        LOGGER.info("step {} secretId: {}", input.getStep(), input.getSecretId());

        Optional<SecretsManagerRotationStep> nullableStep =
                SecretsManagerRotationStep.of(input.getStep());
        if (nullableStep.isPresent()) {
            SecretsManagerRotationStep step = nullableStep.get();
            try {
                if (step == SecretsManagerRotationStep.FINISH_SECRET) {
                    // CREATE SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.CREATE_SECRET);
                    /*First step is for the new password to be generated, which will be labelled as 'AWSPENDING'
                     * and stored in the Secrets Manager as the pending new password until it's been verified by DVLA*/
                    LOGGER.info("Generating a new password");
                    String newPassword = generateNewPassword();
                    String passwordFromPreviousRun = null;
                    try {
                        passwordFromPreviousRun =
                                getValue(secretsManagerClient, input.getSecretId());
                    } catch (SecretNotFoundException | ResourceNotFoundException e) {
                        LOGGER.info(
                                "If the value is null, we place the new secret into Secrets Manager");
                        PutSecretValueRequest secretRequest =
                                PutSecretValueRequest.builder()
                                        .secretId(input.getSecretId())
                                        .secretString(newPassword)
                                        .versionStages("AWSPENDING")
                                        .build();
                        secretsManagerClient.putSecretValue(secretRequest);
                    }
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.CREATE_SECRET);
                    // CREATE SECRET END

                    // SET SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.SET_SECRET);
                    /*Second step is to retrieve the AWS Pending Password and to send it to DVLA*/
                    // Scenarios:
                    // 1: New password set as pending then fails to set in DVLA
                    // 2: New password set and updated in DVLA test fails
                    // 3: New password set and passess test but fails to update
                    // 4: New password set and updates successfully
                    // 1-1: Rerun runs again and tries to set previously pending value to DVLA
                    // 2-1: Rerun runs again and tries to set previously pending value to DVLA. DVLA
                    // returns 401 as saved password no longer matches password in DVLA. Test with
                    // pending password if that fails alarm
                    // 3-1: Rerun runs again and tries to set previously pending value to DVLA. DVLA
                    // returns 401 as saved password no longer matches password in DVLA. Test with
                    // pending password if that fails alarm
                    if (dvlaConfiguration.isPasswordRotationEnabled()) {
                        LOGGER.info(
                                "Testing if password has been updated as part of a previous run...");
                        if (passwordFromPreviousRun != null) {
                            try {
                                LOGGER.info(
                                        "Running DVLA password updated with the value from pending");
                                newPassword = passwordFromPreviousRun;
                                callDVLAApi(passwordFromPreviousRun);
                            } catch (UnauthorisedException e) {
                                LOGGER.info(
                                        "Unauthorised. Password or username has already been changed. Moving to testing");
                                // Do nothing password has already been changed. Continue to test
                            }
                        } else {
                            LOGGER.info(
                                    "Running DVLA password updated with a newly generated password");
                            callDVLAApi(newPassword);
                        }
                    }
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.SET_SECRET);
                    // SET SECRET END

                    // TEST SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.TEST_SECRET);
                    /*Third step is to verify that the secret has been set in DVLA
                     * To do so, we request a token to ensure the new password works, but this does
                     * not replace the token in the DB, and does not invalidate the existing one*/
                    if (dvlaConfiguration.isPasswordRotationEnabled()) {
                        LOGGER.info("Testing the new password against DVLA");
                        tokenRequestService.performNewTokenRequest(newPassword);
                        LOGGER.info("Token retrieved successfully");
                    }
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.TEST_SECRET);
                    // TEST SECRET END

                    // FINISH SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.FINISH_SECRET);
                    /*Final step updates Secrets Manager to set password as AWSCURRENT*/
                    LOGGER.info("Updating Secret in Secrets Manager");
                    updateSecret(input.getSecretId(), newPassword);
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.FINISH_SECRET);
                    // FINISH SECRET END
                }

            } catch (OAuthErrorResponseException e) {
                // Password Renewal Lambda Completed with an Error
                eventProbe.counterMetric(LAMBDA_PASSWORD_RENEWAL_CHECK_COMPLETED_ERROR);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOGGER.error(
                        "Unhandled Exception while handling lambda {} exception {}",
                        context.getFunctionName(),
                        e.getClass());
                LOGGER.error(e.getMessage(), e);

                eventProbe.counterMetric(LAMBDA_PASSWORD_RENEWAL_CHECK_COMPLETED_ERROR);
                throw new RuntimeException(e);
            }
        }
        return "Success";
    }

    private String generateNewPassword() {

        PasswordGenerator password = new PasswordGenerator();

        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(6);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(4);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitalRule = new CharacterRule(digitChars);
        digitalRule.setNumberOfCharacters(2);

        CharacterData specialChars =
                new CharacterData() {
                    public String getErrorCode() {
                        return ERROR_CODE;
                    }

                    public String getCharacters() {
                        return "!#$%*()+";
                    }
                };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);
        LOGGER.info("Password has been successfully generated");

        return password.generatePassword(
                14, splCharRule, lowerCaseRule, upperCaseRule, digitalRule);
    }

    private void callDVLAApi(String newPassword)
            throws OAuthErrorResponseException, UnauthorisedException {
        changePasswordService.sendPasswordChangeRequest(newPassword);
    }

    private void updateSecret(String secretId, String newPassword) {
        try {
            LOGGER.info("Creating a request for Secrets Manager");
            UpdateSecretRequest secretRequest =
                    UpdateSecretRequest.builder()
                            .secretId(secretId)
                            .secretString(newPassword)
                            .build();
            LOGGER.info("Secret Manager Request created");

            secretsManagerClient.updateSecret(secretRequest);
            LOGGER.info("Secrets Manager Request Sent");

        } catch (SecretsManagerException e) {
            LOGGER.error(
                    "Updatye value method returned Exception {}", e.getClass().getSimpleName());
            LOGGER.debug(e);
            throw e;
        }
    }

    private static String getValue(SecretsManagerClient secretsClient, String secretId) {

        try {
            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSPENDING")
                            .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            if (valueResponse == null || valueResponse.secretString() == null) {
                throw new SecretNotFoundException(
                        "Error as new password value was null when password retrieved from Secrets Manager");
            }
            LOGGER.info("The AWSPENDING password has been retrieved from Secrets Manager");
            return valueResponse.secretString();

        } catch (SecretsManagerException e) {
            LOGGER.error("Get value method returned Exception {}", e.getClass().getSimpleName());
            LOGGER.debug(e);
            throw e;
        }
    }
}
