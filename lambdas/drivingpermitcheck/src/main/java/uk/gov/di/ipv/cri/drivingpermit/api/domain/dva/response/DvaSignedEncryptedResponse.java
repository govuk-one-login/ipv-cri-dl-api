package uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response;

public class DvaSignedEncryptedResponse {
    private final String payload;

    public DvaSignedEncryptedResponse(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
