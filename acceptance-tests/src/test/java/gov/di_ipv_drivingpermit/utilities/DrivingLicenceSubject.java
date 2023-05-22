package gov.di_ipv_drivingpermit.utilities;

public class DrivingLicenceSubject implements TestInput {

    private String licenceNumber,
            lastName,
            firstName,
            middleNames,
            birthDay,
            birthMonth,
            birthYear,
            validToDay,
            validToMonth,
            validToYear,
            issueDay,
            issueMonth,
            issueYear,
            issueNumber,
            postcode;

    DrivingLicenceSubject(
            String licenceNumber,
            String lastName,
            String firstName,
            String middleNames,
            String birthDay,
            String birthMonth,
            String birthYear,
            String validToDay,
            String validToMonth,
            String validToYear,
            String issueDay,
            String issueMonth,
            String issueYear,
            String issueNumber,
            String postcode) {
        this.licenceNumber = licenceNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.birthDay = birthDay;
        this.birthMonth = birthMonth;
        this.birthYear = birthYear;
        this.validToDay = validToDay;
        this.validToMonth = validToMonth;
        this.validToYear = validToYear;
        this.issueDay = issueDay;
        this.issueMonth = issueMonth;
        this.issueYear = issueYear;
        this.issueNumber = issueNumber;
        this.postcode = postcode;
    }

    DrivingLicenceSubject(DrivingLicenceSubject drivingLicenceSubject) {
        this.licenceNumber = drivingLicenceSubject.licenceNumber;
        this.lastName = drivingLicenceSubject.lastName;
        this.firstName = drivingLicenceSubject.firstName;
        this.middleNames = drivingLicenceSubject.middleNames;
        this.birthDay = drivingLicenceSubject.birthDay;
        this.birthMonth = drivingLicenceSubject.birthMonth;
        this.birthYear = drivingLicenceSubject.birthYear;
        this.validToDay = drivingLicenceSubject.validToDay;
        this.validToMonth = drivingLicenceSubject.validToMonth;
        this.validToYear = drivingLicenceSubject.validToYear;
        this.issueDay = drivingLicenceSubject.issueDay;
        this.issueMonth = drivingLicenceSubject.issueMonth;
        this.issueYear = drivingLicenceSubject.issueYear;
        this.issueNumber = drivingLicenceSubject.issueNumber;
        this.postcode = drivingLicenceSubject.postcode;
    }

    @Override
    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

    @Override
    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    @Override
    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    @Override
    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    @Override
    public String getValidToDay() {
        return validToDay;
    }

    public void setValidToDay(String validToDay) {
        this.validToDay = validToDay;
    }

    @Override
    public String getValidToMonth() {
        return validToMonth;
    }

    public void setValidToMonth(String validToMonth) {
        this.validToMonth = validToMonth;
    }

    @Override
    public String getValidToYear() {
        return validToYear;
    }

    public void setValidToYear(String validToYear) {
        this.validToYear = validToYear;
    }

    @Override
    public String getIssueDay() {
        return issueDay;
    }

    public void setIssueDay(String issueDay) {
        this.issueDay = issueDay;
    }

    @Override
    public String getIssueMonth() {
        return issueMonth;
    }

    public void setIssueMonth(String issueMonth) {
        this.issueMonth = issueMonth;
    }

    @Override
    public String getIssueYear() {
        return issueYear;
    }

    public void setIssueYear(String issueYear) {
        this.issueYear = issueYear;
    }

    @Override
    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    @Override
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
