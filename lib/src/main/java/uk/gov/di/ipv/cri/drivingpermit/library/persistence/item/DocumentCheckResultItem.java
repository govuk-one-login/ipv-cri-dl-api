package uk.gov.di.ipv.cri.drivingpermit.library.persistence.item;

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
    private int activityHistoryScore;
    private String activityFrom;
    private String checkMethod;
    private String identityCheckPolicy;
    private String issuedBy;
    private String documentNumber;
    private String expiryDate;
    private String transactionId;

    public DocumentCheckResultItem() {}

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

    public void setActivityHistoryScore(int activityHistoryScore) {
        this.activityHistoryScore = activityHistoryScore;
    }

    public int getActivityHistoryScore() {
        return activityHistoryScore;
    }

    public void setActivityFrom(String activityFrom) {
        this.activityFrom = activityFrom;
    }

    public String getActivityFrom() {
        return activityFrom;
    }

    public void setCheckMethod(String checkMethod) {
        this.checkMethod = checkMethod;
    }

    public String getCheckMethod() {
        return checkMethod;
    }

    public void setIdentityCheckPolicy(String identityCheckPolicy) {
        this.identityCheckPolicy = identityCheckPolicy;
    }

    public String getIdentityCheckPolicy() {
        return identityCheckPolicy;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
