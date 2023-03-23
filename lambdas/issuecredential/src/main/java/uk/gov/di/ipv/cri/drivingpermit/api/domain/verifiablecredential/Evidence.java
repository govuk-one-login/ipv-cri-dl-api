package uk.gov.di.ipv.cri.drivingpermit.api.domain.verifiablecredential;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;

import java.util.List;

@JsonPropertyOrder({
    "type",
    "txn",
    "activityHistoryScore",
    "strengthScore",
    "validityScore",
    "ci",
    "checkDetails",
    "failedCheckDetails"
})
public class Evidence {
    @JsonProperty("type")
    private EvidenceType type;

    @JsonProperty("txn")
    private String txn;

    @JsonProperty("strengthScore")
    private Integer strengthScore;

    @JsonProperty("validityScore")
    private Integer validityScore;

    @JsonProperty("activityHistoryScore")
    private Integer activityHistoryScore;

    @JsonProperty("checkDetails")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CheckDetails> checkDetails;

    @JsonProperty("failedCheckDetails")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CheckDetails> failedCheckDetails;

    @JsonProperty("ci")
    private List<String> ci;

    public EvidenceType getType() {
        return type;
    }

    public void setType(EvidenceType type) {
        this.type = type;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public Integer getStrengthScore() {
        return strengthScore;
    }

    public void setStrengthScore(Integer strengthScore) {
        this.strengthScore = strengthScore;
    }

    public Integer getValidityScore() {
        return validityScore;
    }

    public void setValidityScore(Integer validityScore) {
        this.validityScore = validityScore;
    }

    public Integer getActivityHistoryScore() {
        return activityHistoryScore;
    }

    public void setActivityHistoryScore(Integer activityHistoryScore) {
        this.activityHistoryScore = activityHistoryScore;
    }

    public List<CheckDetails> getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(List<CheckDetails> checkDetails) {
        this.checkDetails = checkDetails;
    }

    public List<CheckDetails> getFailedCheckDetails() {
        return failedCheckDetails;
    }

    public void setFailedCheckDetails(List<CheckDetails> failedCheckDetails) {
        this.failedCheckDetails = failedCheckDetails;
    }

    public List<String> getCi() {
        return ci;
    }

    public void setCi(List<String> ci) {
        this.ci = ci;
    }
}
