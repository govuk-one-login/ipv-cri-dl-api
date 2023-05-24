package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.TestDataCreator;
import gov.di_ipv_drivingpermit.utilities.TestInput;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static gov.di_ipv_drivingpermit.pages.Headers.IPV_CORE_STUB;
import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.checkOkHttpResponseOnLink;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicencePageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER = Logger.getLogger(DrivingLicencePageObject.class.getName());

    // Should be separate stub page

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI dev\"]")
    public WebElement drivingLicenceCRIDev;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Build\"]")
    public WebElement drivingLicenceCRIBuild;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Staging\"]")
    public WebElement drivingLicenceCRIStaging;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Integration\"]")
    public WebElement drivingLicenceCRIIntegration;

    @FindBy(id = "rowNumber")
    public WebElement selectRow;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/div/pre")
    public WebElement JSONPayload;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details")
    public WebElement errorResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/summary/span")
    public WebElement viewResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement searchButton;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement goToDLCRIButton;

    // ---------------------

    // Should be separate select issuer page

    @FindBy(xpath = "//*[@id=\"licenceIssuer-fieldset\"]/div/div[3]")
    public WebElement orLabel;

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

    @FindBy(id = "submitButton")
    public WebElement CTButton;

    @FindBy(id = "licenceIssuer-error")
    public WebElement radioButtonError;

    // ---------------

    @FindBy(className = "error-summary")
    public WebElement errorSummary;

    @FindBy(xpath = "//*[@class='govuk-notification-banner__content']")
    public WebElement userNotFoundInThirdPartyBanner;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div/a")
    public WebElement proveAnotherWay;

    @FindBy(id = "govuk-notification-banner-title")
    public WebElement errorText;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]/p[1]")
    public WebElement thereWasAProblemFirstSentence;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/p")
    public WebElement pageDescriptionHeading;

    @FindBy(xpath = "/html/body/div[2]/div/p/strong")
    public WebElement betaBanner;

    @FindBy(className = "govuk-phase-banner__text")
    public WebElement betaBannerText;

    @FindBy(id = "error-summary-title")
    public WebElement errorSummaryTitle;

    @FindBy(id = "drivingLicenceNumber")
    public WebElement LicenceNumber;

    @FindBy(id = "surname")
    public WebElement LastName;

    @FindBy(id = "firstName")
    public WebElement FirstName;

    @FindBy(id = "middleNames")
    public WebElement MiddleNames;

    @FindBy(id = "dateOfBirth-day")
    public WebElement birthDay;

    @FindBy(id = "dateOfBirth-month")
    public WebElement birthMonth;

    @FindBy(id = "dateOfBirth-year")
    public WebElement birthYear;

    @FindBy(id = "expiryDate-day")
    public WebElement LicenceValidToDay;

    @FindBy(id = "expiryDate-month")
    public WebElement LicenceValidToMonth;

    @FindBy(id = "expiryDate-year")
    public WebElement LicenceValidToYear;

    @FindBy(id = "issueDate-day")
    public WebElement LicenceIssueDay;

    @FindBy(id = "issueDate-month")
    public WebElement LicenceIssueMonth;

    @FindBy(id = "issueDate-year")
    public WebElement LicenceIssueYear;

    @FindBy(id = "issueNumber")
    public WebElement IssueNumber;

    @FindBy(id = "postcode")
    public WebElement Postcode;

    @FindBy(id = "consentCheckbox")
    public WebElement consentDVLACheckbox;

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement Continue;

    @FindBy(id = "header")
    public WebElement pageHeader;

    @FindBy(xpath = "//*[@class='govuk-back-link']")
    public WebElement back;

    @FindBy(className = "govuk-details__summary-text")
    public WebElement whyWeText;

    @FindBy(className = "govuk-details__text")
    public WebElement whyWePara;

    // Error summary items

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#licenceIssuer')]")
    public WebElement InvalidDocumentIssuerInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'-day')]")
    public WebElement InvalidDOBErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'LicenceNumber')]")
    public WebElement InvalidDrivingLicenceErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#surname')]")
    public WebElement InvalidLastNameErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#firstName')]")
    public WebElement InvalidFirstNameErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#middleNames')]")
    public WebElement InvalidMiddleNamesErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueDate-day')]")
    public WebElement InvalidIssueDateErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#expiryDate-day')]")
    public WebElement InvalidValidToDateErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueNumber')]")
    public WebElement InvalidIssueNumberErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#postcode')]")
    public WebElement InvalidPostcodeErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#consentCheckbox')]")
    public WebElement DVLAConsentErrorInSummary;

    // -------------------------

    // Field errors

    @FindBy(id = "dateOfBirth-error")
    public WebElement InvalidDateOfBirthFieldError;

    @FindBy(id = "surname-error")
    public WebElement InvalidLastNameFieldError;

    @FindBy(id = "firstName-error")
    public WebElement InvalidFirstNameFieldError;

    @FindBy(id = "middleNames-error")
    public WebElement InvalidMiddleNamesFieldError;

    @FindBy(id = "issueDate-error")
    public WebElement InvalidIssueDateFieldError;

    @FindBy(id = "expiryDate-error")
    public WebElement InvalidValidToDateFieldError;

    @FindBy(id = "drivingLicenceNumber-error")
    public WebElement DrivingLicenceFieldError;

    @FindBy(id = "issueNumber-error")
    public WebElement InvalidIssueNumberFieldError;

    @FindBy(id = "postcode-error")
    public WebElement InvalidPostcodeFieldError;

    @FindBy(id = "consentCheckbox-error")
    public WebElement DVLAConsentCheckboxError;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/h2")
    public WebElement DVLAConsentSection;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[1]/a")
    public WebElement oneLoginLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[2]/a")
    public WebElement dvlaLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[1]")
    public WebElement dvlaSentence;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[2]")
    public WebElement dvlaSentenceTwo;

    // ------------------------

    // --- Hints ---
    @FindBy(id = "dateOfBirth-hint")
    public WebElement dateOfBirthHint;

    @FindBy(id = "issueDate-hint")
    public WebElement issueDateHint;

    @FindBy(id = "drivingLicenceNumber-hint")
    public WebElement licenceNumberHint;

    @FindBy(id = "issueNumber-hint")
    public WebElement issueNumberHint;

    @FindBy(id = "firstName-hint")
    public WebElement firstNameHint;

    @FindBy(id = "middleNames-hint")
    public WebElement middleNameHint;

    @FindBy(id = "expiryDate-hint")
    public WebElement validToHint;

    @FindBy(id = "postcode-hint")
    public WebElement postcodeHint;

    // --- Legend text ---
    @FindBy(xpath = "//*[@id=\"dateOfBirth-fieldset\"]/legend")
    public WebElement dateOfBirthLegend;

    @FindBy(xpath = "//*[@id=\"issueDate-fieldset\"]/legend")
    public WebElement issueDateLegend;

    @FindBy(xpath = "//*[@id=\"expiryDate-fieldset\"]/legend")
    public WebElement validToLegend;

    // --- Label text ---
    @FindBy(id = "drivingLicenceNumber-label")
    public WebElement licenceNumberFieldLabel;

    @FindBy(id = "issueNumber-label")
    public WebElement issueNumberFieldLabel;

    @FindBy(id = "postcode-label")
    public WebElement postcodeLabel;

    public DrivingLicencePageObject() {
        this.configurationService = new ConfigurationService(System.getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
        TestDataCreator.createDefaultResponses();
    }

    // Should be in stub page

    public void navigateToIPVCoreStub() {
        String coreStubUrl = configurationService.getCoreStubUrl(true);
        Driver.get().get(coreStubUrl);
        waitForTextToAppear(IPV_CORE_STUB);
    }

    public void whyWeNeedToKnowThis() {
        Assert.assertTrue(whyWeText.isDisplayed());
        LOGGER.info(whyWeText.getText());
    }

    public void clickOnWhyWeNeedLink() {
        whyWeText.click();
    }

    public void paragraphValidation() {
        Assert.assertTrue(whyWePara.isDisplayed());
        LOGGER.info(whyWePara.getText());
    }

    public void navigateToDrivingLicenceCRIOnTestEnv() {
        visitCredentialIssuers.click();
        String dlCRITestEnvironment = configurationService.getDlCRITestEnvironment();
        LOGGER.info("dlCRITestEnvironment = " + dlCRITestEnvironment);
        if (dlCRITestEnvironment.equalsIgnoreCase("dev")
                || dlCRITestEnvironment.equalsIgnoreCase("local")) {
            drivingLicenceCRIDev.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Build")) {
            drivingLicenceCRIBuild.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Staging")) {
            drivingLicenceCRIStaging.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Integration")) {
            drivingLicenceCRIIntegration.click();
        } else {
            LOGGER.info("No test environment is set");
        }
    }

    public void searchForUATUser(String number) {
        assertURLContains("credential-issuer?cri=driving-licence");
        selectRow.sendKeys(number);
        searchButton.click();
    }

    public void navigateToDrivingLicenceResponse(String validOrInvalid) {
        assertURLContains("callback");
        if ("Invalid".equalsIgnoreCase(validOrInvalid)) {
            errorResponse.click();
        } else {
            viewResponse.click();
        }
    }

    public void navigateToDrivingLicenceCRI() {
        goToDLCRIButton.click();
    }

    // ------------------

    // Should be seperate page

    public void betaBanner() {
        betaBanner.isDisplayed();
    }

    public void betaBannerSentence(String expectedText) {
        Assert.assertEquals(expectedText, betaBannerText.getText());
    }

    public void titleDVLAWithRadioBtn() {
        optionDVLA.isDisplayed();
        radioBtnDVLA.isDisplayed();
    }

    public void titleDVAWithRadioBtn() {
        optionDVA.isDisplayed();
        radioBtnDVA.isDisplayed();
    }

    public void noDrivingLicenceBtn(String expectedText) {
        noDLOption.isDisplayed();
        Assert.assertEquals(expectedText, noDLOption.getText());
        noDLRadioBtn.isDisplayed();
    }

    public void ContinueButton() {
        CTButton.isDisplayed();
        CTButton.isEnabled();
    }

    public void clickOnDVLARadioButton() {
        radioBtnDVLA.click();
        CTButton.click();
    }

    public void clickOnDVARadioButton() {
        radioBtnDVA.click();
        CTButton.click();
    }

    public void clickOnIDoNotHaveAUKDrivingLicenceRadioButton() {
        noDLRadioBtn.click();
        Continue.click();
    }

    public void pageTitleDVLAValidation() {
        assert (Driver.get().getTitle().contains("We’ll check your details with DVLA "));
    }

    public void noSelectContinue() {
        CTButton.click();
    }

    public void assertErrorTitle(String expectedText) {
        Assert.assertEquals(expectedText, InvalidDocumentIssuerInSummary.getText());
    }

    public void errorMessage() {
        errorSummary.isDisplayed();
    }

    public void errorLink() {
        errorSummary.click();
        radioBtnDVLA.isEnabled();
    }

    public void validateErrorText() {
        String expectedText = "Error:\n" + "You must choose an option to continue";
        String actualText = radioButtonError.getText();
        Assert.assertEquals(expectedText, actualText);
    }

    public void drivingLicencePageURLValidationWelsh() {
        assertURLContains("/licence-issuer/?lang=cy");
    }

    public void assertOrLabelText(String expectedText) {
        Assert.assertEquals(expectedText, orLabel.getText());
    }

    // -----------------------

    public void drivingLicencePageURLValidation(String path) {
        assertURLContains(path);
    }

    public void assertUserRoutedToIpvCore() {
        assertPageTitle("IPV Core Stub - GOV.UK");
    }

    public void assertUserRoutedToIpvCoreErrorPage() {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        String expUrl =
                coreStubUrl
                        + "/callback?error=access_denied&error_description=Authorization+permission+denied";
        String actUrl = Driver.get().getCurrentUrl();
        LOGGER.info("expectedUrl = " + expUrl);
        LOGGER.info("actualUrl = " + actUrl);
        Assert.assertEquals(actUrl, expUrl);
    }

    public void jsonErrorResponse(String expectedErrorDescription, String expectedErrorStatusCode)
            throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);

        JsonNode insideError = getJsonNode(result, "errorObject");
        LOGGER.info("insideError = " + insideError);

        JsonNode errorDescription = insideError.get("description");
        JsonNode statusCode = insideError.get("httpstatusCode");
        String ActualErrorDescription = insideError.get("description").asText();
        String ActualStatusCode = insideError.get("httpstatusCode").asText();

        LOGGER.info("errorDescription = " + errorDescription);
        LOGGER.info("statusCode = " + statusCode);
        LOGGER.info("testErrorDescription = " + expectedErrorDescription);
        LOGGER.info("testStatusCode = " + expectedErrorStatusCode);

        Assert.assertEquals(expectedErrorDescription, ActualErrorDescription);
        Assert.assertEquals(expectedErrorStatusCode, ActualStatusCode);
    }

    public void checkScoreInStubIs(String validityScore, String strengthScore) throws IOException {
        scoreIs(validityScore, strengthScore, JSONPayload.getText());
    }

    public void scoreIs(String validityScore, String strengthScore, String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = " + result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String ValidityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(ValidityScore, validityScore);

        String StrengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(StrengthScore, strengthScore);
    }

    public void userNotFoundInThirdPartyErrorIsDisplayed() {
        Assert.assertTrue(userNotFoundInThirdPartyBanner.isDisplayed());
        LOGGER.info(userNotFoundInThirdPartyBanner.getText());
    }

    public void userEntersData(String issuer, String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getDVATestUserFromMap(issuer, drivingLicenceSubjectScenario);
        if (issuer.equals("DVLA")) {
            LicenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
            birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
            birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
            birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
            LicenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
            LicenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
            LicenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
            if (null != drivingLicenceSubject.getIssueNumber()) {
                IssueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
            }
            consentDVLACheckbox.click();
        }
        if (null != drivingLicenceSubject.getMiddleNames()) {
            MiddleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }

        LastName.sendKeys(drivingLicenceSubject.getLastName());
        FirstName.sendKeys(drivingLicenceSubject.getFirstName());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        Postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    // Why is this invalid
    public void userEntersInvalidDrivingDetails() {
        DrivingLicencePageObject enterYourDetailsExactlyDVLAPage = new DrivingLicencePageObject();
        enterYourDetailsExactlyDVLAPage.LicenceNumber.sendKeys("PARKE610112PBFGI");
        enterYourDetailsExactlyDVLAPage.LastName.sendKeys("Testlastname");
        enterYourDetailsExactlyDVLAPage.FirstName.sendKeys("Testfirstname");
        enterYourDetailsExactlyDVLAPage.birthDay.sendKeys("11");
        enterYourDetailsExactlyDVLAPage.birthMonth.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.birthYear.sendKeys("1962");
        enterYourDetailsExactlyDVLAPage.LicenceValidToDay.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.LicenceValidToMonth.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.LicenceValidToYear.sendKeys("2030");
        enterYourDetailsExactlyDVLAPage.LicenceIssueDay.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.LicenceIssueMonth.sendKeys("12");
        enterYourDetailsExactlyDVLAPage.LicenceIssueYear.sendKeys("1970");
        enterYourDetailsExactlyDVLAPage.IssueNumber.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.Postcode.sendKeys("BS98 1AA");
        consentDVLACheckbox.click();

        BrowserUtils.waitForPageToLoad(10);
    }

    public void enterInvalidLastAndFirstName() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LastName.sendKeys("Parker!");
        drivingLicencePageObject.FirstName.sendKeys("Peter@@!");
        drivingLicencePageObject.MiddleNames.sendKeys("@@@@@@@");
    }

    public void enterDvlaBirthYear(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.birthDay.clear();
        drivingLicencePageObject.birthDay.click();
        drivingLicencePageObject.birthDay.sendKeys(day);
        drivingLicencePageObject.birthMonth.clear();
        drivingLicencePageObject.birthMonth.click();
        drivingLicencePageObject.birthMonth.sendKeys(month);
        drivingLicencePageObject.birthYear.clear();
        drivingLicencePageObject.birthYear.click();
        drivingLicencePageObject.birthYear.sendKeys(year);
    }

    public void enterIssueDate(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceIssueDay.clear();
        drivingLicencePageObject.LicenceIssueDay.click();
        drivingLicencePageObject.LicenceIssueDay.sendKeys(day);
        drivingLicencePageObject.LicenceIssueMonth.clear();
        drivingLicencePageObject.LicenceIssueMonth.click();
        drivingLicencePageObject.LicenceIssueMonth.sendKeys(month);
        drivingLicencePageObject.LicenceIssueYear.clear();
        drivingLicencePageObject.LicenceIssueYear.click();
        drivingLicencePageObject.LicenceIssueYear.sendKeys(year);
    }

    public void enterValidToDate(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceValidToDay.clear();
        drivingLicencePageObject.LicenceValidToDay.click();
        drivingLicencePageObject.LicenceValidToDay.sendKeys(day);
        drivingLicencePageObject.LicenceValidToMonth.clear();
        drivingLicencePageObject.LicenceValidToMonth.click();
        drivingLicencePageObject.LicenceValidToMonth.sendKeys(month);
        drivingLicencePageObject.LicenceValidToYear.clear();
        drivingLicencePageObject.LicenceValidToYear.click();
        drivingLicencePageObject.LicenceValidToYear.sendKeys(year);
    }

    public void enterLicenceNumber(String licenceNumber) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceNumber.clear();
        drivingLicencePageObject.LicenceNumber.click();
        drivingLicencePageObject.LicenceNumber.sendKeys(licenceNumber);
    }

    public void clearIssueNumber() {
        this.IssueNumber.clear();
    }

    public void enterIssueNumber(String issueNumber) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.IssueNumber.clear();
        drivingLicencePageObject.IssueNumber.click();
        drivingLicencePageObject.IssueNumber.sendKeys(issueNumber);
    }

    public void enterPostcode(String postcode) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.Postcode.clear();
        drivingLicencePageObject.Postcode.click();
        drivingLicencePageObject.Postcode.sendKeys(postcode);
    }

    public void userReEntersDataAsADrivingLicenceSubject(String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getDVATestUserFromMap("DVLA", drivingLicenceSubjectScenario);

        LicenceNumber.clear();
        LastName.clear();
        FirstName.clear();
        MiddleNames.clear();
        birthDay.clear();
        birthMonth.clear();
        birthYear.clear();
        LicenceValidToDay.clear();
        LicenceValidToMonth.clear();
        LicenceValidToYear.clear();
        LicenceIssueDay.clear();
        LicenceIssueMonth.clear();
        LicenceIssueYear.clear();
        IssueNumber.clear();
        Postcode.clear();
        LicenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
        LastName.sendKeys(drivingLicenceSubject.getLastName());
        FirstName.sendKeys(drivingLicenceSubject.getFirstName());
        if (null != drivingLicenceSubject.getMiddleNames()) {
            MiddleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }
        if (null != drivingLicenceSubject.getIssueNumber()) {
            IssueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
        }
        birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
        birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
        birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        LicenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
        LicenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
        LicenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
        Postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    public void assertInvalidDoBInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidDOBErrorInSummary.getText());
    }

    public void assertInvalidDoBOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidDateOfBirthFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidIssueDateInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidIssueDateErrorInSummary.getText());
    }

    public void assertInvalidIssueDateOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidIssueDateFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidValidToDateInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidValidToDateErrorInSummary.getText());
    }

    public void assertInvalidValidToDateOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidValidToDateFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidLicenceNumberInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidDrivingLicenceErrorInSummary.getText());
    }

    public void assertInvalidLicenceNumberOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, DrivingLicenceFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidIssueNumberInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidIssueNumberErrorInSummary.getText());
    }

    public void assertInvalidIssueNumberOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidIssueNumberFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidPostcodeInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidPostcodeErrorInSummary.getText());
    }

    public void assertInvalidPostcodeOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidPostcodeFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidLastNameInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidLastNameErrorInSummary.getText());
    }

    public void assertInvalidLastNameOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidLastNameFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidFirstNameInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidFirstNameErrorInSummary.getText());
    }

    public void assertInvalidFirstNameOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidFirstNameFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidMiddleNameInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, InvalidMiddleNamesErrorInSummary.getText());
    }

    public void assertInvalidMiddleNameOnField(String expectedText) {
        Assert.assertEquals(
                expectedText, InvalidMiddleNamesFieldError.getText().trim().replace("\n", ""));
    }

    public void ciInVC(String ci) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        JsonNode vcNode = getJsonNode(result, "vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        List<String> cis = getCIsFromEvidence(evidenceNode);

        if (StringUtils.isNotEmpty(ci)) {
            if (cis.size() > 0) {
                assertTrue(cis.contains(ci));
            } else {
                fail("No CIs found");
            }
        }
    }

    public void assertPersonalNumberInVc(String personalNumber) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        JsonNode vcNode = getJsonNode(result, "vc");
        String licenceNumber = getPersonalNumberFromVc(vcNode);
        assertEquals(personalNumber, licenceNumber);
    }

    public void validateErrorPageHeading() {
        String expectedHeading = "Sorry, there is a problem";
        String actualHeading = pageHeader.getText();
        if (expectedHeading.equals(actualHeading)) {
            LOGGER.info("Pass : Sorry, there is a problem page is displayed");
        } else {
            LOGGER.info("Fail : Who was your UK driving licence issued by? is displayed");
            fail("Fail : Who was your UK driving licence issued by? is displayed");
        }
    }

    public void assertPageTitle(String expTitle) {
        String actualTitle = Driver.get().getTitle();

        LOGGER.info("Page title: " + actualTitle);
        Assert.assertEquals(expTitle, actualTitle);
    }

    public void assertPageHeading(String expectedText) {
        Assert.assertEquals(expectedText, pageHeader.getText().split("\n")[0]);
    }

    public void assertProveAnotherWayLinkText(String expectedText) {
        Assert.assertEquals(expectedText, getParent(proveAnotherWay).getText());
    }

    public void assertErrorPrefix(String expectedText) {
        Assert.assertEquals(expectedText, errorText.getText());
    }

    public void assertFirstLineOfUserNotFoundText(String expectedText) {
        Assert.assertEquals(expectedText, userNotFoundInThirdPartyBanner.getText().split("\n")[0]);
    }

    public void youWillBeAbleToFindSentence(String expectedText) {
        Assert.assertEquals(expectedText, thereWasAProblemFirstSentence.getText());
    }

    public void assertPageSourceContains(String expectedText) {
        assert (Driver.get().getPageSource().contains(expectedText));
    }

    public void assertLastNameLabelText(String expectedText) {
        Assert.assertEquals(expectedText, getLabel(getParent(LastName)).getText());
    }

    public void assertGivenNameLegendText(String expectedText) {
        Assert.assertEquals(
                expectedText,
                FirstName.findElement(By.xpath("./../../.."))
                        .findElement(By.tagName("legend"))
                        .getText());
    }

    public void assertMiddleNameLabelText(String expectedText) {
        Assert.assertEquals(expectedText, getLabel(getParent(MiddleNames)).getText());
    }

    public void assertGivenNameDescription(String expectedText) {
        Assert.assertEquals(
                expectedText, getLabel(firstNameHint.findElement(By.xpath("./../.."))).getText());
    }

    public void assertGivenNameHint(String expectedText) {
        Assert.assertEquals(expectedText, firstNameHint.getText());
    }

    public void assertMiddleNameHint(String expectedText) {
        Assert.assertEquals(expectedText, middleNameHint.getText());
    }

    public void assertDateOfBirthLegendText(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthLegend.getText());
    }

    public void assertDateOfBirthHintText(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthHint.getText());
    }

    public void assertBirthDayLabelText(String expectedText) {
        Assert.assertEquals(expectedText, getLabel(getParent(birthDay)).getText());
    }

    public void assertBirthMonthLabelText(String expectedText) {
        Assert.assertEquals(expectedText, getLabel(getParent(birthMonth)).getText());
    }

    public void assertBirthYearLabelText(String expectedText) {
        Assert.assertEquals(expectedText, getLabel(getParent(birthYear)).getText());
    }

    public void assertIssueDateLegendText(String expectedText) {
        Assert.assertEquals(expectedText, issueDateLegend.getText());
    }

    public void assertIssueDateHintText(String expectedText) {
        Assert.assertEquals(expectedText, issueDateHint.getText());
    }

    public void assertValidToHintText(String expectedText) {
        Assert.assertEquals(expectedText, validToHint.getText());
    }

    public void assertValidToHintTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, validToHint.getText());
    }

    public void assertLicenceNumberLabelText(String expectedText) {
        Assert.assertEquals(expectedText, licenceNumberFieldLabel.getText());
    }

    public void assertLicenceNumberHintText(String expectedText) {
        Assert.assertEquals(expectedText, licenceNumberHint.getText());
    }

    public void assertIssueNumberLabelText(String expectedText) {
        Assert.assertEquals(expectedText, issueNumberFieldLabel.getText());
    }

    public void assertIssueNumberHintText(String expectedText) {
        Assert.assertEquals(expectedText, issueNumberHint.getText());
    }

    public void assertPostcodeLabelText(String expectedText) {
        Assert.assertEquals(expectedText, postcodeLabel.getText());
    }

    public void assertPostcodeHintText(String expectedText) {
        Assert.assertEquals(expectedText, postcodeHint.getText());
    }

    public void assertPageDescription(String expectedText) {
        Assert.assertEquals(expectedText, pageDescriptionHeading.getText());
    }

    public void assertValidToLegend(String expectedText) {
        Assert.assertEquals(expectedText, validToLegend.getText());
    }

    public void assertErrorSummaryText(String expectedText) {
        Assert.assertEquals(expectedText, errorSummaryTitle.getText());
    }

    public void assertIssueNumberDescriptionText(String expectedText) {
        Assert.assertEquals(
                expectedText,
                getParent(issueNumberFieldLabel)
                        .findElement(By.tagName("p"))
                        .getText()
                        .trim()
                        .replace("\n", ""));
    }

    public void assertCTATextAs(String expectedText) {
        assertEquals(CTButton.getText(), expectedText);
    }

    private List<String> getCIsFromEvidence(JsonNode evidenceNode) throws IOException {
        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        List<String> cis =
                getListOfNodes(evidence.get(0), "ci").stream()
                        .map(JsonNode::asText)
                        .collect(Collectors.toList());
        return cis;
    }

    private JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        return jsonNode.get(vc);
    }

    private String getPersonalNumberFromVc(JsonNode vcNode) throws IOException {
        JsonNode credentialSubject = vcNode.findValue("credentialSubject");
        List<JsonNode> evidence = getListOfNodes(credentialSubject, "drivingPermit");

        String licenceNumber = evidence.get(0).get("personalNumber").asText();
        return licenceNumber;
    }

    private List<JsonNode> getListOfNodes(JsonNode vcNode, String evidence) throws IOException {
        JsonNode evidenceNode = vcNode.get(evidence);

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        return objectReader.readValue(evidenceNode);
    }

    private WebElement getParent(WebElement webElement) {
        return webElement.findElement(By.xpath("./.."));
    }

    private WebElement getLabel(WebElement webElement) {
        return webElement.findElement(By.tagName("label"));
    }

    public void assertDVLAConsentErrorInErrorSummary(String expectedText) {
        Assert.assertEquals(expectedText, DVLAConsentErrorInSummary.getText());
    }

    public void assertDVLAConsentErrorOnCheckbox(String expectedText) {
        Assert.assertEquals(
                expectedText, DVLAConsentCheckboxError.getText().trim().replace("\n", ""));
    }

    public void goToPage(String page) {
        waitForTextToAppear(page);
    }

    public void assertConsentSection(String consentSection) {
        Assert.assertEquals(consentSection, DVLAConsentSection.getText());
    }

    public void assertOneLoginPrivacyLink(String oneLoginPrivacyLink) {
        Assert.assertEquals(oneLoginPrivacyLink, oneLoginLink.getText());
        String oneLoginDVLALinkUrl = oneLoginLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
        oneLoginLink.click();
    }

    public void assertDVLAPrivacyLink(String dvlaPrivacyLink) {
        Assert.assertEquals(dvlaPrivacyLink, dvlaLink.getText());
        String oneLoginDVLALinkUrl = dvlaLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
    }

    public void assertDVLAContent(String contentDVLA) {
        Assert.assertEquals(contentDVLA, dvlaSentence.getText());
    }

    public void assertDVLAContentLineTwo(String contentDVLALine2) {
        Assert.assertEquals(contentDVLALine2, dvlaSentenceTwo.getText());
    }
}
