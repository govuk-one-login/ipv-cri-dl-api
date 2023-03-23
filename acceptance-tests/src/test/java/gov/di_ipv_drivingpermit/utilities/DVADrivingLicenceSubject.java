package gov.di_ipv_drivingpermit.utilities;

public class DVADrivingLicenceSubject implements TestInput {

    private String dvaLicenceNumber,
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
            postcode;

    DVADrivingLicenceSubject(
            String dvaLicenceNumber,
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
            String postcode) {
        this.dvaLicenceNumber = dvaLicenceNumber;
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
        this.postcode = postcode;
    }

    DVADrivingLicenceSubject(DVADrivingLicenceSubject dvaDrivingLicenceSubject) {
        this.dvaLicenceNumber = dvaDrivingLicenceSubject.dvaLicenceNumber;
        this.lastName = dvaDrivingLicenceSubject.lastName;
        this.firstName = dvaDrivingLicenceSubject.firstName;
        this.middleNames = dvaDrivingLicenceSubject.middleNames;
        this.birthDay = dvaDrivingLicenceSubject.birthDay;
        this.birthMonth = dvaDrivingLicenceSubject.birthMonth;
        this.birthYear = dvaDrivingLicenceSubject.birthYear;
        this.validToDay = dvaDrivingLicenceSubject.validToDay;
        this.validToMonth = dvaDrivingLicenceSubject.validToMonth;
        this.validToYear = dvaDrivingLicenceSubject.validToYear;
        this.issueDay = dvaDrivingLicenceSubject.issueDay;
        this.issueMonth = dvaDrivingLicenceSubject.issueMonth;
        this.issueYear = dvaDrivingLicenceSubject.issueYear;
        this.postcode = dvaDrivingLicenceSubject.postcode;
    }

    public String getDvaLicenceNumber() {
        return dvaLicenceNumber;
    }

    public void setDvaLicenceNumber(String dvaLicenceNumber) {
        this.dvaLicenceNumber = dvaLicenceNumber;
    }

    @Override
    public String getLicenceNumber() {
        return this.dvaLicenceNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getValidToDay() {
        return validToDay;
    }

    public void setValidToDay(String validToDay) {
        this.validToDay = validToDay;
    }

    public String getValidToMonth() {
        return validToMonth;
    }

    public void setValidToMonth(String validToMonth) {
        this.validToMonth = validToMonth;
    }

    public String getValidToYear() {
        return validToYear;
    }

    public void setValidToYear(String validToYear) {
        this.validToYear = validToYear;
    }

    public String getIssueDay() {
        return issueDay;
    }

    public void setIssueDay(String issueDay) {
        this.issueDay = issueDay;
    }

    public String getIssueMonth() {
        return issueMonth;
    }

    public void setIssueMonth(String issueMonth) {
        this.issueMonth = issueMonth;
    }

    public String getIssueYear() {
        return issueYear;
    }

    @Override
    public String getIssueNumber() {
        return null;
    }

    public void setIssueYear(String issueYear) {
        this.issueYear = issueYear;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
