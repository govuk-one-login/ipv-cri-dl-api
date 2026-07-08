package gov.di_ipv_drivingpermit.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourceRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;

import java.time.Instant;
import java.util.List;

public class CloudWatchLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchLogService.class);
    private final CloudWatchLogsClient logsClient;
    private final CloudFormationClient cfnClient;

    public CloudWatchLogService() {
        DefaultCredentialsProvider credentials = DefaultCredentialsProvider.builder().build();
        this.logsClient =
                CloudWatchLogsClient.builder()
                        .region(Region.EU_WEST_2)
                        .credentialsProvider(credentials)
                        .build();
        this.cfnClient =
                CloudFormationClient.builder()
                        .region(Region.EU_WEST_2)
                        .credentialsProvider(credentials)
                        .build();
    }

    private List<String> filterEvents(
            String logGroupName, String filterPattern, Instant startTime) {
        FilterLogEventsRequest request =
                FilterLogEventsRequest.builder()
                        .logGroupName(logGroupName)
                        .filterPattern("\"" + filterPattern + "\"")
                        .startTime(startTime.toEpochMilli())
                        .build();
        return logsClient.filterLogEventsPaginator(request).events().stream()
                .map(FilteredLogEvent::message)
                .toList();
    }

    private String resolveLogGroupName(String logGroupLogicalId) {
        String functionLogicalId = logGroupLogicalId.replace("LogGroup", "");
        String envVar = "LOG_GROUP_" + functionLogicalId;
        String logGroupName = System.getenv(envVar);
        if (logGroupName != null) {
            LOGGER.info("Resolved log group '{}' from env var '{}'", logGroupName, envVar);
            return logGroupName;
        }
        String stackName = System.getenv("AWS_STACK_NAME");
        if (stackName != null && !stackName.isBlank()) {
            logGroupName =
                    cfnClient
                            .describeStackResource(
                                    DescribeStackResourceRequest.builder()
                                            .stackName(stackName)
                                            .logicalResourceId(logGroupLogicalId)
                                            .build())
                            .stackResourceDetail()
                            .physicalResourceId();
            LOGGER.debug(
                    "Resolved log group '{}' from CloudFormation stack '{}'",
                    logGroupName,
                    stackName);
            return logGroupName;
        }
        throw new IllegalStateException(
                "Env var '"
                        + envVar
                        + "' not set — log group name could not be resolved for '"
                        + logGroupLogicalId
                        + "'");
    }

    /**
     * Scans a log group for any occurrence of a given term within the time window.
     *
     * @param logGroupLogicalId the CloudFormation logical ID of the log group resource
     * @param term the term to search for
     * @param startTime the earliest log event timestamp to include
     * @return matching log events
     */
    public List<String> scanForTerm(String logGroupLogicalId, String term, Instant startTime) {
        String logGroupName = resolveLogGroupName(logGroupLogicalId);
        try {
            List<String> results = filterEvents(logGroupName, term, startTime);
            LOGGER.debug(
                    "Scanned '{}' for '{}' since {} — {} match(es)",
                    logGroupName,
                    term,
                    startTime,
                    results.size());
            return results;
        } catch (CloudWatchLogsException e) {
            LOGGER.warn(
                    "CloudWatch query failed for log group '{}', term '{}' — skipping: {} {}",
                    logGroupName,
                    term,
                    e.awsErrorDetails().errorCode(),
                    e.getMessage());
            return List.of();
        }
    }
}
