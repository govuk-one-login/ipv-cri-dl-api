package uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ExcludeFromGeneratedCoverageReport
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class DcsPayload {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final String TIME_ZONE = "UTC";

    @JsonProperty private UUID correlationId;
    @JsonProperty private UUID requestId;
    @JsonProperty private String timestamp;
    @JsonProperty private String licenceNumber;
    @JsonProperty private String driverNumber;
    @JsonProperty private String postcode;
    @JsonProperty private String clientId = "di-ipv-passport-test-2021-12";
    @JsonProperty private String surname;
    @JsonProperty private String issueNumber;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> forenames;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate dateOfBirth;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate issueDate;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate dateOfIssue;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    public LocalDate expiryDate;

    public DcsPayload() {}

    @JsonCreator
    public DcsPayload(
            @JsonProperty(value = "surname", required = true) String surname,
            @JsonProperty(value = "forenames", required = true) List<String> forenames,
            @JsonProperty(value = "dateOfBirth", required = true) LocalDate dateOfBirth,
            @JsonProperty(value = "issueDate", required = false) LocalDate issueDate,
            @JsonProperty(value = "dateOfIssue", required = false) LocalDate dateOfIssue,
            @JsonProperty(value = "expiryDate", required = true) LocalDate expiryDate,
            @JsonProperty(value = "postcode", required = true) String postcode) {
        this.surname = surname;
        this.forenames = forenames;
        this.dateOfBirth = dateOfBirth;
        this.issueDate = issueDate;
        this.dateOfIssue = dateOfIssue;
        this.expiryDate = expiryDate;
        this.correlationId = UUID.randomUUID();
        this.requestId = UUID.randomUUID();
        this.timestamp = new SimpleDateFormat(TIMESTAMP_DATE_FORMAT).format(new Date());
        this.postcode = postcode;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
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

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
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

    public String getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
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

    @Override
    public String toString() {
        return "DcsPayload{"
                + "correlationId="
                + correlationId
                + ", requestId="
                + requestId
                + ", timestamp='"
                + timestamp
                + '\''
                + ", licenceNumber='"
                + licenceNumber
                + '\''
                + ", driverNumber='"
                + driverNumber
                + '\''
                + ", postcode='"
                + postcode
                + '\''
                + ", clientId='"
                + clientId
                + '\''
                + ", surname='"
                + surname
                + '\''
                + ", issueNumber='"
                + issueNumber
                + '\''
                + ", forenames="
                + forenames
                + ", dateOfBirth="
                + dateOfBirth
                + ", issueDate="
                + issueDate
                + ", dateOfIssue="
                + dateOfIssue
                + ", expiryDate="
                + expiryDate
                + '}';
    }
}
