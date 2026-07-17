package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.utilities.CloudWatchLogService;
import gov.di_ipv_drivingpermit.utilities.PiiTermsCollector;
import gov.di_ipv_drivingpermit.utilities.TestRunContext;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PiiLogScanStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(PiiLogScanStepDefs.class);

    private static final List<String> CFN_LOG_GROUPS =
            List.of(
                    "DrivingPermitCheckingFunctionLogGroup",
                    "IssueCredentialFunctionLogGroup",
                    "PersonInfoFunctionLogGroup");

    @BeforeAll
    public static void recordSuiteStart() {
        TestRunContext.recordSuiteStart();
    }

    @AfterAll
    public static void scanLogsForPii() {
        String stackName = System.getenv("AWS_STACK_NAME");
        if (stackName == null) {
            LOGGER.info("Skipping PII log scan — AWS_STACK_NAME not set");
            return;
        }

        if (TestRunContext.getSuiteStartTime() == null) {
            LOGGER.warn("Skipping PII log scan — suite start time not recorded");
            return;
        }

        Set<String> piiTerms = PiiTermsCollector.collectFromAllTestUsers();
        String ecsLogGroup = System.getenv("ECS_LOG_GROUP_NAME");
        int totalGroups = CFN_LOG_GROUPS.size() + (ecsLogGroup != null ? 1 : 0);
        LOGGER.info(
                "PII scan: checking {} terms across {} log groups", piiTerms.size(), totalGroups);

        CloudWatchLogService cloudWatchLogService = new CloudWatchLogService();
        List<String> violations = new ArrayList<>();

        for (String logGroup : CFN_LOG_GROUPS) {
            for (String term : piiTerms) {
                recordViolations(
                        cloudWatchLogService.scanForTerm(
                                stackName, logGroup, term, TestRunContext.getSuiteStartTime()),
                        logGroup,
                        term,
                        violations);
            }
        }

        if (ecsLogGroup != null) {
            LOGGER.info("Scanning ECS log group '{}' for PII", ecsLogGroup);
            for (String term : piiTerms) {
                recordViolations(
                        cloudWatchLogService.scanForTermByDirectName(
                                ecsLogGroup, term, TestRunContext.getSuiteStartTime()),
                        ecsLogGroup,
                        term,
                        violations);
            }
        } else {
            LOGGER.info("Skipping ECS log scan — ECS_LOG_GROUP_NAME not set");
        }

        assertTrue(
                violations.isEmpty(),
                "PII detected in CloudWatch logs after test run: " + violations);
    }

    private static void recordViolations(
            List<String> matches, String logGroup, String term, List<String> violations) {
        if (!matches.isEmpty()) {
            LOGGER.error(
                    "PII DETECTED in '{}': term '{}' found in {} log event(s)",
                    logGroup,
                    term,
                    matches.size());
            matches.forEach(match -> LOGGER.error("  {}", match));
            violations.add("'" + term + "' found in " + logGroup);
        }
    }
}
