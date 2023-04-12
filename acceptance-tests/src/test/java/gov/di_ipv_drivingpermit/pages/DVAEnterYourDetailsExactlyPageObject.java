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

    // --- Hints ---

    @FindBy(id = "dvaDateOfBirth-hint")
    public WebElement dateOfBirthHintDVA;

    @FindBy(id = "dateOfIssue-hint")
    public WebElement dateOfIssueHint;

    @FindBy(id = "dvaLicenceNumber-hint")
    public WebElement dvaLicenceNumberHint;

    // --- Legend text ---

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-fieldset\"]/legend")
    public WebElement dateOfBirthLegendDVA;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-fieldset\"]/legend")
    public WebElement dateOfIssueLegend;

    // --- Labels ---

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[1]/div/label")
    public WebElement dayFieldLabel;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[2]/div/label")
    public WebElement monthFieldLabel;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[3]/div/label")
    public WebElement yearFieldLabel;

    @FindBy(id = "dvaLicenceNumber-label")
    public WebElement licenceNumberLabelDVA;

    // Error summary items

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dvaDateOfBirth-day')]")
    public WebElement DVAInvalidDOBErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dateOfIssue-day')]")
    public WebElement DVAInvalidIssueDateErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dvaLicenceNumber')]")
    public WebElement DVAInvalidDrivingLicenceErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#consentDVACheckbox')]")
    public WebElement DVAConsentErrorInSummary;

    // --------------------------

    // Field errors

    @FindBy(id = "dvaDateOfBirth-error")
    public WebElement DVAInvalidDOBFieldError;

    @FindBy(id = "dateOfIssue-error")
    public WebElement DVAInvalidIssueDateFieldError;

    @FindBy(id = "dvaLicenceNumber-error")
    public WebElement DVADrivingLicenceFieldError;

    @FindBy(id = "dvaDateOfBirth-error")
    public WebElement dateOfBirthErrorDVA;

    @FindBy(id = "dvaLicenceNumber-error")
    public WebElement drivingLicenceNumberErrorDVA;

    @FindBy(id = "consentDVACheckbox-error")
    public WebElement DVAConsentCheckboxError;

    // ------------------------

    public DVAEnterYourDetailsExactlyPageObject() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void userEntersData(String issuer, String drivingLicenceSubjectScenario) {
        super.userEntersData(issuer, drivingLicenceSubjectScenario);
        TestInput drivingLicenceSubject =
                TestDataCreator.getDVATestUserFromMap(issuer, drivingLicenceSubjectScenario);
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

    // Why is this invalid?
    public void userEntersInvalidDVADrivingDetails() {
        dvaLicenceNumber.sendKeys("11110610");
        LastName.sendKeys("Testlastname");
        FirstName.sendKeys("Testfirstname");
        birthDay.sendKeys("11");
        birthMonth.sendKeys("10");
        birthYear.sendKeys("1962");
        LicenceValidToDay.sendKeys("01");
        LicenceValidToMonth.sendKeys("01");
        LicenceValidToYear.sendKeys("2030");
        dateOfIssueDay.sendKeys("10");
        dateOfIssueMonth.sendKeys("12");
        dateOfIssueYear.sendKeys("1970");
        Postcode.sendKeys("BS98 1AA");
        consentDVACheckbox.click();

        BrowserUtils.waitForPageToLoad(10);
    }

    public void userReEntersDataAsDVADrivingLicenceSubject(String drivingLicenceSubjectScenario) {
        TestInput dvaDrivingLicenceSubject =
                TestDataCreator.getDVATestUserFromMap("DVA", drivingLicenceSubjectScenario);

        dvaLicenceNumber.clear();
        LastName.clear();
        FirstName.clear();
        birthDay.clear();
        birthMonth.clear();
        birthYear.clear();
        LicenceValidToDay.clear();
        LicenceValidToMonth.clear();
        LicenceValidToYear.clear();
        dateOfIssueDay.clear();
        dateOfIssueMonth.clear();
        dateOfIssueYear.clear();
        Postcode.clear();
        consentDVACheckbox.click();
        dvaLicenceNumber.sendKeys(dvaDrivingLicenceSubject.getLicenceNumber());
        LastName.sendKeys(dvaDrivingLicenceSubject.getLastName());
        FirstName.sendKeys(dvaDrivingLicenceSubject.getFirstName());
        birthDay.sendKeys(dvaDrivingLicenceSubject.getBirthDay());
        birthMonth.sendKeys(dvaDrivingLicenceSubject.getBirthMonth());
        birthYear.sendKeys(dvaDrivingLicenceSubject.getBirthYear());
        LicenceValidToDay.sendKeys(dvaDrivingLicenceSubject.getValidToDay());
        LicenceValidToMonth.sendKeys(dvaDrivingLicenceSubject.getValidToMonth());
        LicenceValidToYear.sendKeys(dvaDrivingLicenceSubject.getValidToYear());
        dateOfIssueDay.sendKeys(dvaDrivingLicenceSubject.getIssueDay());
        dateOfIssueMonth.sendKeys(dvaDrivingLicenceSubject.getIssueMonth());
        dateOfIssueYear.sendKeys(dvaDrivingLicenceSubject.getIssueYear());
        Postcode.sendKeys(dvaDrivingLicenceSubject.getPostcode());
        consentDVACheckbox.click();
    }

    public void pageTitleDVAValidation() {
        assert (Driver.get().getTitle().contains("We’ll check your details with DVA "));
    }

    public void assertInvalidDateOfBirthErrorText(String expectedText) {
        Assert.assertEquals(expectedText, mapErrorTextToSingleLine(DVAInvalidDOBFieldError));
    }

    public void assertInvalidIssueDateErrorText(String expectedText) {
        Assert.assertEquals(expectedText, DVAInvalidIssueDateErrorInSummary.getText());
    }

    public void assertInvalidIssueDateErrorFieldText(String expectedText) {
        Assert.assertEquals(expectedText, mapErrorTextToSingleLine(DVAInvalidIssueDateFieldError));
    }

    public void assertInvalidDrivingLicenceErrorSummaryText(String expectedText) {
        Assert.assertEquals(expectedText, DVAInvalidDrivingLicenceErrorInSummary.getText());
    }

    public void assertInvalidDrivingLicenceFieldText(String expectedText) {
        Assert.assertEquals(expectedText, mapErrorTextToSingleLine(DVADrivingLicenceFieldError));
    }

    public void assertDateOfBirthLegendTextDVLA(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthLegendDVA.getText());
    }

    public void assertDateOfBirthHintTextDVLA(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthHintDVA.getText());
    }

    public void assertBirthDayLabelTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, dayFieldLabel.getText());
    }

    public void assertBirthYearLabelTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, yearFieldLabel.getText());
    }

    public void assertDateOfIssueLegendText(String expectedText) {
        Assert.assertEquals(expectedText, dateOfIssueLegend.getText());
    }

    public void assertBirthMonthLabelTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, monthFieldLabel.getText());
    }

    public void assertDateOfIssueHintText(String expectedText) {
        Assert.assertEquals(expectedText, dateOfIssueHint.getText());
    }

    public void assertLicenceNumberLabelTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, licenceNumberLabelDVA.getText());
    }

    public void assertDOBErrorText(String expectedText) {
        Assert.assertEquals(expectedText, mapErrorTextToSingleLine(dateOfBirthErrorDVA));
    }

    public void assertLicenceNumberErrorText(String expectedText) {
        Assert.assertEquals(expectedText, mapErrorTextToSingleLine(drivingLicenceNumberErrorDVA));
    }

    public void assertLicenceNumberHintforDVA(String expectedText) {
        Assert.assertEquals(expectedText, dvaLicenceNumberHint.getText());
    }

    public void enterInValidPostCode() {
        enterPostcode("@@@$$$**");
    }

    public void enterDvaLicenceNumber(String licenceNumber) {
        DVAEnterYourDetailsExactlyPageObject dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPageObject();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.clear();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.click();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.sendKeys(licenceNumber);
    }

    public void enterDVADateOfBirth(String day, String month, String year) {
        DVAEnterYourDetailsExactlyPageObject dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPageObject();
        dvaEnterYourDetailsExactlyPage.birthDay.clear();
        dvaEnterYourDetailsExactlyPage.birthDay.click();
        dvaEnterYourDetailsExactlyPage.birthDay.sendKeys(day);
        dvaEnterYourDetailsExactlyPage.birthMonth.clear();
        dvaEnterYourDetailsExactlyPage.birthMonth.click();
        dvaEnterYourDetailsExactlyPage.birthMonth.sendKeys(month);
        dvaEnterYourDetailsExactlyPage.birthYear.clear();
        dvaEnterYourDetailsExactlyPage.birthYear.click();
        dvaEnterYourDetailsExactlyPage.birthYear.sendKeys(year);
    }

    public void enterDateOfBirthInThePast() {
        enterDVADateOfBirth("11", "10", "2062");
    }

    public void enterInvalidIssueDayForDVA(String day, String month, String year) {
        DVAEnterYourDetailsExactlyPageObject dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPageObject();
        dvaEnterYourDetailsExactlyPage.dateOfIssueDay.clear();
        dvaEnterYourDetailsExactlyPage.dateOfIssueDay.click();
        dvaEnterYourDetailsExactlyPage.dateOfIssueDay.sendKeys(day);
        dvaEnterYourDetailsExactlyPage.dateOfIssueMonth.clear();
        dvaEnterYourDetailsExactlyPage.dateOfIssueMonth.click();
        dvaEnterYourDetailsExactlyPage.dateOfIssueMonth.sendKeys(month);
        dvaEnterYourDetailsExactlyPage.dateOfIssueYear.clear();
        dvaEnterYourDetailsExactlyPage.dateOfIssueYear.click();
        dvaEnterYourDetailsExactlyPage.dateOfIssueYear.sendKeys(year);
    }

    public void enterDVAValidToDate(String day, String month, String year) {
        DVAEnterYourDetailsExactlyPageObject dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPageObject();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.sendKeys(day);
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.sendKeys(month);
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.sendKeys(year);
    }

    private String mapErrorTextToSingleLine(WebElement webElement) {
        return webElement.getText().trim().replace("\n", "");
    }

    public void assertDVAConsentErrorInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, DVAConsentErrorInSummary.getText());
    }

    public void assertDVAConsentErrorOnCheckbox(String expectedText) {
        Assert.assertEquals(
                expectedText, DVAConsentCheckboxError.getText().trim().replace("\n", ""));
    }
}
