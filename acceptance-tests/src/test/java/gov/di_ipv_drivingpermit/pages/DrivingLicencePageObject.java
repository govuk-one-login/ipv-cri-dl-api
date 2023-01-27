package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.DrivingLicenceSubject;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static gov.di_ipv_drivingpermit.pages.Headers.IPV_CORE_STUB;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicencePageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER = Logger.getLogger(DrivingLicencePageObject.class.getName());

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Build\"]")
    public WebElement drivingLicenceCRIBuild;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Staging\"]")
    public WebElement drivingLicenceCRIStaging;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Integration\"]")
    public WebElement drivingLicenceCRIIntegration;

    @FindBy(id = "licenceIssuerRadio-DVLA-label")
    public WebElement optionDVLA;

    @FindBy(id = "licenceIssuerRadio")
    public WebElement radioBtnDVLA;

    @FindBy(xpath = "//*[@id=\"licenceIssuerRadio-DVA-label\"]")
    public WebElement optionDVA;

    @FindBy(id = "licenceIssuerRadio-DVA")
    public WebElement radioBtnDVA;

    @FindBy(id = "licenceIssuerRadio-noLicence-label")
    public WebElement noDLOption;

    @FindBy(id = "licenceIssuerRadio-noLicence")
    public WebElement noDLRadioBtn;

    @FindBy(id = "submitButton")
    public WebElement CTButton;

    @FindBy(id = "rowNumber")
    public WebElement selectRow;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul")
    public WebElement errorSummary;

    @FindBy(xpath = "//*[@id=\"licenceIssuerRadio-error\"]")
    public WebElement errorText;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/div/pre")
    public WebElement JSONPayload;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details")
    public WebElement errorResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/summary/span")
    public WebElement viewResponse;

    @FindBy(xpath = "//*[@class='govuk-notification-banner__content']")
    public WebElement InvalidLicenceDetailsError;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div/a")
    public WebElement proveanotherway;

    @FindBy(id = "licenceIssuerRadio-noLicence")
    public WebElement nodrivinglicenceradio;

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

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement searchButton;

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

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement Continue;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement goToDLCRIButton;

    @FindBy(xpath = "//*[@id=\"header\"]")
    public WebElement errorTitle;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dateOfBirth-day')]")
    public WebElement InvalidDOBError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#drivingLicenceNumber')]")
    public WebElement InvalidDrivingLicenceError;

    @FindBy(xpath = "//*[@class='govuk-back-link']")
    public WebElement back;

    @FindBy(id = "dateOfBirth-error")
    public WebElement InvalidDateOfBirthFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#surname')]")
    public WebElement InvalidLastNameError;

    @FindBy(id = "surname-error")
    public WebElement InvalidLastNameFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#firstName')]")
    public WebElement InvalidFirstNameError;

    @FindBy(id = "firstName-error")
    public WebElement InvalidFirstNameFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#middleNames')]")
    public WebElement InvalidMiddleNamesError;

    @FindBy(id = "middleNames-error")
    public WebElement InvalidMiddleNamesFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueDate-day')]")
    public WebElement InvalidIssueDateError;

    @FindBy(id = "issueDate-error")
    public WebElement InvalidIssueDateFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#expiryDate-day')]")
    public WebElement InvalidValidToDateError;

    @FindBy(id = "expiryDate-error")
    public WebElement InvalidValidToDateFieldError;

    @FindBy(id = "drivingLicenceNumber-error")
    public WebElement DrivingLicenceFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueNumber')]")
    public WebElement InvalidIssueNumberError;

    @FindBy(id = "issueNumber-error")
    public WebElement InvalidIssueNumberFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#postcode')]")
    public WebElement InvalidPostcodeError;

    @FindBy(id = "postcode-error")
    public WebElement InvalidPostcodeFieldError;

    public DrivingLicencePageObject() {
        this.configurationService = new ConfigurationService(System.getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
    }

    public void navigateToIPVCoreStub() {
        String coreStubUrl = configurationService.getCoreStubUrl(true);
        Driver.get().get(coreStubUrl);
        waitForTextToAppear(IPV_CORE_STUB);
    }

    public void navigateToDrivingLicenceCRI(String environment) {
        visitCredentialIssuers.click();
        assertURLContains("credential-issuers");
        switch (environment) {
            case "Build":
                drivingLicenceCRIBuild.click();
                break;

            case "Staging":
                drivingLicenceCRIStaging.click();
                break;

            case "Integration":
                drivingLicenceCRIIntegration.click();
                break;

            default:
                break;
        }
    }

    public void drivingLicencePageURLValidation() {
        String actualUrl = Driver.get().getCurrentUrl();
        LOGGER.info("actualUrl = " + actualUrl);
        assert (actualUrl.contains("review-d"));
    }

    public void validateDLPageTitle() {
        String actualTitle = Driver.get().getTitle();
        String expTitle = "Who was your UK driving licence issued by? – – GOV.UK";
        if (actualTitle.equals(expTitle)) {
            LOGGER.info("Pass : directed to Who was your UK driving license issued by?");
        } else {
            LOGGER.info("Fail : not directed to the Driving Licence Page");
        }
    }

    public void titleDVLAWithRadioBtn() {
        optionDVLA.isDisplayed();
        radioBtnDVLA.isDisplayed();
    }

    public void titleDVAWithRadioBtn() {
        optionDVA.isDisplayed();
        radioBtnDVA.isDisplayed();
    }

    public void noDrivingLicenceBtn() {
        noDLOption.isDisplayed();
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

    public void pageTitleDVLAValidation() {
        if (Driver.get().getTitle().contains("We’ll check your details with DVLA ")) {
            LOGGER.info("Page title contains \"We’ll check your details with DVLA \" ");
        }
    }

    public void pageTitleDVAValidation() {
        if (Driver.get().getTitle().contains("We’ll check your details with DVA ")) {
            LOGGER.info("Page title contains \"We’ll check your details with DVA \" ");
        }
    }

    public void clickOnDVARadioButton() {
        radioBtnDVA.click();
        CTButton.click();
    }

    public void noDrivingLicenceOption() {
        noDLOption.click();
        CTButton.click();
    }

    public void ipvCoreRoutingPage() {
        String actualTitle = Driver.get().getTitle();
        String expTitle = "IPV Core Stub - GOV.UK";
        Assert.assertEquals(expTitle, actualTitle);
    }

    public void ipvCoreRoutingPageURL() {
        String coreStubUrl = configurationService.getCoreStubUrl(true);
        String expUrl =
                coreStubUrl
                        + "/callback?error=access_denied&error_description=Authorization+permission+denied";
        String actUrl = Driver.get().getCurrentUrl();
        LOGGER.info("expectedUrl = " + expUrl);
        LOGGER.info("actualUrl = " + actUrl);
        Assert.assertEquals(actUrl, expUrl);
    }

    public void noSelectContinue() {
        CTButton.click();
    }

    public void errormessage() {
        errorSummary.isDisplayed();
    }

    public void errorTitle() {
        if (Driver.get().getTitle().contains("You must choose an option to continue")) {
            LOGGER.info("Page title contains \"You must choose an option to continue \" ");
        }
    }

    public void errorLink() {
        errorSummary.click();
        radioBtnDVLA.isEnabled();
    }

    public void validateErrorText() {
        String expectedText = "Error:\n" + "You must choose an option to continue";
        String actualText = errorText.getText();
        Assert.assertEquals(expectedText, actualText);
    }

    public void jsonErrorResponse(String testErrorDescription, String testStatusCode)
            throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode insideError = jsonNode.get("errorObject");
        LOGGER.info("insideError = " + insideError);
        JsonNode errorDescription = insideError.get("description");
        JsonNode statusCode = insideError.get("httpstatusCode");
        String ActualErrorDescription = insideError.get("description").asText();
        String ActualStatusCode = insideError.get("httpstatusCode").asText();
        LOGGER.info("errorDescription = " + errorDescription);
        LOGGER.info("statusCode = " + statusCode);
        LOGGER.info("testErrorDescription = " + testErrorDescription);
        LOGGER.info("testStatusCode = " + testStatusCode);
        Assert.assertEquals(testErrorDescription, ActualErrorDescription);
        Assert.assertEquals(testStatusCode, ActualStatusCode);
    }

    public void scoreIs(String validityScore, String strengthScore) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        String ValidityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(ValidityScore, validityScore);

        String StrengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(StrengthScore, strengthScore);
    }

    public void couldNotFindDetailsErrorDisplayed() {
        Assert.assertTrue(InvalidLicenceDetailsError.isDisplayed());
        LOGGER.info(InvalidLicenceDetailsError.getText());
    }

    public void userEntersData(DrivingLicenceSubject drivingLicenceSubject) {
        LicenceNumber.sendKeys(drivingLicenceSubject.getlicenceNumber());
        LastName.sendKeys(drivingLicenceSubject.getlastName());
        FirstName.sendKeys(drivingLicenceSubject.getfirstName());
        MiddleNames.sendKeys(drivingLicenceSubject.getmiddleNames());
        birthDay.sendKeys(drivingLicenceSubject.getbirthDay());
        birthMonth.sendKeys(drivingLicenceSubject.getbirthMonth());
        birthYear.sendKeys(drivingLicenceSubject.getbirthYear());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getvalidtoDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getvalidtoMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getvalidtoYear());
        LicenceIssueDay.sendKeys(drivingLicenceSubject.getlicenceIssueDay());
        LicenceIssueMonth.sendKeys(drivingLicenceSubject.getlicenceIssueMonth());
        LicenceIssueYear.sendKeys(drivingLicenceSubject.getlicenceIssueYear());
        IssueNumber.sendKeys(drivingLicenceSubject.getlicenceIssueNumber());
        Postcode.sendKeys(drivingLicenceSubject.getpostcode());
    }

    public void userEntersInvalidDrivingDetails() {
        new EnterYourDetailsExactlyDVLAPage().drivingLicenceNumber.sendKeys("PARKE610112PBFGI");
        new EnterYourDetailsExactlyDVLAPage().Lastname.sendKeys("Testlastname");
        new EnterYourDetailsExactlyDVLAPage().Firstname.sendKeys("Testfirstname");
        new EnterYourDetailsExactlyDVLAPage().DayOfBirth.sendKeys("11");
        new EnterYourDetailsExactlyDVLAPage().MonthOfBirth.sendKeys("10");
        new EnterYourDetailsExactlyDVLAPage().YearOfBirth.sendKeys("1962");
        new EnterYourDetailsExactlyDVLAPage().LicenceValidToDay.sendKeys("01");
        new EnterYourDetailsExactlyDVLAPage().LicenceValidToMonth.sendKeys("01");
        new EnterYourDetailsExactlyDVLAPage().LicenceValidToYear.sendKeys("2030");
        new EnterYourDetailsExactlyDVLAPage().LicenceIssueDay.sendKeys("10");
        new EnterYourDetailsExactlyDVLAPage().LicenceIssueMonth.sendKeys("12");
        new EnterYourDetailsExactlyDVLAPage().LicenceIssueYear.sendKeys("1970");
        new EnterYourDetailsExactlyDVLAPage().IssueNumber.sendKeys("01");
        new EnterYourDetailsExactlyDVLAPage().Postcode.sendKeys("BS98 1AA");
        BrowserUtils.waitFor(3);
        BrowserUtils.waitForPageToLoad(10);
    }

    public void userReEntersDataAsADrivingLicenceSubject(
            DrivingLicenceSubject drivingLicenceSubject) {
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
        LicenceNumber.sendKeys(drivingLicenceSubject.getlicenceNumber());
        LastName.sendKeys(drivingLicenceSubject.getlastName());
        FirstName.sendKeys(drivingLicenceSubject.getfirstName());
        MiddleNames.sendKeys(drivingLicenceSubject.getmiddleNames());
        birthDay.sendKeys(drivingLicenceSubject.getbirthDay());
        birthMonth.sendKeys(drivingLicenceSubject.getbirthMonth());
        birthYear.sendKeys(drivingLicenceSubject.getbirthYear());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getvalidtoDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getvalidtoMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getvalidtoYear());
        LicenceIssueDay.sendKeys(drivingLicenceSubject.getlicenceIssueDay());
        LicenceIssueMonth.sendKeys(drivingLicenceSubject.getlicenceIssueMonth());
        LicenceIssueYear.sendKeys(drivingLicenceSubject.getlicenceIssueYear());
        IssueNumber.sendKeys(drivingLicenceSubject.getlicenceIssueNumber());
        Postcode.sendKeys(drivingLicenceSubject.getpostcode());
    }

    public void clickOnIDoNotHaveAUKDrivingLicenceRadioButton() {
        nodrivinglicenceradio.click();
        Continue.click();
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

    public void invalidDOBErrorDisplayed() {
        Assert.assertEquals(
                "Check you have entered your date of birth correctly", InvalidDOBError.getText());
    }

    public void invalidDateOfBirthFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Check you have entered your date of birth correctly",
                InvalidDateOfBirthFieldError.getText());
    }

    public void noDOBErrorDisplayed() {
        Assert.assertEquals(
                "Enter your date of birth as it appears on your driving licence",
                InvalidDOBError.getText());
    }

    public void noDateOfBirthFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter your date of birth as it appears on your driving licence",
                InvalidDateOfBirthFieldError.getText());
    }

    public void futureDOBErrorDisplayed() {
        Assert.assertEquals("Your date of birth must be in the past", InvalidDOBError.getText());
    }

    public void futureDOBFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your date of birth must be in the past",
                InvalidDateOfBirthFieldError.getText());
    }

    public void invalidIssueDateErrorDisplayed() {
        Assert.assertEquals(
                "Enter the date as it appears on your driving licence",
                InvalidIssueDateError.getText());
    }

    public void invalidIssueDateFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the date as it appears on your driving licence",
                InvalidIssueDateFieldError.getText());
    }

    public void futureIssueDateErrorDisplayed() {
        Assert.assertEquals("The issue date must be in the past", InvalidIssueDateError.getText());
    }

    public void futureIssueDateFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "The issue date must be in the past",
                InvalidIssueDateFieldError.getText());
    }

    public void invalidValidToDateErrorDisplayed() {
        Assert.assertEquals(
                "Enter the date as it appears on your driving licence",
                InvalidValidToDateError.getText());
    }

    public void invalidValidToDateFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the date as it appears on your driving licence",
                InvalidValidToDateFieldError.getText());
    }

    public void expiredDrivingLicenceErrorDisplayed() {
        Assert.assertEquals(
                "You cannot use an expired driving licence", InvalidValidToDateError.getText());
    }

    public void expiredDrivingLicenceFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "You cannot use an expired driving licence",
                InvalidValidToDateFieldError.getText());
    }

    public void shortDrivingLicenceNumberErrorDisplayed() {
        Assert.assertEquals(
                "Your licence number should be 16 characters long",
                InvalidDrivingLicenceError.getText());
    }

    public void shortDrivingLicenceNumberFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your licence number should be 16 characters long",
                DrivingLicenceFieldError.getText());
    }

    public void specialCharDrivingLicenceErrorDisplayed() {
        Assert.assertEquals(
                "Your licence number should not include any symbols or spaces",
                InvalidDrivingLicenceError.getText());
    }

    public void specialCharDrivingLicenceFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your licence number should not include any symbols or spaces",
                DrivingLicenceFieldError.getText());
    }

    public void invalidDrivingLicenceErrorDisplayed() {
        Assert.assertEquals(
                "Enter the number exactly as it appears on your driving licence",
                InvalidDrivingLicenceError.getText());
    }

    public void invalidDrivingLicenceFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the number exactly as it appears on your driving licence",
                DrivingLicenceFieldError.getText());
    }

    public void shortIssueNumberErrorDisplayed() {
        Assert.assertEquals(
                "Your issue number should be 2 numbers long", InvalidIssueNumberError.getText());
    }

    public void shortIssueNumberFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your issue number should be 2 numbers long",
                InvalidIssueNumberFieldError.getText());
    }

    public void invalidIssueNumberErrorDisplayed() {
        Assert.assertEquals(
                "Enter the issue number as it appears on your driving licence",
                InvalidIssueNumberError.getText());
    }

    public void invalidIssueNumberFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the issue number as it appears on your driving licence",
                InvalidIssueNumberFieldError.getText());
    }

    public void specialCharIssueNumberErrorDisplayed() {
        Assert.assertEquals(
                "Your issue number should not include any symbols or spaces",
                InvalidIssueNumberError.getText());
    }

    public void specialCharIssueNumberFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your issue number should not include any symbols or spaces",
                InvalidIssueNumberFieldError.getText());
    }

    public void shortPostcodeErrorDisplayed() {
        Assert.assertEquals(
                "Your postcode should be between 5 and 7 characters",
                InvalidPostcodeError.getText());
    }

    public void shortPostcodeFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your postcode should be between 5 and 7 characters",
                InvalidPostcodeFieldError.getText());
    }

    public void specialCharPostcodeErrorDisplayed() {
        Assert.assertEquals(
                "Your postcode should only include numbers and letters",
                InvalidPostcodeError.getText());
    }

    public void specialCharPostcodeFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your postcode should only include numbers and letters",
                InvalidPostcodeFieldError.getText());
    }

    public void alphaOrNumericPostcodeErrorDisplayed() {
        Assert.assertEquals(
                "Your postcode should include numbers and letters", InvalidPostcodeError.getText());
    }

    public void alphaOrNumericPostcodeFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your postcode should include numbers and letters",
                InvalidPostcodeFieldError.getText());
    }

    public void invalidPostcodeErrorDisplayed() {
        Assert.assertEquals("Enter your postcode", InvalidPostcodeError.getText());
    }

    public void invalidPostcodeFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter your postcode", InvalidPostcodeFieldError.getText());
    }

    public void internationalPostcodeErrorDisplayed() {
        Assert.assertEquals("Enter a UK postcode", InvalidPostcodeError.getText());
    }

    public void internationalPostcodeFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter a UK postcode", InvalidPostcodeFieldError.getText());
    }

    public void invalidLastNameErrorDisplayed() {
        Assert.assertEquals(
                "Enter your last name as it appears on your driving licence",
                InvalidLastNameError.getText());
    }

    public void invalidLastNameFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter your last name as it appears on your driving licence",
                InvalidLastNameFieldError.getText());
    }

    public void invalidFirstNameErrorDisplayed() {
        Assert.assertEquals(
                "Enter your first name as it appears on your driving licence",
                InvalidFirstNameError.getText());
    }

    public void invalidFirstNameFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter your first name as it appears on your driving licence",
                InvalidFirstNameFieldError.getText());
    }

    public void invalidMiddleNamesErrorDisplayed() {
        Assert.assertEquals(
                "Enter any middle names as they appear on your driving licence",
                InvalidMiddleNamesError.getText());
    }

    public void invalidMiddleNamesFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter any middle names as they appear on your driving licence",
                InvalidMiddleNamesFieldError.getText());
    }

    public void ciInVC(String ci) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        JsonNode cisNode = evidence.get(0).get("ci");

        ObjectReader listReader =
                new ObjectMapper().readerFor(new TypeReference<List<String>>() {});
        List<String> cis = listReader.readValue(cisNode);

        if (StringUtils.isNotEmpty(ci)) {
            if (cis.size() > 0) {
                assertTrue(cis.contains(ci));
            } else {
                fail("No CIs found");
            }
        }
    }

    public void documentNumberInVC(String documentNumber) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = " + result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode evidenceNode = vcNode.get("drivingPermit");

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        String licenceNumber = evidence.get(0).get("documentNumber").asText();
        assertEquals(documentNumber, licenceNumber);
    }

    public void navigateToDrivingLicenceCRI() {
        goToDLCRIButton.click();
    }

    public void deleteCookie() {

        Set<Cookie> cookies = Driver.get().manage().getCookies();

        for (Cookie cookie : cookies) {
            LOGGER.info(cookie.getName() + ":" + cookie.getValue());
        }
        Driver.get().manage().deleteCookieNamed("service_session");
        Driver.get().navigate().refresh();
    }

    public void errorPageURLValidation() {
        String actualTitle = Driver.get().getTitle();
        String expTitle = "Sorry, there is a problem – – GOV.UK";
        if (expTitle.equals(actualTitle)) {
            LOGGER.info("Pass : Error page is displayed");
        } else {
            LOGGER.info("Fail : Who was your UK driving licence issued by? is displayed");
        }
        LOGGER.info("title:" + actualTitle);
    }

    public void validateErrorPageHeading() {
        String expectedHeading = "Sorry, there is a problem";
        String actualHeading = errorTitle.getText();
        if (expectedHeading.equals(actualHeading)) {
            LOGGER.info("Pass : Sorry, there is a problem page is displayed");
        } else {
            LOGGER.info("Fail : Who was your UK driving licence issued by? is displayed");
        }
    }

    public void navigateToDrivingLicenceCRIOnTestEnv() {
        visitCredentialIssuers.click();
        String dlCRITestEnvironment = configurationService.getDlCRITestEnvironment();
        LOGGER.info("dlCRITestEnvironment = " + dlCRITestEnvironment);
        if (dlCRITestEnvironment.equalsIgnoreCase("Build")) {
            drivingLicenceCRIBuild.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Staging")) {
            drivingLicenceCRIStaging.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Integration")) {
            drivingLicenceCRIIntegration.click();
        } else {
            LOGGER.info("No test environment is set");
        }
    }

    public void pageTabTitleInEnglish() {
        String actualTitle = Driver.get().getTitle();
        String expTitle =
                "Enter your details exactly as they appear on your UK driving licence – – GOV.UK";
        Assert.assertEquals(expTitle, actualTitle);
        LOGGER.info(actualTitle);
    }

    public void errorPageTitleInEnglish() {
        String actualTitle = Driver.get().getTitle();
        String expTitle =
                "Error: Enter your details exactly as they appear on your UK driving licence – – GOV.UK";
        Assert.assertEquals(expTitle, actualTitle);
        LOGGER.info(actualTitle);
    }
}
