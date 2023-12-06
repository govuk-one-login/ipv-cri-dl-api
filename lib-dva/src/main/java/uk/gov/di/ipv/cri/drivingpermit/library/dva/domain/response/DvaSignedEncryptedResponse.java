package uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response;

public class DvaSignedEncryptedResponse {
    private final String payload;

    public DvaSignedEncryptedResponse(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
