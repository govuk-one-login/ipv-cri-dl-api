package gov.di_ipv_drivingpermit.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourceRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
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
        return logsClient
                .filterLogEvents(
                        FilterLogEventsRequest.builder()
                                .logGroupName(logGroupName)
                                .filterPattern("\"" + filterPattern + "\"")
                                .startTime(startTime.toEpochMilli())
                                .build())
                .events()
                .stream()
                .map(FilteredLogEvent::message)
                .toList();
    }

    private String resolveLogGroupName(String stackName, String logGroupLogicalId) {
        LOGGER.info(
                "Resolving log group for stack '{}' logical ID '{}'", stackName, logGroupLogicalId);
        return cfnClient
                .describeStackResource(
                        DescribeStackResourceRequest.builder()
                                .stackName(stackName)
                                .logicalResourceId(logGroupLogicalId)
                                .build())
                .stackResourceDetail()
                .physicalResourceId();
    }

    /**
     * Scans a log group for any occurrence of a given term within the time window.
     *
     * @param stackName the CloudFormation stack name
     * @param logGroupLogicalId the CloudFormation logical ID of the log group resource
     * @param term the term to search for
     * @param startTime the earliest log event timestamp to include
     * @return matching log events
     */
    public List<String> scanForTerm(
            String stackName, String logGroupLogicalId, String term, Instant startTime) {
        String logGroupName = resolveLogGroupName(stackName, logGroupLogicalId);
        return filterEvents(logGroupName, term, startTime);
    }

    /**
     * Scans a log group by its literal name (for log groups outside the CRI stack).
     *
     * @param logGroupName the literal CloudWatch log group name
     * @param term the term to search for
     * @param startTime the earliest log event timestamp to include
     * @return matching log events
     */
    public List<String> scanForTermByDirectName(
            String logGroupName, String term, Instant startTime) {
        return filterEvents(logGroupName, term, startTime);
    }
}
