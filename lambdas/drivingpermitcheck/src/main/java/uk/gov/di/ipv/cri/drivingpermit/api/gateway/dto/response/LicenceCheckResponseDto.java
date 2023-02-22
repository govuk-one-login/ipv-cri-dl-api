package uk.gov.di.ipv.cri.drivingpermit.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LicenceCheckResponseDto {
    private String requestHash;
    private boolean validDocument;
    private String issuerID;

    @JsonCreator
    public LicenceCheckResponseDto(
            @JsonProperty("requestHash") String requestHash,
            @JsonProperty("validDocument") boolean validDocument,
            @JsonProperty("issuerID") String issuerID) {
        this.requestHash = requestHash;
        this.validDocument = validDocument;
        this.issuerID = issuerID;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public boolean isValidDocument() {
        return validDocument;
    }

    public void setValidDocument(boolean validDocument) {
        this.validDocument = validDocument;
    }

    public String getIssuerID() {
        return issuerID;
    }

    public void setIssuerID(String issuerID) {
        this.issuerID = issuerID;
    }
}
