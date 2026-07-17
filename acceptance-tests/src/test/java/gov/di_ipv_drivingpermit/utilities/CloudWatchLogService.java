package gov.di_ipv_drivingpermit.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

public class CloudWatchLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchLogService.class);
    private static final int INITIAL_DELAY_MS = 10000;
    private static final int RETRY_INTERVAL_MS = 5000;
    private static final int MAX_RETRIES = 6;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CloudWatchLogsClient logsClient;
    private final CloudFormationClient cfnClient;
    private String cachedJourneyId;

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

    /**
     * Two-step log retrieval: 1. Query by sessionId to find the govuk_signin_journey_id 2. Re-query
     * by govuk_signin_journey_id to get all correlated log messages
     *
     * @param stackName the CloudFormation stack name
     * @param logGroupLogicalId the CloudFormation logical ID of the log group resource
     * @param sessionId the session ID to locate the journey ID
     * @param startTime the earliest log event timestamp to include
     */
    public List<String> getLogMessages(
            String stackName, String logGroupLogicalId, String sessionId, Instant startTime) {
        String logGroupName = resolveLogGroupName(stackName, logGroupLogicalId);
        LOGGER.info("Resolved log group: '{}'", logGroupName);

        if (cachedJourneyId == null) {
            cachedJourneyId = resolveJourneyId(logGroupName, sessionId, startTime);
            LOGGER.info("Resolved and cached govuk_signin_journey_id: '{}'", cachedJourneyId);
        } else {
            LOGGER.info("Reusing cached govuk_signin_journey_id: '{}'", cachedJourneyId);
        }

        List<String> messages = queryByJourneyId(logGroupName, cachedJourneyId, startTime);

        if (messages.isEmpty()) {
            LOGGER.info(
                    "Journey ID query on '{}' returned no events, falling back to time-window query",
                    logGroupName);
            messages = timeWindowQuery(logGroupName, startTime);
        }

        LOGGER.info("CloudWatch query on '{}' returned {} events", logGroupName, messages.size());
        return messages;
    }

    /**
     * Two-step log retrieval by txn — used for FE UI journeys where sessionId is unavailable.
     * Resolves the govuk_signin_journey_id from the txn on first call and caches it for subsequent
     * calls within the same scenario (e.g. checking multiple log groups).
     *
     * @param stackName the CloudFormation stack name
     * @param logGroupLogicalId the CloudFormation logical ID of the log group resource
     * @param txn the transaction ID from the VC evidence
     * @param startTime the earliest log event timestamp to include
     */
    public List<String> getLogMessagesByTxn(
            String stackName, String logGroupLogicalId, String txn, Instant startTime) {
        String logGroupName = resolveLogGroupName(stackName, logGroupLogicalId);
        LOGGER.info("Resolved log group: '{}'", logGroupName);

        if (cachedJourneyId == null) {
            cachedJourneyId = resolveJourneyId(logGroupName, txn, startTime);
            LOGGER.info("Resolved and cached govuk_signin_journey_id: '{}'", cachedJourneyId);
        } else {
            LOGGER.info("Reusing cached govuk_signin_journey_id: '{}'", cachedJourneyId);
        }

        List<String> messages = queryByJourneyId(logGroupName, cachedJourneyId, startTime);

        LOGGER.info(
                "CloudWatch query on '{}' with journey ID '{}' returned {} events",
                logGroupName,
                cachedJourneyId,
                messages.size());
        return messages;
    }

    /**
     * Single-step log retrieval by time window — used for FE UI journeys where no correlator
     * (sessionId or txn) is available. Queries all log events in the window and returns them.
     * Relies on the test running sequentially with a tight time window to avoid false matches.
     *
     * @param stackName the CloudFormation stack name
     * @param logGroupLogicalId the CloudFormation logical ID of the log group resource
     * @param startTime the earliest log event timestamp to include
     */
    public List<String> getLogMessagesInWindow(
            String stackName, String logGroupLogicalId, Instant startTime) {
        if (startTime == null) {
            LOGGER.warn(
                    "Skipping time-window query for '{}' — startTime is null", logGroupLogicalId);
            return List.of();
        }
        String logGroupName = resolveLogGroupName(stackName, logGroupLogicalId);
        LOGGER.info("Resolved log group: '{}'", logGroupName);

        sleep(INITIAL_DELAY_MS);
        List<String> messages = timeWindowQuery(logGroupName, startTime);

        LOGGER.info(
                "CloudWatch time-window query on '{}' returned {} events",
                logGroupName,
                messages.size());
        return messages;
    }

    /**
     * Queries by sessionId and extracts the govuk_signin_journey_id, retrying to allow for
     * CloudWatch log ingestion delay.
     */
    private String resolveJourneyId(String logGroupName, String sessionId, Instant startTime) {
        sleep(INITIAL_DELAY_MS);
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            List<String> sessionLogs = filterEvents(logGroupName, sessionId, startTime);
            LOGGER.info(
                    "Attempt {}/{}: found {} log events matching sessionId '{}'",
                    attempt,
                    MAX_RETRIES,
                    sessionLogs.size(),
                    sessionId);
            Optional<String> journeyId =
                    sessionLogs.stream()
                            .map(this::parseJourneyId)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst();
            if (journeyId.isPresent()) {
                return journeyId.get();
            }
            if (attempt < MAX_RETRIES) {
                sleep(RETRY_INTERVAL_MS);
            }
        }
        throw new IllegalStateException(
                "Could not find govuk_signin_journey_id in logs for sessionId: " + sessionId);
    }

    private List<String> queryByJourneyId(
            String logGroupName, String journeyId, Instant startTime) {
        return filterEvents(logGroupName, journeyId, startTime);
    }

    private List<String> timeWindowQuery(String logGroupName, Instant startTime) {
        return logsClient
                .filterLogEvents(
                        FilterLogEventsRequest.builder()
                                .logGroupName(logGroupName)
                                .startTime(startTime.toEpochMilli())
                                .build())
                .events()
                .stream()
                .map(FilteredLogEvent::message)
                .toList();
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

    private Optional<String> parseJourneyId(String logMessage) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(logMessage);
            JsonNode journeyIdNode = node.get("govuk_signin_journey_id");
            if (journeyIdNode != null && !journeyIdNode.isNull()) {
                return Optional.of(journeyIdNode.asText());
            }
        } catch (Exception _) {
            // not a JSON log line, skip
        }
        return Optional.empty();
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

    @SuppressWarnings("java:S2925")
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
