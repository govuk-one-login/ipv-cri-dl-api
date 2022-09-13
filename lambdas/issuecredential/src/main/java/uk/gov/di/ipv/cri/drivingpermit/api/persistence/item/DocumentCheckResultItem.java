package uk.gov.di.ipv.cri.drivingpermit.api.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;
import java.util.UUID;

@DynamoDbBean
public class DocumentCheckResultItem {
    private UUID sessionId;
    private List<String> contraIndicators;
    private int strengthScore;
    private int validityScore;
    private String transactionId;

    public DocumentCheckResultItem() {}

    public DocumentCheckResultItem(
            UUID sessionId,
            List<String> contraIndicators,
            Integer strengthScore,
            Integer validityScore) {
        this.sessionId = sessionId;
        this.contraIndicators = contraIndicators;
        this.strengthScore = strengthScore;
        this.validityScore = validityScore;
    }

    @DynamoDbPartitionKey()
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(List<String> contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public int getStrengthScore() {
        return strengthScore;
    }

    public void setStrengthScore(int strengthScore) {
        this.strengthScore = strengthScore;
    }

    public int getValidityScore() {
        return validityScore;
    }

    public void setValidityScore(int validityScore) {
        this.validityScore = validityScore;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
