package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.TestDataCreator;
import gov.di_ipv_drivingpermit.utilities.TestInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.checkOkHttpResponseOnLink;
import static java.lang.System.getenv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicencePageObject extends UniversalSteps {

    private final ConfigurationService configurationService;

    private static final Logger LOGGER = LogManager.getLogger();

    // ---------------------

    @FindBy(id = "licenceIssuer-DVLA-label")
    public WebElement optionDVLA;

    @FindBy(id = "licenceIssuer")
    public WebElement radioBtnDVLA;

    @FindBy(id = "licenceIssuer-DVA-label")
    public WebElement optionDVA;

    @FindBy(id = "licenceIssuer-DVA")
    public WebElement radioBtnDVA;

    @FindBy(id = "licenceIssuer-noLicence-label")
    public WebElement noDLOption;

    @FindBy(id = "licenceIssuer-noLicence")
    public WebElement noDLRadioBtn;

    @FindBy(id = "continue")
    public WebElement ctButton;

    @FindBy(id = "licenceIssuer-error")
    public WebElement radioButtonError;

    @FindBy(xpath = "/html/body/div[2]/nav/ul/li[2]/a")
    public WebElement languageToggle;

    @FindBy(xpath = "/html/body/div[2]/nav/ul/li[1]/a")
    public WebElement languageToggleWales;

    // ---------------

    @FindBy(className = "error-summary")
    public WebElement errorSummary;

    @FindBy(xpath = "//*[@class='govuk-notification-banner__content']")
    public WebElement userNotFoundInThirdPartyBanner;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]")
    public WebElement userNotFoundInThirdPartyBannerDva;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div/a")
    public WebElement proveAnotherWay;

    @FindBy(id = "drivingLicenceNumber")
    public WebElement licenceNumber;

    @FindBy(xpath = "//*[@id=\"dvaLicenceNumber\"]")
    public WebElement licenceNumberDva;

    @FindBy(id = "surname")
    public WebElement lastName;

    @FindBy(id = "firstName")
    public WebElement firstName;

    @FindBy(id = "middleNames")
    public WebElement middleNames;

    @FindBy(id = "dateOfBirth-day")
    public WebElement birthDay;

    @FindBy(id = "dateOfBirth-month")
    public WebElement birthMonth;

    @FindBy(id = "dateOfBirth-year")
    public WebElement birthYear;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthDayDva;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthMonthDva;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthYearDva;

    @FindBy(id = "expiryDate-day")
    public WebElement licenceValidToDay;

    @FindBy(id = "expiryDate-month")
    public WebElement licenceValidToMonth;

    @FindBy(id = "expiryDate-year")
    public WebElement licenceValidToYear;

    @FindBy(id = "issueDate-day")
    public WebElement licenceIssueDay;

    @FindBy(id = "issueDate-month")
    public WebElement licenceIssueMonth;

    @FindBy(id = "issueDate-year")
    public WebElement licenceIssueYear;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-day\"]")
    public WebElement licenceIssueDayDva;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-month\"]")
    public WebElement licenceIssueMonthDva;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-year\"]")
    public WebElement licenceIssueYearDva;

    @FindBy(id = "issueNumber")
    public WebElement issueNumber;

    @FindBy(id = "postcode")
    public WebElement postcode;

    @FindBy(id = "consentCheckbox")
    public WebElement consentDVLACheckbox;

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement continuebutton;

    @FindBy(xpath = "//*[@id=\"confirmDetails\"]")
    public WebElement correctDetailsRadioButton;

    @FindBy(xpath = "//*[@id=\"confirmDetails-detailsNotConfirmed\"]")
    public WebElement incorrectDetailsRadioButton;

    @FindBy(id = "header")
    public WebElement pageHeader;

    @FindBy(xpath = "//*[@class='govuk-back-link']")
    public WebElement back;

    @FindBy(className = "govuk-details__summary-text")
    public WebElement whyWeText;

    @FindBy(className = "govuk-details__text")
    public WebElement whyWePara;

    @FindBy(id = "consentDVACheckbox")
    public WebElement consentDVACheckbox;

    // Error summary items

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#licenceIssuer')]")
    public WebElement invalidDocumentIssuerInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'-day')]")
    public WebElement invalidDOBErrorInSummary;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/div/ul/li[2]/a")
    public WebElement invalidDrivingLicenceErrorInSummary;

    @FindBy(xpath = "//*[@id=\"drivingLicenceNumber-error\"]")
    public WebElement invalidDrivingLicenceErrorInField;

    // -------------------------

    // Field errors

    @FindBy(id = "dateOfBirth-error")
    public WebElement invalidDateOfBirthFieldError;

    @FindBy(id = "consentCheckbox-error")
    public WebElement dvlaConsentCheckboxError;

    @FindBy(id = "consentDVACheckbox-error")
    public WebElement dvaConsentCheckboxError;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/h2")
    public WebElement dvlaConsentSection;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[1]/a")
    public WebElement oneLoginLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[2]/a")
    public WebElement dvlaLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[1]")
    public WebElement dvlaSentence;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[2]")
    public WebElement dvlaSentenceTwo;

    // ------------------------

    public DrivingLicencePageObject() {
        this.configurationService = new ConfigurationService(getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
        TestDataCreator.createDefaultResponses();
    }

    public void whyWeNeedToKnowThis() {
        assertTrue(whyWeText.isDisplayed());
        LOGGER.info(whyWeText.getText());
    }

    public void clickOnWhyWeNeedLink() {
        whyWeText.click();
    }

    public void paragraphValidation() {
        assertTrue(whyWePara.isDisplayed());
        LOGGER.info(whyWePara.getText());
    }

    public void titleDVLAWithRadioBtn(String expectedText) {
        optionDVLA.isDisplayed();
        assertEquals(expectedText, optionDVLA.getText());
        radioBtnDVLA.isDisplayed();
    }

    public void titleDVAWithRadioBtn(String expectedText) {
        optionDVA.isDisplayed();
        assertEquals(expectedText, optionDVA.getText());
        radioBtnDVA.isDisplayed();
    }

    public void noDrivingLicenceBtn(String expectedText) {
        noDLOption.isDisplayed();
        assertEquals(expectedText, noDLOption.getText());
        noDLRadioBtn.isDisplayed();
    }

    public void ContinueButton(String expectedText) {
        ctButton.isDisplayed();
        assertEquals(expectedText, ctButton.getText());
        ctButton.isEnabled();
    }

    public void clickOnDVLARadioButton() {
        radioBtnDVLA.click();
        ctButton.click();
    }

    public void clickOnDVARadioButton() {
        radioBtnDVA.click();
        ctButton.click();
    }

    public void clickOnIDoNotHaveAUKDrivingLicenceRadioButton() {
        noDLRadioBtn.click();
        continuebutton.click();
    }

    public void noSelectContinue() {
        ctButton.click();
    }

    public void assertErrorTitle(String expectedText) {
        assertEquals(expectedText, invalidDocumentIssuerInSummary.getText());
    }

    public void errorMessage() {
        errorSummary.isDisplayed();
    }

    public void errorLink() {
        errorSummary.click();
        radioBtnDVLA.isEnabled();
    }

    public void validateErrorText(String expectedText) {
        assertEquals(expectedText, radioButtonError.getText().trim().replace("\n", ""));
    }

    public void drivingLicencePageURLValidationWelsh() {
        assertURLContains("/licence-issuer/?lng=cy");
    }

    // -----------------------

    public void userNotFoundInThirdPartyErrorIsDisplayed() {
        BrowserUtils.waitForVisibility(userNotFoundInThirdPartyBanner, 10);
        assertTrue(userNotFoundInThirdPartyBanner.isDisplayed());
        LOGGER.info(userNotFoundInThirdPartyBanner.getText());
    }

    public void userNotFoundInThirdPartyErrorIsDisplayedDva() {
        BrowserUtils.waitForVisibility(userNotFoundInThirdPartyBannerDva, 10);
        assertTrue(userNotFoundInThirdPartyBannerDva.isDisplayed());
        LOGGER.info(userNotFoundInThirdPartyBannerDva.getText());
    }

    public void userReEntersLastName(String invalidLastName) {
        lastName.clear();
        lastName.sendKeys(invalidLastName);
    }

    public void userReEntersFirstName(String invalidFirstName) {
        firstName.clear();
        firstName.sendKeys(invalidFirstName);
    }

    public void userReEntersBirthDay(String invalidBirthDay) {
        birthDay.clear();
        birthDay.sendKeys(invalidBirthDay);
    }

    public void userReEntersDvaBirthDay(String invalidBirthDay) {
        birthDayDva.clear();
        birthDayDva.sendKeys(invalidBirthDay);
    }

    public void userReEntersDvaBirthMonth(String invalidBirthMonth) {
        birthMonthDva.clear();
        birthMonthDva.sendKeys(invalidBirthMonth);
    }

    public void userReEntersDvaBirthYear(String invalidBirthYear) {
        birthYearDva.clear();
        birthYearDva.sendKeys(invalidBirthYear);
    }

    public void userReEntersBirthMonth(String invalidBirthMonth) {
        birthMonth.clear();
        birthMonth.sendKeys(invalidBirthMonth);
    }

    public void userReEntersBirthYear(String invalidBirthYear) {
        birthYear.clear();
        birthYear.sendKeys(invalidBirthYear);
    }

    public void userReEntersIssueDay(String invalidLicenceIssueDay) {
        licenceIssueDay.clear();
        licenceIssueDay.sendKeys(invalidLicenceIssueDay);
    }

    public void userReEntersDvaIssueDay(String invalidLicenceIssueDay) {
        licenceIssueDayDva.clear();
        licenceIssueDayDva.sendKeys(invalidLicenceIssueDay);
    }

    public void userReEntersDvaIssueMonth(String invalidLicenceIssueMonth) {
        licenceIssueMonthDva.clear();
        licenceIssueMonthDva.sendKeys(invalidLicenceIssueMonth);
    }

    public void userReEntersDvaIssueYear(String invalidLicenceIssueYear) {
        licenceIssueYearDva.clear();
        licenceIssueYearDva.sendKeys(invalidLicenceIssueYear);
    }

    public void userReEntersLicenceNumber(String invalidLicenceNumber) {
        licenceNumber.clear();
        licenceNumber.sendKeys(invalidLicenceNumber);
    }

    public void userReEntersDvaLicenceNumber(String invalidLicenceNumber) {
        licenceNumberDva.clear();
        licenceNumberDva.sendKeys(invalidLicenceNumber);
    }

    public void userReEntersIssueMonth(String invalidLicenceIssueMonth) {
        licenceIssueMonth.clear();
        licenceIssueMonth.sendKeys(invalidLicenceIssueMonth);
    }

    public void userReEntersIssueYear(String invalidLicenceIssueYear) {
        licenceIssueYear.clear();
        licenceIssueYear.sendKeys(invalidLicenceIssueYear);
    }

    public void userReEntersIssueNumber(String invalidIssueNumber) {
        issueNumber.clear();
        issueNumber.sendKeys(invalidIssueNumber);
    }

    public void userReEntersValidToDay(String invalidValidToDate) {
        licenceValidToDay.clear();
        licenceValidToDay.sendKeys(invalidValidToDate);
    }

    public void userReEntersValidToMonth(String invalidValidToMonth) {
        licenceValidToMonth.clear();
        licenceValidToMonth.sendKeys(invalidValidToMonth);
    }

    public void userReEntersValidToYear(String invalidValidToYear) {
        licenceValidToYear.clear();
        licenceValidToYear.sendKeys(invalidValidToYear);
    }

    public void userReEntersPostcode(String invalidPostcode) {
        postcode.clear();
        postcode.sendKeys(invalidPostcode);
    }

    public void userEntersData(String issuer, String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getTestUserFromMap(issuer, drivingLicenceSubjectScenario);
        if (issuer.equals("DVLA")) {
            licenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
            birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
            birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
            birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
            licenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
            licenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
            licenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
            if (null != drivingLicenceSubject.getIssueNumber()) {
                issueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
            }
            consentDVLACheckbox.click();
        }
        if (null != drivingLicenceSubject.getMiddleNames()) {
            middleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }

        lastName.sendKeys(drivingLicenceSubject.getLastName());
        firstName.sendKeys(drivingLicenceSubject.getFirstName());
        licenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        licenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        licenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    public void userEntersInvalidDrivingDetails() {
        DrivingLicencePageObject enterYourDetailsExactlyDVLAPage = new DrivingLicencePageObject();
        enterYourDetailsExactlyDVLAPage.licenceNumber.sendKeys("PARKE610112PBFGI");
        enterYourDetailsExactlyDVLAPage.lastName.sendKeys("Testlastname");
        enterYourDetailsExactlyDVLAPage.firstName.sendKeys("Testfirstname");
        enterYourDetailsExactlyDVLAPage.birthDay.sendKeys("11");
        enterYourDetailsExactlyDVLAPage.birthMonth.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.birthYear.sendKeys("1962");
        enterYourDetailsExactlyDVLAPage.licenceValidToDay.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.licenceValidToMonth.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.licenceValidToYear.sendKeys("2030");
        enterYourDetailsExactlyDVLAPage.licenceIssueDay.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.licenceIssueMonth.sendKeys("12");
        enterYourDetailsExactlyDVLAPage.licenceIssueYear.sendKeys("2018");
        enterYourDetailsExactlyDVLAPage.issueNumber.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.postcode.sendKeys("BS98 1AA");
        consentDVLACheckbox.click();

        BrowserUtils.waitForPageToLoad(10);
    }

    public void userReEntersDataAsADrivingLicenceSubject(String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getTestUserFromMap("DVLA", drivingLicenceSubjectScenario);

        licenceNumber.clear();
        lastName.clear();
        firstName.clear();
        middleNames.clear();
        birthDay.clear();
        birthMonth.clear();
        birthYear.clear();
        licenceValidToDay.clear();
        licenceValidToMonth.clear();
        licenceValidToYear.clear();
        licenceIssueDay.clear();
        licenceIssueMonth.clear();
        licenceIssueYear.clear();
        issueNumber.clear();
        postcode.clear();
        licenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
        lastName.sendKeys(drivingLicenceSubject.getLastName());
        firstName.sendKeys(drivingLicenceSubject.getFirstName());
        if (null != drivingLicenceSubject.getMiddleNames()) {
            middleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }
        if (null != drivingLicenceSubject.getIssueNumber()) {
            issueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
        }
        birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
        birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
        birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
        licenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        licenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        licenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        licenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
        licenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
        licenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
        postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    public void assertInvalidDoBInErrorSummary(String expectedText) {
        BrowserUtils.waitForVisibility(invalidDOBErrorInSummary, 10);
        assertEquals(expectedText, invalidDOBErrorInSummary.getText());
    }

    public void assertInvalidDoBOnField(String expectedText) {
        BrowserUtils.waitForVisibility(invalidDateOfBirthFieldError, 10);
        assertEquals(expectedText, invalidDateOfBirthFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidLicenceNumberInErrorSummary(String expectedText) {
        BrowserUtils.waitForVisibility(invalidDrivingLicenceErrorInSummary, 10);
        assertEquals(expectedText, invalidDrivingLicenceErrorInSummary.getText());
    }

    public void assertInvalidLicenceNumberField(String expectedText) {
        BrowserUtils.waitForVisibility(invalidDrivingLicenceErrorInField, 10);
        assertEquals(
                expectedText, invalidDrivingLicenceErrorInField.getText().trim().replace("\n", ""));
    }

    public void assertNoConsentGivenInErrorSummary(String expectedText) {
        BrowserUtils.waitForVisibility(dvlaConsentCheckboxError, 10);
        String formattedErrorText = dvlaConsentCheckboxError.getText().replaceAll("\\s+", " ");
        assertEquals(expectedText, formattedErrorText);
    }

    public void assertNoConsentGivenInDVAErrorSummary(String expectedText) {
        BrowserUtils.waitForVisibility(dvaConsentCheckboxError, 10);
        String formattedErrorText = dvaConsentCheckboxError.getText().replaceAll("\\s+", " ");
        assertEquals(expectedText, formattedErrorText);
    }

    public void validateErrorPageHeading(String errorHeading) {
        assertEquals(errorHeading, pageHeader.getText());
    }

    public List<JsonNode> getListOfNodes(JsonNode vcNode, String evidence) throws IOException {
        JsonNode evidenceNode = vcNode.get(evidence);

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        return objectReader.readValue(evidenceNode);
    }

    public void assertConsentSection(String consentSection) {
        assertEquals(consentSection, dvlaConsentSection.getText());
    }

    public void assertOneLoginPrivacyLink(String oneLoginPrivacyLink) {
        assertEquals(oneLoginPrivacyLink, oneLoginLink.getText());
        String oneLoginDVLALinkUrl = oneLoginLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
        oneLoginLink.click();

        Object[] windowHandles = Driver.get().getWindowHandles().toArray();

        System.out.println("Window handles: " + Arrays.toString(windowHandles));

        Driver.get().switchTo().window((String) windowHandles[1]);
        Driver.get().close();
        Driver.get().switchTo().window((String) windowHandles[0]);
    }

    public void assertDVLAPrivacyLink(String dvlaPrivacyLink) {
        assertEquals(dvlaPrivacyLink, dvlaLink.getText());
        String oneLoginDVLALinkUrl = dvlaLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
    }

    public void assertDVLAContent(String contentDVLA) {
        assertEquals(contentDVLA, dvlaSentence.getText());
    }

    public void assertDVLAContentLineTwo(String contentDVLALine2) {
        assertEquals(contentDVLALine2, dvlaSentenceTwo.getText());
    }
}
