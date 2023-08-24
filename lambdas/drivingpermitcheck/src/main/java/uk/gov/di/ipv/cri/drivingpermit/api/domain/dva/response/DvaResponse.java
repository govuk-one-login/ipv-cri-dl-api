package uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.util.List;

@ExcludeFromGeneratedCoverageReport
public class DvaResponse {
    private List<String> errorMessage;
    private String requestHash;
    private boolean validDocument;
    private String issuerID;

    public DvaResponse() {}

    @JsonCreator
    public DvaResponse(
            @JsonProperty(value = "requestHash") String requestHash,
            @JsonProperty(value = "validDocument") boolean validDocument,
            @JsonProperty(value = "issuerID") String issuerID,
            @JsonProperty(value = "errorMessage") List<String> errorMessage) {
        this.requestHash = requestHash;
        this.validDocument = validDocument;
        this.issuerID = issuerID;
        this.errorMessage = errorMessage;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public boolean isValidDocument() {
        return validDocument;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(List<String> errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public void setValidDocument(boolean validDocument) {
        this.validDocument = validDocument;
    }

    public void setIssuerID(String issuerID) {
        this.issuerID = issuerID;
    }
}
