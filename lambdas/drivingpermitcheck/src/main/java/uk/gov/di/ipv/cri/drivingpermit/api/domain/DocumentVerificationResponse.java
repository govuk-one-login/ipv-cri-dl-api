package uk.gov.di.ipv.cri.drivingpermit.api.domain;

public class DocumentVerificationResponse {

    private String redirectUrl;
    private boolean retry;

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
