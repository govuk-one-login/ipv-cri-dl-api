package uk.gov.di.ipv.cri.drivingpermit.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LicenceCheckRequestDto {

    private String issuerID;
    private String requestID;
    private String familyName;
    private List<String> givenNames;
    private String dateOfBirth;
    private String validFrom;
    private String validTo;
    private String driverLicenceNumber;
    private String issueNumber;
    private String address;

    public LicenceCheckRequestDto(
            @JsonProperty("issuerID") String issuerID,
            @JsonProperty("requestID") String requestID,
            @JsonProperty("familyName") String familyName,
            @JsonProperty("givenNames") List<String> givenNames,
            @JsonProperty("dateOfBirth") String dateOfBirth,
            @JsonProperty("validFrom") String validFrom,
            @JsonProperty("validTo") String validTo,
            @JsonProperty("driverLicenceNumber") String driverLicenceNumber,
            @JsonProperty("address") String address) {

        this.issuerID = issuerID;
        this.requestID = requestID;
        this.familyName = familyName;
        this.givenNames = givenNames;
        this.dateOfBirth = dateOfBirth;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.driverLicenceNumber = driverLicenceNumber;
        this.address = address;
    }

    public String getIssuerID() {
        return issuerID;
    }

    public void setIssuerID(String issuerID) {
        this.issuerID = issuerID;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public List<String> getGivenNames() {
        return givenNames;
    }

    public void setGivenNames(List<String> givenNames) {
        this.givenNames = givenNames;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public String getDriverLicenceNumber() {
        return driverLicenceNumber;
    }

    public void setDriverLicenceNumber(String driverLicenceNumber) {
        this.driverLicenceNumber = driverLicenceNumber;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
