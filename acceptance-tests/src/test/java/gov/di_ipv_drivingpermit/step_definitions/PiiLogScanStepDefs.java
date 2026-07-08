package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.utilities.CloudWatchLogService;
import gov.di_ipv_drivingpermit.utilities.PiiTermsCollector;
import gov.di_ipv_drivingpermit.utilities.TestRunContext;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PiiLogScanStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(PiiLogScanStepDefs.class);

    private static final List<String> LOG_GROUPS_TO_SCAN =
            List.of("DrivingPermitCheckingFunctionLogGroup", "IssueCredentialFunctionLogGroup");

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
        LOGGER.info("PII scan: checking {} terms across {} log groups", piiTerms.size(), LOG_GROUPS_TO_SCAN.size());

        CloudWatchLogService cloudWatchLogService = new CloudWatchLogService();
        List<String> violations = new ArrayList<>();

        for (String logGroup : LOG_GROUPS_TO_SCAN) {
            for (String term : piiTerms) {
                List<String> matches =
                        cloudWatchLogService.scanForTerm(
                                stackName, logGroup, term, TestRunContext.getSuiteStartTime());
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

        assertTrue(
                violations.isEmpty(),
                "PII detected in CloudWatch logs after test run: " + violations);
    }
}
