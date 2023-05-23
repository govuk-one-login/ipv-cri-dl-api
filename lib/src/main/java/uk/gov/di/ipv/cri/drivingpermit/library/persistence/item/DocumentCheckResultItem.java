package uk.gov.di.ipv.cri.drivingpermit.library.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;
import java.util.Objects;
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

    // DVLA only
    private String issueNumber;

    // Used by DVLA and mapped from dateOfIssue for DVA
    private String issueDate;

    private String transactionId;

    // expiry for documentCheckResultItem in DynamoDb
    private long expiry;

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

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentCheckResultItem that = (DocumentCheckResultItem) o;
        return strengthScore == that.strengthScore
                && validityScore == that.validityScore
                && activityHistoryScore == that.activityHistoryScore
                && expiry == that.expiry
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(contraIndicators, that.contraIndicators)
                && Objects.equals(activityFrom, that.activityFrom)
                && Objects.equals(checkMethod, that.checkMethod)
                && Objects.equals(identityCheckPolicy, that.identityCheckPolicy)
                && Objects.equals(issuedBy, that.issuedBy)
                && Objects.equals(documentNumber, that.documentNumber)
                && Objects.equals(expiryDate, that.expiryDate)
                && Objects.equals(issueNumber, that.issueNumber)
                && Objects.equals(issueDate, that.issueDate)
                && Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sessionId,
                contraIndicators,
                strengthScore,
                validityScore,
                activityHistoryScore,
                activityFrom,
                checkMethod,
                identityCheckPolicy,
                issuedBy,
                documentNumber,
                expiryDate,
                issueNumber,
                issueDate,
                transactionId,
                expiry);
    }
}
