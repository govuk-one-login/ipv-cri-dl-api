package uk.gov.di.ipv.cri.drivingpermit.api.domain;

import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermit;

import java.util.List;

public class DocumentCheckVerificationResult {

    private boolean executedSuccessfully;
    private boolean verified;
    private List<String> validationErrors;
    private String error;
    private List<String> contraIndicators;
    private int strengthScore;
    private int validityScore;
    private int activityHistoryScore;
    private CheckDetails checkDetails;
    private DrivingPermit drivingPermit;

    private String transactionId;
    private int attemptCount;

    public boolean isExecutedSuccessfully() {
        return executedSuccessfully;
    }

    public void setExecutedSuccessfully(boolean executedSuccessfully) {
        this.executedSuccessfully = executedSuccessfully;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public List<String> getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(List<String> contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public void setActivityHistoryScore(int activityHistoryScore) {
        this.activityHistoryScore = activityHistoryScore;
    }

    public int getActivityHistoryScore() {
        return activityHistoryScore;
    }

    public CheckDetails getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(CheckDetails checkDetails) {
        this.checkDetails = checkDetails;
    }

    public DrivingPermit getDrivingPermit() {
        return drivingPermit;
    }

    public void setDrivingPermit(DrivingPermit drivingPermit) {
        this.drivingPermit = drivingPermit;
    }
}
