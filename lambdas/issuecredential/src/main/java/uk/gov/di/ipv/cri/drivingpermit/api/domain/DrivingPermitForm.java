package uk.gov.di.ipv.cri.drivingpermit.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;

import java.time.LocalDate;
import java.util.List;

@ExcludeFromGeneratedCoverageReport
public class DrivingPermitForm {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String TIME_ZONE = "UTC";

    @JsonProperty private String drivingLicenceNumber;
    @JsonProperty private String surname;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> forenames;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Address> addresses;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    private LocalDate dateOfBirth;

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIME_ZONE)
    private LocalDate expiryDate;

    public DrivingPermitForm() {}

    @JsonCreator
    public DrivingPermitForm(
            @JsonProperty(value = "drivingLicenceNumber", required = true)
                    String drivingLicenceNumber,
            @JsonProperty(value = "surname", required = true) String surname,
            @JsonProperty(value = "forenames", required = true) List<String> forenames,
            @JsonProperty(value = "dateOfBirth", required = true) LocalDate dateOfBirth,
            @JsonProperty(value = "expiryDate", required = true) LocalDate expiryDate,
            @JsonProperty(value = "addresses", required = true) List<Address> addresses) {
        this.drivingLicenceNumber = drivingLicenceNumber;
        this.surname = surname;
        this.forenames = forenames;
        this.dateOfBirth = dateOfBirth;
        this.expiryDate = expiryDate;
        this.addresses = addresses;
    }

    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }

    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
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

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return "DrivingPermitForm{"
                + ", drivingLicenceNumber='"
                + drivingLicenceNumber
                + ", surname='"
                + surname
                + ", forenames="
                + forenames
                + ", dateOfBirth="
                + dateOfBirth
                + ", expiryDate="
                + expiryDate
                + ", addresses="
                + addresses
                + '}';
    }
}
