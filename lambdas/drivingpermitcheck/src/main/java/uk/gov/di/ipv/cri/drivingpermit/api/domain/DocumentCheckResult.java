package uk.gov.di.ipv.cri.drivingpermit.api.domain;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;

public class DocumentCheckResult {
    private APIResultSource apiResultSource;

    private boolean executedSuccessfully;
    private String result;
    private String errorMessage;
    private String transactionId;

    private CheckDetails checkDetails;

    private boolean isValid;
    private int attemptCount;

    public DocumentCheckResult() {}

    public boolean isExecutedSuccessfully() {
        return executedSuccessfully;
    }

    public void setExecutedSuccessfully(boolean executedSuccessfully) {
        this.executedSuccessfully = executedSuccessfully;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public CheckDetails getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(CheckDetails checkDetails) {
        this.checkDetails = checkDetails;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public void setApiResultSource(APIResultSource apiResultSource) {
        this.apiResultSource = apiResultSource;
    }

    public APIResultSource getApiResultSource() {
        return apiResultSource;
    }
}
