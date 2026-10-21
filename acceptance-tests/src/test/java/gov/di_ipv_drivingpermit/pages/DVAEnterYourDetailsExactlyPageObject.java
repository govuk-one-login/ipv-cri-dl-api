package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.TestDataCreator;
import gov.di_ipv_drivingpermit.utilities.TestInput;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DVAEnterYourDetailsExactlyPageObject extends DrivingLicencePageObject {

    @FindBy(id = "dvaDateOfBirth-day")
    public WebElement birthDay;

    @FindBy(id = "dvaDateOfBirth-month")
    public WebElement birthMonth;

    @FindBy(id = "dvaDateOfBirth-year")
    public WebElement birthYear;

    @FindBy(id = "dateOfIssue-day")
    public WebElement dateOfIssueDay;

    @FindBy(id = "dateOfIssue-month")
    public WebElement dateOfIssueMonth;

    @FindBy(id = "dateOfIssue-year")
    public WebElement dateOfIssueYear;

    @FindBy(id = "dvaLicenceNumber")
    public WebElement dvaLicenceNumber;

    @FindBy(id = "consentDVACheckbox")
    public WebElement consentDVACheckbox;

    // Error summary items

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/div/ul/li/a")
    public WebElement dvaConsentErrorInSummary;

    // --------------------------

    // Field errors

    @FindBy(xpath = "//*[@id=\"consentDVACheckbox-error\"]")
    public WebElement dvaConsentCheckboxError;

    // ------------------------

    public DVAEnterYourDetailsExactlyPageObject() {
        PageFactory.initElements(Driver.get(), this);
    }

    @Override
    public void userEntersData(String issuer, String drivingLicenceSubjectScenario) {
        super.userEntersData(issuer, drivingLicenceSubjectScenario);
        TestInput drivingLicenceSubject =
                TestDataCreator.getTestUserFromMap(issuer, drivingLicenceSubjectScenario);
        DVAEnterYourDetailsExactlyPageObject dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPageObject();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.sendKeys(
                drivingLicenceSubject.getLicenceNumber());
        dvaEnterYourDetailsExactlyPage.birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
        dvaEnterYourDetailsExactlyPage.birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
        dvaEnterYourDetailsExactlyPage.birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
        dvaEnterYourDetailsExactlyPage.dateOfIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
        dvaEnterYourDetailsExactlyPage.dateOfIssueMonth.sendKeys(
                drivingLicenceSubject.getIssueMonth());
        dvaEnterYourDetailsExactlyPage.dateOfIssueYear.sendKeys(
                drivingLicenceSubject.getIssueYear());
        dvaEnterYourDetailsExactlyPage.consentDVACheckbox.click();
    }

    public void userEntersInvalidDVADrivingDetails() {
        dvaLicenceNumber.sendKeys("11110610");
        lastName.sendKeys("Testlastname");
        firstName.sendKeys("Testfirstname");
        birthDay.sendKeys("11");
        birthMonth.sendKeys("10");
        birthYear.sendKeys("1962");
        licenceValidToDay.sendKeys("01");
        licenceValidToMonth.sendKeys("01");
        licenceValidToYear.sendKeys("2030");
        dateOfIssueDay.sendKeys("10");
        dateOfIssueMonth.sendKeys("12");
        dateOfIssueYear.sendKeys("2018");
        postcode.sendKeys("BS98 1AA");
        consentDVACheckbox.click();

        BrowserUtils.waitForPageToLoad(10);
    }

    public void userReEntersDataAsDVADrivingLicenceSubject(String drivingLicenceSubjectScenario) {
        TestInput dvaDrivingLicenceSubject =
                TestDataCreator.getTestUserFromMap("DVA", drivingLicenceSubjectScenario);

        dvaLicenceNumber.clear();
        lastName.clear();
        firstName.clear();
        birthDay.clear();
        birthMonth.clear();
        birthYear.clear();
        licenceValidToDay.clear();
        licenceValidToMonth.clear();
        licenceValidToYear.clear();
        dateOfIssueDay.clear();
        dateOfIssueMonth.clear();
        dateOfIssueYear.clear();
        postcode.clear();
        consentDVACheckbox.click();
        dvaLicenceNumber.sendKeys(dvaDrivingLicenceSubject.getLicenceNumber());
        lastName.sendKeys(dvaDrivingLicenceSubject.getLastName());
        firstName.sendKeys(dvaDrivingLicenceSubject.getFirstName());
        birthDay.sendKeys(dvaDrivingLicenceSubject.getBirthDay());
        birthMonth.sendKeys(dvaDrivingLicenceSubject.getBirthMonth());
        birthYear.sendKeys(dvaDrivingLicenceSubject.getBirthYear());
        licenceValidToDay.sendKeys(dvaDrivingLicenceSubject.getValidToDay());
        licenceValidToMonth.sendKeys(dvaDrivingLicenceSubject.getValidToMonth());
        licenceValidToYear.sendKeys(dvaDrivingLicenceSubject.getValidToYear());
        dateOfIssueDay.sendKeys(dvaDrivingLicenceSubject.getIssueDay());
        dateOfIssueMonth.sendKeys(dvaDrivingLicenceSubject.getIssueMonth());
        dateOfIssueYear.sendKeys(dvaDrivingLicenceSubject.getIssueYear());
        postcode.sendKeys(dvaDrivingLicenceSubject.getPostcode());
        consentDVACheckbox.click();
    }

    public void assertDVAConsentErrorInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, dvaConsentErrorInSummary.getText());
    }

    public void assertDVAConsentErrorOnCheckbox(String expectedText) {
        Assert.assertEquals(
                expectedText, dvaConsentCheckboxError.getText().trim().replace("\n", ""));
    }
}
