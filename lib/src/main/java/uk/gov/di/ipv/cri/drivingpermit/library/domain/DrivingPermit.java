package uk.gov.di.ipv.cri.drivingpermit.library.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"documentNumber", "expiryDate", "issuedBy"})
public class DrivingPermit {

    @JsonProperty("documentNumber")
    private String documentNumber;

    @JsonProperty("expiryDate")
    private String expiryDate;

    @JsonProperty("issuedBy")
    private String issuedBy;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }
}
