package uk.gov.di.ipv.cri.drivingpermit.library.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"checkMethod", "identityCheckPolicy", "activityFrom"})
public class CheckDetails {

    @JsonProperty("checkMethod")
    private String checkMethod;

    @JsonProperty("identityCheckPolicy")
    private String identityCheckPolicy;

    @JsonProperty("activityFrom")
    private String activityFrom;

    public String getCheckMethod() {
        return checkMethod;
    }

    public void setCheckMethod(String checkMethod) {
        this.checkMethod = checkMethod;
    }

    public String getIdentityCheckPolicy() {
        return identityCheckPolicy;
    }

    public void setIdentityCheckPolicy(String identityCheckPolicy) {
        this.identityCheckPolicy = identityCheckPolicy;
    }

    public String getActivityFrom() {
        return activityFrom;
    }

    public void setActivityFrom(String activityFrom) {
        this.activityFrom = activityFrom;
    }
}
