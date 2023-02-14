package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.Driver;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.logging.Logger;

public class WelshLangDrivingLicencePageObject extends DrivingLicencePageObject {

    private static final Logger LOGGER =
            Logger.getLogger(WelshLangDrivingLicencePageObject.class.getName());

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Build\"]")
    public WebElement drivingLicenceCDRIBuild;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Staging\"]")
    public WebElement drivingLicenceCDRIStaging;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Integration\"]")
    public WebElement drivingLicenceCDRIIntegration;

    @FindBy(xpath = "//*[@id=\"licenceIssuerRadio-fieldset\"]/div/div[3]")
    public WebElement orOption;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div")
    public WebElement proveIdentityTextWelsh;

    @FindBy(id = "header")
    public WebElement checkYourDetailTextWelsh;

    @FindBy(xpath = "//*[@id=\"govuk-notification-banner-title\"]")
    public WebElement errorTextWelsh;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]/h2")
    public WebElement weWereUnableToFind;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]/p[1]")
    public WebElement thereWasAProblemDVLA;

    @FindBy(id = "surname-label")
    public WebElement lastNameWelsh;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/div[2]/fieldset/legend")
    public WebElement givenName;

    @FindBy(id = "firstName-label")
    public WebElement firstNameinWelsh;

    @FindBy(id = "middleNames-label")
    public WebElement middleNameWelsh;

    @FindBy(id = "firstName-hint")
    public WebElement firstNameSentenceWelsh;

    @FindBy(id = "middleNames-hint")
    public WebElement middleNameSentenceWelsh;

    @FindBy(xpath = "//*[@id=\"dateOfBirth-fieldset\"]/legend")
    public WebElement dateFieldWelsh;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-fieldset\"]/legend")
    public WebElement dateFieldDVAWelsh;

    @FindBy(id = "dateOfBirth-hint")
    public WebElement dateOfBirthExWelssh;

    @FindBy(id = "dvaDateOfBirth-hint")
    public WebElement dateOfBirthExDVAWelsh;

    @FindBy(xpath = " //*[@id=\"dateOfBirth\"]/div[1]/div/label")
    public WebElement dayfieldWelsh;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[1]/div/label")
    public WebElement dayfieldDVAWelsh;

    @FindBy(xpath = "//*[@id=\"dateOfBirth\"]/div[2]/div/label")
    public WebElement monthfieldWelsh;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[2]/div/label")
    public WebElement monthfieldDVAWelsh;

    @FindBy(xpath = "//*[@id=\"dateOfBirth\"]/div[3]/div/label")
    public WebElement yearfieldWelsh;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth\"]/div[3]/div/label")
    public WebElement yearfieldWelshDVA;

    @FindBy(xpath = "//*[@id=\"issueDate-fieldset\"]/legend")
    public WebElement issuefieldWelsh;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-fieldset\"]/legend")
    public WebElement issuefieldDVAWelsh;

    @FindBy(id = "issueDate-hint")
    public WebElement issuefieldExample;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-hint\"]")
    public WebElement issuefieldExampleDVA;

    @FindBy(xpath = "expiryDate-hint")
    public WebElement validTofieldExample;

    @FindBy(xpath = "//*[@id=\"expiryDate-hint\"]")
    public WebElement validTofieldExampleDVA;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/p")
    public WebElement frontpageText;

    @FindBy(xpath = "//*[@id=\"expiryDate-fieldset\"]/legend")
    public WebElement ValidToDatefieldWelsh;

    @FindBy(id = "drivingLicenceNumber-label")
    public WebElement licenceNumberField;

    @FindBy(id = "drivingLicenceNumber-hint")
    public WebElement licenceExample;

    @FindBy(id = "dvaLicenceNumber-label")
    public WebElement licenceNumberFieldDVA;

    @FindBy(id = "issueNumber-label")
    public WebElement issueNumberField;

    @FindBy(id = "issueNumber-hint")
    public WebElement issueeNumberExampleField;

    @FindBy(id = "postcode-label")
    public WebElement postCodeField;

    @FindBy(id = "postcode-hint")
    public WebElement postCodeExampleField;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[1]/a")
    public WebElement lastNameErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[2]/a")
    public WebElement firstNameErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[3]/a")
    public WebElement middleNameErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[3]/a")
    public WebElement enterDateErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[4]/a")
    public WebElement issueDateErrorMsg;

    @FindBy(id = "dvaDateOfBirth-error")
    public WebElement enterDateErrorDVAMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[4]/a")
    public WebElement pastIssueDateErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[5]/a")
    public WebElement expiryDateErrorMsg;

    @FindBy(xpath = " //*[@id=\"main-content\"]/div[1]/div/ul/li[6]/a")
    public WebElement exactLicenceErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[6]/a")
    public WebElement exactLicenceErrorDVAMsg;

    @FindBy(id = "dvaLicenceNumber-hint")
    public WebElement dvaLicenceHint;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[7]/a")
    public WebElement IssueNumberErrorMsg;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[8]/a")
    public WebElement postcodeErrorMgs;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[7]/a")
    public WebElement postcodeErrorMgsDVA;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]/p[1]")
    public WebElement checkBannerDVAMsg;

    @FindBy(xpath = "//*[@id=\"header\"]")
    public WebElement dvlaPageHeadingWelsh;

    @FindBy(xpath = "/html/body/div[2]/div/p/strong")
    public WebElement betaBannerWelsh;

    @FindBy(className = "govuk-phase-banner__text")
    public WebElement betaBannerWelshSentence;

    @FindBy(xpath = "//*[@id=\"error-summary-title\"]")
    public WebElement thereIsaProblemText;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[2]/a")
    public WebElement invlidDLErrorText;

    @FindBy(id = "licenceIssuerRadio")
    public WebElement radioBtnDVLA;

    @FindBy(id = "licenceIssuerRadio-DVA")
    public WebElement radioBtnDVA;

    @FindBy(id = "licenceIssuerRadio-noLicence-label")
    public WebElement noDLOption;

    @FindBy(id = "licenceIssuerRadio-noLicence")
    public WebElement noDLRadioBtn;

    @FindBy(id = "submitButton")
    public WebElement CTButton;

    public WelshLangDrivingLicencePageObject() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void navigateToDrivingLicenceCRI(String environment) {
        visitCredentialIssuers.click();
        assertURLContains("credential-issuers");
        switch (environment) {
            case "Build":
                drivingLicenceCDRIBuild.click();
                break;

            case "Staging":
                drivingLicenceCDRIStaging.click();
                break;

            case "Integration":
                drivingLicenceCDRIIntegration.click();
                break;

            default:
                break;
        }
    }

    public void betaBanner() {
        betaBannerWelsh.isDisplayed();
    }

    public void betaBannerSentenceWelsh(String expectedText) {
        Assert.assertEquals(expectedText, betaBannerWelshSentence.getText());
    }

    public void changeLanguageToWelsh() {
        String currentURL = Driver.get().getCurrentUrl();
        String newURL = currentURL + "/?lang=cy";
        Driver.get().get(newURL);
    }

    public void drivingLicencePageURLValidationWelsh() {
        String expectedUrl = "https://review-d.build.account.gov.uk/licence-issuer/?lang=cy";
        String actualUrl = Driver.get().getCurrentUrl();
        Assert.assertEquals(expectedUrl, actualUrl);
    }

    public void validateDLPageTitleWelsh(String expectedTitle) {
        String actualTitle = Driver.get().getTitle();
        Assert.assertEquals(expectedTitle, actualTitle);
    }

    public void pageTitleDVLAValidationWelsh() {
        String actualTitle = Driver.get().getTitle();
        String expTitle =
                "Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – – GOV.UK";
        Assert.assertEquals(expTitle, actualTitle);
        LOGGER.info(actualTitle);
    }

    public void ErrorPageTitleDVLA() {
        String actualTitle = Driver.get().getTitle();
        String expTitle =
                "Gwall: Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru – – GOV.UK";
        Assert.assertEquals(expTitle, actualTitle);
        LOGGER.info(actualTitle);
    }

    public void dvlaPageHeading(String expectedText) {
        Assert.assertEquals(expectedText, dvlaPageHeadingWelsh.getText());
    }

    public void dvlaProveYourIdentitySentence(String expectedText) {
        Assert.assertEquals(expectedText, proveIdentityTextWelsh.getText());
    }

    public void checkYourDetailsSentence(String expectedText) {
        Assert.assertEquals(expectedText, checkYourDetailTextWelsh.getText());
    }

    public void errorWord(String expectedText) {
        Assert.assertEquals(expectedText, errorTextWelsh.getText());
    }

    public void weCouldNotFindDetailsSentence(String expectedText) {
        Assert.assertEquals(expectedText, weWereUnableToFind.getText());
    }

    public void youWillBeAbleToFindSentence(String expectedText) {
        Assert.assertEquals(expectedText, thereWasAProblemDVLA.getText());
    }

    public void youWillBeAbleToFindSentenceDVA(String expectedText) {
        Assert.assertEquals(expectedText, checkBannerDVAMsg.getText());
    }

    public void thereIsaProblemSentence(String expectedText) {
        if (Driver.get()
                .getPageSource()
                .contains(
                        "Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad")) {
            LOGGER.info(
                    "Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad");
        }
    }

    public void lastNameWelsh(String expectedText) {
        Assert.assertEquals(expectedText, lastNameWelsh.getText());
    }

    public void givenNameWelsh(String expectedText) {
        Assert.assertEquals(expectedText, givenName.getText());
    }

    public void firstNameWelsh(String expectedText) {
        Assert.assertEquals(expectedText, firstNameinWelsh.getText());
    }

    public void middleNameWelsh(String expectedText) {
        Assert.assertEquals(expectedText, middleNameWelsh.getText());
    }

    public void firstNameSentence(String expectedText) {
        Assert.assertEquals(expectedText, firstNameSentenceWelsh.getText());
    }

    public void middleNameSentence(String expectedText) {
        Assert.assertEquals(expectedText, middleNameSentenceWelsh.getText());
    }

    public void dateOfBirthField(String expectedText) {
        Assert.assertEquals(expectedText, dateFieldWelsh.getText());
    }

    public void dateOfBirthFieldDVA(String expectedText) {
        Assert.assertEquals(expectedText, dateFieldDVAWelsh.getText());
    }

    public void dateOfBirthFieldhint(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthExWelssh.getText());
    }

    public void dateOfBirthFieldHintDVA(String expectedText) {
        Assert.assertEquals(expectedText, dateOfBirthExDVAWelsh.getText());
    }

    public void dateField(String expectedText) {
        Assert.assertEquals(expectedText, dayfieldWelsh.getText());
    }

    public void dateFieldDVA(String expectedText) {
        Assert.assertEquals(expectedText, dayfieldDVAWelsh.getText());
    }

    public void monthField(String expectedText) {
        Assert.assertEquals(expectedText, monthfieldWelsh.getText());
    }

    public void monthFieldDVA(String expectedText) {
        Assert.assertEquals(expectedText, monthfieldDVAWelsh.getText());
    }

    public void yearField(String expectedText) {
        Assert.assertEquals(expectedText, yearfieldWelsh.getText());
    }

    public void yearFieldDVA(String expectedText) {
        Assert.assertEquals(expectedText, yearfieldWelshDVA.getText());
    }

    public void issueDateField(String expectedText) {
        Assert.assertEquals(expectedText, issuefieldWelsh.getText());
    }

    public void issueDateFieldDVA(String expectedText) {
        Assert.assertEquals(expectedText, issuefieldDVAWelsh.getText());
    }

    public void issueDateSentence(String expectedText) {
        Assert.assertEquals(expectedText, issuefieldExample.getText());
    }

    public void issueDateSentenceDVA(String expectedText) {
        Assert.assertEquals(expectedText, issuefieldExampleDVA.getText());
    }

    public void validityDateSentence(String expectedText) {
        Assert.assertEquals(expectedText, validTofieldExample.getText());
    }

    public void validityDateSentenceforDVA(String expectedText) {
        Assert.assertEquals(expectedText, validTofieldExampleDVA.getText());
    }

    public void licenceNumberWelsh(String expectedText) {
        Assert.assertEquals(expectedText, licenceNumberField.getText());
    }

    public void licenceNumberSentence(String expectedText) {
        Assert.assertEquals(expectedText, licenceExample.getText());
    }

    public void licenceNumberWelshDVA(String expectedText) {
        Assert.assertEquals(expectedText, licenceNumberFieldDVA.getText());
    }

    public void issueNumberWelsh(String expectedText) {
        Assert.assertEquals(expectedText, issueNumberField.getText());
    }

    public void issueNumberSentence(String expectedText) {
        Assert.assertEquals(expectedText, issueeNumberExampleField.getText());
    }

    public void noDrivingLicenceBtnWelsh(String expectedText) {
        Assert.assertEquals(expectedText, noDLOption.getText());
        noDLRadioBtn.isDisplayed();
    }

    public void orDisplayWelsh() {
        orOption.isDisplayed();
    }

    public void postcodeWelsh(String expectedText) {
        Assert.assertEquals(expectedText, postCodeField.getText());
    }

    public void postcodeSentence(String expectedText) {
        Assert.assertEquals(expectedText, postCodeExampleField.getText());
    }

    public void continueButtonWelsh() {
        CTButton.isDisplayed();
        CTButton.isEnabled();
    }

    public void licenceSelectionSentence(String expectedText) {
        Assert.assertEquals(expectedText, frontpageText.getText());
    }

    public void validToDateFieldTitle(String expectedText) {
        Assert.assertEquals(expectedText, ValidToDatefieldWelsh.getText());
    }

    public void invalidlastAndFirstNameWelsh() {
        new DrivingLicencePageObject().LastName.sendKeys("Parker!");
        new DrivingLicencePageObject().FirstName.sendKeys("Peter@@!");
        new DrivingLicencePageObject().MiddleNames.sendKeys("@@@@@@@");
    }

    public void clearDOBandReEnterWelsh() {
        new DrivingLicencePageObject().birthDay.clear();
        new DrivingLicencePageObject().birthDay.click();
        new DrivingLicencePageObject().birthDay.sendKeys("15");
        new DrivingLicencePageObject().birthMonth.clear();
        new DrivingLicencePageObject().birthMonth.click();
        new DrivingLicencePageObject().birthMonth.sendKeys("04");
        new DrivingLicencePageObject().birthYear.clear();
        new DrivingLicencePageObject().birthYear.click();
        new DrivingLicencePageObject().birthYear.sendKeys("1968");
    }

    public void clearDOBandReEnterWelshtofuture() {
        new DrivingLicencePageObject().birthDay.clear();
        new DrivingLicencePageObject().birthDay.click();
        new DrivingLicencePageObject().birthDay.sendKeys("15");
        new DrivingLicencePageObject().birthMonth.clear();
        new DrivingLicencePageObject().birthMonth.click();
        new DrivingLicencePageObject().birthMonth.sendKeys("04");
        new DrivingLicencePageObject().birthYear.clear();
        new DrivingLicencePageObject().birthYear.click();
        new DrivingLicencePageObject().birthYear.sendKeys("2028");
    }

    public void clearDOBandReEnterWelshForDVA() {
        new DrivingLicencePageObject().birthDay.clear();
        new DrivingLicencePageObject().birthDay.click();
        new DrivingLicencePageObject().birthDay.sendKeys("");
        new DrivingLicencePageObject().birthMonth.clear();
        new DrivingLicencePageObject().birthMonth.click();
        new DrivingLicencePageObject().birthMonth.sendKeys("");
        new DrivingLicencePageObject().birthYear.clear();
        new DrivingLicencePageObject().birthYear.click();
        new DrivingLicencePageObject().birthYear.sendKeys("");
    }

    public void thereIsaProblemText(String expectedText) {
        Assert.assertEquals(expectedText, thereIsaProblemText.getText());
    }

    public void weWillCheckYourDetails(String expectedText) {
        if (Driver.get()
                .getPageSource()
                .contains(
                        "Byddwn yn gwirio eich manylion gyda'r DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.")) {
            LOGGER.info(
                    "Byddwn yn gwirio eich manylion gyda'r DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.");
        }
    }

    public void lastNameErrorSentenceWelsh(String expectedText) {
        Assert.assertEquals(expectedText, lastNameErrorMsg.getText());
    }

    public void firstNameErrorSentenceWelsh(String expectedText) {
        Assert.assertEquals(expectedText, firstNameErrorMsg.getText());
    }

    public void middleNameErrorSentence(String expectedText) {
        Assert.assertEquals(expectedText, middleNameErrorMsg.getText());
    }

    public void enterDOBErrorTextWelsh(String expectedText) {
        Assert.assertEquals(expectedText, issueDateErrorMsg.getText());
    }

    public void enterDOBErrorTextWelshDVA(String expectedText) {
        Assert.assertEquals(expectedText, enterDateErrorDVAMsg.getText());
    }

    public void enterValidDOBErrorTextWelsh(String expectedText) {
        Assert.assertEquals(expectedText, enterDateErrorMsg.getText());
    }

    public void errorMessageFutureDOBWelsh(String expectedText) {
        Assert.assertEquals(expectedText, enterDateErrorMsg.getText());
    }

    public void enterInValidIssueDate() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceIssueDay.click();
        drivingLicencePageObject.LicenceIssueDay.sendKeys("");
        drivingLicencePageObject.LicenceIssueMonth.click();
        drivingLicencePageObject.LicenceIssueMonth.sendKeys("");
        drivingLicencePageObject.LicenceIssueYear.click();
        drivingLicencePageObject.LicenceIssueYear.sendKeys("");
    }

    public void enterInValidIssueDateWithFutureYear() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceIssueDay.clear();
        drivingLicencePageObject.LicenceIssueDay.click();
        drivingLicencePageObject.LicenceIssueDay.sendKeys("23");
        drivingLicencePageObject.LicenceIssueMonth.clear();
        drivingLicencePageObject.LicenceIssueMonth.click();
        drivingLicencePageObject.LicenceIssueMonth.sendKeys("03");
        drivingLicencePageObject.LicenceIssueYear.clear();
        drivingLicencePageObject.LicenceIssueYear.click();
        drivingLicencePageObject.LicenceIssueYear.sendKeys("2032");
    }

    public void issueDateErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, pastIssueDateErrorMsg.getText());
    }

    public void clickOnDVLARadioButtonWelsh() {
        radioBtnDVLA.click();
        CTButton.click();
    }

    public void clickOnDVARadioButtonWelsh() {
        radioBtnDVA.click();
        CTButton.click();
    }

    public void noDrivingLicenceOptionWelsh() {
        noDLOption.click();
        CTButton.click();
    }

    public void inValidDLText() {
        invlidDLErrorText.isDisplayed();
    }

    public void inValidIssueDateText(String expectedText) {
        Assert.assertEquals(expectedText, pastIssueDateErrorMsg.getText());
    }

    public void inValidIssueDateTextDVA(String expectedText) {
        Assert.assertEquals(expectedText, expiryDateErrorMsg.getText());
    }

    public void enterInValidUntilDate() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceValidToDay.click();
        drivingLicencePageObject.LicenceValidToDay.sendKeys("");
        drivingLicencePageObject.LicenceValidToMonth.click();
        drivingLicencePageObject.LicenceValidToMonth.sendKeys("");
        drivingLicencePageObject.LicenceValidToYear.click();
        drivingLicencePageObject.LicenceValidToYear.sendKeys("");
    }

    public void enterTheValidToExpiredYear() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceValidToDay.clear();
        drivingLicencePageObject.LicenceValidToDay.click();
        drivingLicencePageObject.LicenceValidToDay.sendKeys("12");
        drivingLicencePageObject.LicenceValidToMonth.clear();
        drivingLicencePageObject.LicenceValidToMonth.click();
        drivingLicencePageObject.LicenceValidToMonth.sendKeys("12");
        drivingLicencePageObject.LicenceValidToYear.clear();
        drivingLicencePageObject.LicenceValidToYear.click();
        drivingLicencePageObject.LicenceValidToYear.sendKeys("2012");
    }

    public void validToErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, expiryDateErrorMsg.getText());
    }

    public void invalidDrivingLicenceempty() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceNumber.click();
        drivingLicencePageObject.LicenceNumber.sendKeys("");
    }

    public void invalidDrivingLicenceDVLA() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceNumber.click();
        drivingLicencePageObject.LicenceNumber.sendKeys("PARKE610@$112");
    }

    public void invalidDrivingLicenceWithSplCharDVLA() {
        new DrivingLicencePageObject().LicenceNumber.clear();
        new DrivingLicencePageObject().LicenceNumber.click();
        new DrivingLicencePageObject().LicenceNumber.sendKeys("@@@@@@@@@@@@@@@@");
    }

    public void inValidDrivingLicenceDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.clear();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.click();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.sendKeys("1acd1113756456");
    }

    public void invalidDrivingLicenceWithlessCharDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.click();
        dvaEnterYourDetailsExactlyPage.dvaLicenceNumber.sendKeys("111106");
    }

    public void licenceErrorWelshforExactonDL(String expectedText) {
        Assert.assertEquals(expectedText, exactLicenceErrorMsg.getText());
    }

    public void licenceNumberErrorWelshForDVLA(String expectedText) {
        Assert.assertEquals(expectedText, exactLicenceErrorMsg.getText());
    }

    public void licenceErrorWelshforSplChar(String expectedText) {
        Assert.assertEquals(expectedText, exactLicenceErrorMsg.getText());
    }

    public void invalidIssueNumber() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.IssueNumber.click();
        drivingLicencePageObject.IssueNumber.sendKeys("7");
    }

    public void IssueNumberErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, IssueNumberErrorMsg.getText());
    }

    public void clearIssueNumber() {
        new DrivingLicencePageObject().IssueNumber.clear();
    }

    public void enterIssueNumberErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, IssueNumberErrorMsg.getText());
    }

    public void enterInValidPostCode() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.Postcode.clear();
        drivingLicencePageObject.Postcode.click();
        drivingLicencePageObject.Postcode.sendKeys("@@@$$$**");
    }

    public void invalidPostCode() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.Postcode.clear();
        drivingLicencePageObject.Postcode.click();
        drivingLicencePageObject.Postcode.sendKeys("BS98");
    }

    public void enterYourPostCodeErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgs.getText());
    }

    public void postCodeErrorInvalidWelsh(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgs.getText());
    }

    public void postCodeErrorWelsh(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgs.getText());
    }

    public void postCodeErrorWelshDVA(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgsDVA.getText());
    }

    public void postCodeErrorInvalidWelshDVA(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgsDVA.getText());
    }

    public void enterYourPostCodeErrorWelshDVA(String expectedText) {
        Assert.assertEquals(expectedText, postcodeErrorMgsDVA.getText());
    }

    public void invalidDobForDVAWelsh() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.click();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.sendKeys("ss");
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.click();
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.sendKeys("aa");
        dvaEnterYourDetailsExactlyPage.YearOfBirth.click();
        dvaEnterYourDetailsExactlyPage.YearOfBirth.sendKeys("aaaa");
    }

    public void dvaclearDOBandReEnterWelshtofuture() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.click();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.sendKeys("15");
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.click();
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.sendKeys("04");
        dvaEnterYourDetailsExactlyPage.YearOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.YearOfBirth.click();
        dvaEnterYourDetailsExactlyPage.YearOfBirth.sendKeys("1968");
    }

    public void dvaPastErrorrWelsh() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.click();
        dvaEnterYourDetailsExactlyPage.DayOfBirth.sendKeys("11");
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.click();
        dvaEnterYourDetailsExactlyPage.MonthOfBirth.sendKeys("10");
        dvaEnterYourDetailsExactlyPage.YearOfBirth.clear();
        dvaEnterYourDetailsExactlyPage.YearOfBirth.click();
        dvaEnterYourDetailsExactlyPage.YearOfBirth.sendKeys("2062");
    }

    public void invalidIssueDayForDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.LicenceIssueDay.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueDay.sendKeys("");
        dvaEnterYourDetailsExactlyPage.LicenceIssueMonth.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueMonth.sendKeys("");
        dvaEnterYourDetailsExactlyPage.LicenceIssueYear.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueYear.sendKeys("");
    }

    public void enterInValidIssueDateWithFutureYearDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.LicenceIssueDay.clear();
        dvaEnterYourDetailsExactlyPage.LicenceIssueDay.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueDay.sendKeys("23");
        dvaEnterYourDetailsExactlyPage.LicenceIssueMonth.clear();
        dvaEnterYourDetailsExactlyPage.LicenceIssueMonth.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueMonth.sendKeys("03");
        dvaEnterYourDetailsExactlyPage.LicenceIssueYear.clear();
        dvaEnterYourDetailsExactlyPage.LicenceIssueYear.click();
        dvaEnterYourDetailsExactlyPage.LicenceIssueYear.sendKeys("2062");
    }

    public void invalidValidUntilForDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.sendKeys("");
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.sendKeys("");
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.sendKeys("");
    }

    public void enterTheValidToExpiredYearForDVA() {
        DVAEnterYourDetailsExactlyPage dvaEnterYourDetailsExactlyPage =
                new DVAEnterYourDetailsExactlyPage();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToDay.sendKeys("23");
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToMonth.sendKeys("03");
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.clear();
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.click();
        dvaEnterYourDetailsExactlyPage.LicenceValidToYear.sendKeys("2005");
    }

    public void licenceNumberErrorWelshForDVA(String expectedText) {
        Assert.assertEquals(expectedText, exactLicenceErrorDVAMsg.getText());
    }

    public void licenceNumberErrorWelshforDVA(String expectedText) {
        Assert.assertEquals(expectedText, dvaLicenceHint.getText());
    }
}
