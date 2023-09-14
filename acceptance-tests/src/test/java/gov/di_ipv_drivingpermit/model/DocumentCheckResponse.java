package gov.di_ipv_drivingpermit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentCheckResponse {

    private boolean retry;
    private String redirectUrl;
    private String dlSessionId;
    private String state;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Boolean getRetry() {
        return retry;
    }

    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    public String getDlSessionId() {
        return dlSessionId;
    }

    public String getState() {
        return state;
    }
}
