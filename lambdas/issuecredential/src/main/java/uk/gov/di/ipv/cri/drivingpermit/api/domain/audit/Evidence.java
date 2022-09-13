package uk.gov.di.ipv.cri.drivingpermit.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"type", "txn", "strengthScore", "validityScore", "ci"})
public class Evidence {
    @JsonProperty("type")
    private String type;

    @JsonProperty("txn")
    private String txn;

    @JsonProperty("strengthScore")
    private Integer strengthScore;

    @JsonProperty("validityScore")
    private Integer validityScore;

    @JsonProperty("ci")
    private List<String> ci;

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public List<String> getCi() {
        return ci;
    }

    public void setCi(List<String> ci) {
        this.ci = ci;
    }
}
