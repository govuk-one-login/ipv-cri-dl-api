package uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.DvaInterface;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ExcludeFromGeneratedCoverageReport
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class DvaPayload implements DvaInterface {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final String TIME_ZONE = "UTC";

    @JsonProperty(value = "requestId", required = true)
    private UUID requestId;

    @JsonProperty private String issuerId;
    @JsonProperty private String timestamp;

    @JsonProperty(value = "driverLicenceNumber", required = true)
    private String driverLicenceNumber;

    @JsonProperty(value = "address", required = true)
    private String postcode;

    @JsonProperty(value = "familyName", required = true)
    private String surname;

    @JsonProperty private String issueNumber;

    @JsonProperty(value = "givenNames", required = true)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> forenames;

    @JsonProperty(value = "dateOfBirth", required = true)
    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate dateOfBirth;

    @JsonProperty(value = "validFrom", required = true)
    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate dateOfIssue;

    @JsonProperty(value = "validTo", required = true)
    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate expiryDate;

    public DvaPayload() {}

    @JsonCreator
    public DvaPayload(
            @JsonProperty(value = "requestId", required = true) UUID requestId,
            @JsonProperty(value = "familyName", required = true) String surname,
            @JsonProperty(value = "givenNames", required = true) List<String> forenames,
            @JsonProperty(value = "dateOfBirth", required = true) LocalDate dateOfBirth,
            @JsonProperty(value = "validFrom", required = true) LocalDate dateOfIssue,
            @JsonProperty(value = "validTo", required = true) LocalDate expiryDate,
            @JsonProperty(value = "driverLicenceNumber", required = true)
                    String driverLicenceNumber,
            @JsonProperty(value = "address", required = true) String postcode) {
        this.requestId = requestId;
        this.surname = surname;
        this.forenames = forenames;
        this.dateOfBirth = dateOfBirth;
        this.dateOfIssue = dateOfIssue;
        this.expiryDate = expiryDate;
        this.driverLicenceNumber = driverLicenceNumber;
        this.timestamp = new SimpleDateFormat(TIMESTAMP_DATE_FORMAT).format(new Date());
        this.postcode = postcode;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<String> getForenames() {
        return forenames;
    }

    public void setForenames(List<String> forenames) {
        this.forenames = forenames;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDriverLicenceNumber() {
        return driverLicenceNumber;
    }

    public void setDriverLicenceNumber(String driverLicenceNumber) {
        this.driverLicenceNumber = driverLicenceNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public LocalDate getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(LocalDate dateOfIssue) {
        this.dateOfIssue = dateOfIssue;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    @Override
    public String toString() {
        return "DvaPayload{"
                + "requestId="
                + requestId
                + ", timestamp='"
                + timestamp
                + '\''
                + ", driverLicenceNumber='"
                + driverLicenceNumber
                + '\''
                + ", address='"
                + postcode
                + '\''
                + ", familyName='"
                + surname
                + '\''
                + ", givenNames="
                + forenames
                + ", dateOfBirth="
                + dateOfBirth
                + ", validFrom="
                + dateOfIssue
                + ", validTo="
                + expiryDate
                + '}';
    }
}
