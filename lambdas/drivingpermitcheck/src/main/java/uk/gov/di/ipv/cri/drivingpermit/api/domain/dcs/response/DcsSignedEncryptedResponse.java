package uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response;

public class DcsSignedEncryptedResponse {
    private final String payload;

    public DcsSignedEncryptedResponse(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
