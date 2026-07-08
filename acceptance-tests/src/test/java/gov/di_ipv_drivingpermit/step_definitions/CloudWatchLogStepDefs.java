package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DLCommonPageObject;
import gov.di_ipv_drivingpermit.pages.DrivingLicenceAPIPage;
import gov.di_ipv_drivingpermit.utilities.CloudWatchLogService;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloudWatchLogStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchLogStepDefs.class);

    private final CloudWatchLogService cloudWatchLogService = new CloudWatchLogService();

    private final String stackName = System.getenv("AWS_STACK_NAME");

    @Then("the {string} lambda logs should contain {string}")
    public void lambdaLogsShouldContain(String logGroupLogicalId, String expectedMessage) {
        if (stackName == null) {
            LOGGER.info("Skipping CloudWatch log assertion — AWS_STACK_NAME not set");
            return;
        }

        String sessionId = DrivingLicenceAPIPage.getSessionId();
        List<String> logMessages;

        if (sessionId != null) {
            logMessages =
                    cloudWatchLogService.getLogMessages(
                            stackName,
                            logGroupLogicalId,
                            sessionId,
                            DrivingLicenceAPIPage.getTestStartTime());
        } else {
            String txn = DLCommonPageObject.getVcTxn();
            LOGGER.info("No sessionId available, falling back to txn-based lookup: '{}'", txn);
            logMessages =
                    cloudWatchLogService.getLogMessagesByTxn(
                            stackName,
                            logGroupLogicalId,
                            txn,
                            DLCommonPageObject.getFeTestStartTime());
        }

        logMessages.forEach(message -> LOGGER.info("  {}", message));

        boolean found = logMessages.stream().anyMatch(message -> message.contains(expectedMessage));

        LOGGER.info(
                "Log assertion for message '{}': {}",
                expectedMessage,
                found ? "FOUND" : "NOT FOUND");

        assertTrue(
                found,
                "Expected log message not found in '"
                        + logGroupLogicalId
                        + "' logs: '"
                        + expectedMessage
                        + "'");
    }

    @Then("the {string} lambda logs should not contain {string}")
    public void lambdaLogsShouldNotContain(String logGroupLogicalId, String unexpectedMessage) {
        if (stackName == null) {
            LOGGER.info("Skipping CloudWatch log assertion — AWS_STACK_NAME not set");
            return;
        }

        String sessionId = DrivingLicenceAPIPage.getSessionId();
        List<String> logMessages;

        if (sessionId != null) {
            logMessages =
                    cloudWatchLogService.getLogMessages(
                            stackName,
                            logGroupLogicalId,
                            sessionId,
                            DrivingLicenceAPIPage.getTestStartTime());
        } else {
            String txn = DLCommonPageObject.getVcTxn();
            LOGGER.info("No sessionId available, falling back to txn-based lookup: '{}'", txn);
            logMessages =
                    cloudWatchLogService.getLogMessagesByTxn(
                            stackName,
                            logGroupLogicalId,
                            txn,
                            DLCommonPageObject.getFeTestStartTime());
        }

        logMessages.forEach(message -> LOGGER.info("  {}", message));

        boolean found =
                logMessages.stream().anyMatch(message -> message.contains(unexpectedMessage));

        LOGGER.info(
                "Negative log assertion for message '{}': {}",
                unexpectedMessage,
                found ? "UNEXPECTEDLY FOUND" : "CORRECTLY ABSENT");

        assertTrue(
                !found,
                "Unexpected log message found in '"
                        + logGroupLogicalId
                        + "' logs: '"
                        + unexpectedMessage
                        + "'");
    }
}
