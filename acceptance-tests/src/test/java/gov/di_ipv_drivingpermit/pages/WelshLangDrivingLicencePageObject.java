package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.Driver;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.logging.Logger;

public class WelshLangDrivingLicencePageObject extends UniversalSteps {

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

    // @FindBy(className = "govuk-warning-text__text")
    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/div[9]/strong/text()")
    public WebElement noretrymessage;

    @FindBy(id = "surname-label")
    public WebElement lastNameWelsh;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/div[2]/fieldset/legend")
    public WebElement givenName;

    @FindBy(id = "firstName-label")
    public WebElement firstNameWelsh;

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

    @FindBy(id = "dateOfIssue-hint")
    public WebElement issuefieldExampleDVA;

    @FindBy(id = "expiryDate-hint")
    public WebElement validTofieldExample;

    @FindBy(xpath = "//*[@id=\"expiryDate-fieldset\"]/legend")
    public WebElement ValidToDatefieldWelsh;

    @FindBy(id = "drivingLicenceNumber-label")
    public WebElement licenceNumberField;

    @FindBy(id = "dvaLicenceNumber-label")
    public WebElement licenceNumberFieldDVA;

    @FindBy(id = "drivingLicenceNumber-hint")
    public WebElement licenceNumberExampleField;

    @FindBy(id = "issueNumber-label")
    public WebElement issueNumberField;

    @FindBy(id = "issueNumber-hint")
    public WebElement issueeNumberExampleField;

    @FindBy(id = "postcode-label")
    public WebElement postCodeField;

    @FindBy(id = "postcode-hint")
    public WebElement postCodeExampleField;

    @FindBy(id = "surname-error")
    public WebElement lastNameErrorMsg;

    @FindBy(id = "firstName-error")
    public WebElement firstNameErrorMsg;

    @FindBy(id = "middleNames-error")
    public WebElement middleNameErrorMsg;

    @FindBy(id = "dateOfBirth-error")
    public WebElement enterDateErrorMsg;

    @FindBy(id = "issueDate-error")
    public WebElement issueDateErrorMsg;

    @FindBy(id = "dvaDateOfBirth-error")
    public WebElement enterDateErrorDVAMsg;

    @FindBy(id = "issueDate-error")
    public WebElement pastIssueDateErrorMsg;

    @FindBy(id = "dateOfIssue-error")
    public WebElement pastIssueDateErrorDVAMsg;

    // @FindBy(id = "dateOfIssue-error")
    // public WebElement  IssueDateErrorDVAMsg;

    @FindBy(id = "expiryDate-error")
    public WebElement expiryDateErrorMsg;

    @FindBy(id = "drivingLicenceNumber-error")
    public WebElement exactLicenceErrorMsg;

    @FindBy(id = "dvaLicenceNumber-error")
    public WebElement exactLicenceErrorDVAMsg;

    @FindBy(id = "dvaLicenceNumber-hint")
    public WebElement dvaLicenceHint;

    @FindBy(id = "issueNumber-error")
    public WebElement IssueNumberErrorMsg;

    @FindBy(id = "postcode-error")
    public WebElement postcodeErrorMgs;

    @FindBy(className = "govuk-grid-column-two-thirds")
    public WebElement checkDetailsDVAMsg;

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

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[1]/div/ul/li[1]/a")
    public WebElement invlidDOBErrorText;

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

    public void betaBannerSentenceWelsh() {
        betaBannerWelshSentence.isDisplayed();
    }

    public void changeLanguageToWelsh() {
        String currentURL = Driver.get().getCurrentUrl();
        String newURL = currentURL + "/?lang=cy";
        Driver.get().get(newURL);
    }

    public void drivingLicencePageURLValidationWelsh() {
        String expectedUrl = "https://review-d.build.account.gov.uk/licence-issuer/?lang=cy";
        String actualUrl = Driver.get().getCurrentUrl();
        LOGGER.info("expectedUrl = " + expectedUrl);
        LOGGER.info("actualUrl = " + actualUrl);
        Assert.assertEquals(expectedUrl, actualUrl);
    }

    public void validateDLPageTitleWelsh() {
        String actualTitle = Driver.get().getTitle();
        System.out.print("Actual Title:" + actualTitle);
        String expTitle = "Pwy wnaeth gyhoeddi eich trwydded yrru y DU? – – GOV.UK";
        if (actualTitle.contains(expTitle)) {
            LOGGER.info("Pass : directed to Pwy wnaeth gyhoeddi eich trwydded yrru y DU?");
        } else {
            LOGGER.info("Fail : not directed to the Driving Licence Page");
        }
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

    public void dvlaPageHeading() {
        String expectedText =
                "Rhowch eich manylion yn union fel maent yn ymddangos ar eich trwydded yrru";
        String actualText = dvlaPageHeadingWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void dvlaProveYourIdentitySentence() {
        String expectedText =
                "Os nad oes gennych drwydded yrru y DU neu os na allwch gofio'ch manylion, gallwch brofi pwy ydych chi mewn ffordd arall yn lle.";
        String actualText = proveIdentityTextWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void checkYourDetailsSentence() {
        String expectedText =
                "Gwiriwch bod eich manylion yn paru gyda beth sydd ar eich trwydded yrru y DU";
        String actualText = checkYourDetailTextWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void erroeWord() {
        String expectedText = "Gwall";
        String actualText = errorTextWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        if (Driver.get().getPageSource().contains("Gwall")) {
            LOGGER.info(actualText);
        }
    }

    public void weCouldNotFindDetailsSentence() {
        String expectedText = "Nid oeddem yn gallu dod o hyd i'ch manylion";
        String actualText = weWereUnableToFind.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void youWillBeAbleToFindSentence() {
        String expectedText = "Roedd yna broblem wrth i ni wirio eich manylion gyda'r DVLA.";
        String actualText = thereWasAProblemDVLA.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void youWillBeAbleToFindSentenceDVA() {
        String expectedText = "Roedd yna broblem wrth i ni wirio eich manylion gyda'r DVA.";
        String actualText = checkBannerDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void thereIsaProblemSentence() {
        if (Driver.get()
                .getPageSource()
                .contains(
                        "Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad")) {
            LOGGER.info(
                    "Ni fyddwch yn gallu newid eich manylion eto os byddwch yn gwneud camgymeriad");
        }
    }

    public void lastNameWelsh() {
        String expectedText = "Enw olaf";
        String actualText = lastNameWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void givenNameWelsh() {
        String expectedText = "Enwau a roddwyd";
        String actualText = givenName.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void firstNameWelsh() {
        String expectedText = "Enw cyntaf";
        String actualText = firstNameWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void middleNameWelsh() {
        String expectedText = "Enwau canol";
        String actualText = middleNameWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void firstNameSentence() {
        String expectedText =
                "Mae hwn yn adran 2 o'ch trwydded. Nid oes angen i chi gynnwys eich teitl.";
        String actualText = firstNameSentenceWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void middleNameSentence() {
        String expectedText = "Gadewch hyn yn wag os nad oes gennych unrhyw enwau canol";
        String actualText = middleNameSentenceWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void DateOfBirthField() {
        String expectedText = "Dyddiad geni";
        String actualText = dateFieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);

        // Assert.assertEquals("Dyddiad geni",dateFieldWelsh.getText());
    }

    public void DateOfBirthFieldDVA() {
        String expectedText = "Dyddiad geni";
        String actualText = dateFieldDVAWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void DateOfBirthFieldhint() {
        String expectedText = "Er enghraifft, 5 9 1973";
        String actualText = dateOfBirthExWelssh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);

        // Assert.assertEquals("Dyddiad geni",dateFieldWelsh.getText());
    }

    public void DateOfBirthFieldHintDVA() {
        String expectedText = "Er enghraifft, 5 9 1973";
        String actualText = dateOfBirthExDVAWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void dateField() {
        String expectedText = "Diwrnod";
        String actualText = dayfieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void dateFieldDVA() {
        String expectedText = "Diwrnod";
        String actualText = dayfieldDVAWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void monthField() {
        String expectedText = "Mis";
        String actualText = monthfieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void monthFieldDVA() {
        String expectedText = "Mis";
        String actualText = monthfieldDVAWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void yearField() {
        String expectedText = "Blwyddyn";
        String actualText = yearfieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void yearFieldDVA() {
        String expectedText = "Blwyddyn";
        String actualText = yearfieldWelshDVA.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueDateField() {
        String expectedText = "Dyddiad cyhoeddi";
        String actualText = issuefieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueDateFieldDVA() {
        String expectedText = "Dyddiad cyhoeddi";
        String actualText = issuefieldDVAWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueDateSentence() {
        String expectedText = "Dyma'r dyddiad yn adran 4a o'ch trwydded, er enghraifft 27 5 2019";
        String actualText = issuefieldExample.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueDateSentenceDVA() {
        String expectedText = "Dyma'r dyddiad yn adran 4a o'ch trwydded, er enghraifft 27 5 2019";
        String actualText = issuefieldExampleDVA.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void validityDateSentence() {
        String expectedText = "Dyma'r dyddiad yn adran 4b o'ch trwydded, er enghraifft 27 5 2019";
        String actualText = validTofieldExample.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceNumberWelsh() {
        String expectedText = "Rhif trwydded";
        String actualText = licenceNumberField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceNumberWelshDVA() {
        String expectedText = "Rhif trwydded";
        String actualText = licenceNumberFieldDVA.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceSentence() {
        String expectedText =
                "Dyma'r rhif hir yn adran 5 ar eich trwydded er enghraifft HARRI559146MJ931";
        String actualText = licenceNumberExampleField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueNumberWelsh() {
        String expectedText = "Rhif cyhoeddi";
        String actualText = issueNumberField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueNumberSentence() {
        String expectedText = "Dyma'r rhif 2 ddigid ar ôl y gofod yn adran 5 o'ch trwydded";
        String actualText = issueeNumberExampleField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void noDrivingLicenceBtnWelsh() {
        noDLOption.isDisplayed();
        noDLRadioBtn.isDisplayed();
    }

    public void orDisplayWelsh() {
        orOption.isDisplayed();
    }

    public void postcodeWelsh() {
        String expectedText = "Cod post";
        String actualText = postCodeField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void postcodeSentence() {
        String expectedText = "Rhowch y cod post yn y cyfeiriad yn adran 8 o'ch trwydded";
        String actualText = postCodeExampleField.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void continueButtonWelsh() {
        CTButton.isDisplayed();
        CTButton.isEnabled();
    }

    public void licenceSelectionSentence() {
        if (Driver.get()
                .getPageSource()
                .contains(
                        "Gallwch ddod o hyd i hwn yn adran 4c o'ch trwydded yrru. Bydd naill ai'n dweud DVLA (Asiantaeth Trwyddedu Gyrru a Cherbydau) neu DVA (Asiantaeth Gyrrwyr a Cherbydau).")) {
            LOGGER.info(
                    "lastname is \"Gallwch ddod o hyd i hwn yn adran 4c o'ch trwydded yrru. Bydd naill ai'n dweud DVLA (Asiantaeth Trwyddedu Gyrru a Cherbydau) neu DVA (Asiantaeth Gyrrwyr a Cherbydau).\" ");
        }
    }

    public void validToDateFieldTitle() {
        String expectedText = "Yn ddilys tan";
        String actualText = ValidToDatefieldWelsh.getText();
        Assert.assertEquals(expectedText, actualText);
        if (Driver.get().getPageSource().contains("Yn ddilys tan")) {
            LOGGER.info(" Issue date as \"Yn ddilys tan\" ");
        }
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

    public void thereIsaProblemText() {
        thereIsaProblemText.isDisplayed();
        if (Driver.get().getPageSource().contains("Mae problem")) {
            LOGGER.info("Mae problem");
        }
    }

    public void weWillCheckYourDetails() {
        if (Driver.get()
                .getPageSource()
                .contains(
                        "Byddwn yn gwirio eich manylion gyda'r DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.")) {
            LOGGER.info(
                    "Byddwn yn gwirio eich manylion gyda'r DVLA i sicrhau nad yw eich trwydded yrru wedi cael ei chanslo na'i hadrodd fel un sydd ar goll neu wedi ei dwyn.");
        }
    }

    public void lastNameErrorSentenceWelsh() {
        String expectedText =
                "Gwall:\n" + "Rhowch eich enw olaf fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = lastNameErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
    }

    public void firstNameErrorSentenceWelsh() {
        String expectedText =
                "Gwall:\n" + "Rhowch eich enw cyntaf fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = firstNameErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
    }

    public void middleNameErrorSentence() {
        String expectedText =
                "Gwall:\n"
                        + "Rhowch unrhyw enwau canol fel y maent yn ymddangos ar eich trwydded yrru";
        String actualText = middleNameErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
    }

    public void enterDOBErrorTextWelsh() {
        String expectedText =
                "Gwall:\n" + "Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = issueDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterDOBErrorTextWelshDVA() {
        String expectedText =
                "Gwall:\n" + "Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = enterDateErrorDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterValidDOBErrorTextWelsh() {
        String expectedText = "Gwall:\n" + "Gwiriwch eich bod wedi rhoi eich dyddiad geni yn gywir";
        String actualText = enterDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void errorMessageFutureDOBWelsh() {
        String expectedText = "Gwall:\n" + "Rhaid i'ch dyddiad geni fod yn y gorffennol";
        String actualText = enterDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void errorMessageFutureDOBWelshDVA() {
        String expectedText = "Gwall:\n" + "Rhaid i'ch dyddiad geni fod yn y gorffennol";
        String actualText = enterDateErrorDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterInValidIssueDate() {
        new DrivingLicencePageObject().LicenceIssueDay.click();
        new DrivingLicencePageObject().LicenceIssueDay.sendKeys("");
        new DrivingLicencePageObject().LicenceIssueMonth.click();
        new DrivingLicencePageObject().LicenceIssueMonth.sendKeys("");
        new DrivingLicencePageObject().LicenceIssueYear.click();
        new DrivingLicencePageObject().LicenceIssueYear.sendKeys("");
    }

    public void enterInValidIssueDateWithFutureYear() {
        new DrivingLicencePageObject().LicenceIssueDay.clear();
        new DrivingLicencePageObject().LicenceIssueDay.click();
        new DrivingLicencePageObject().LicenceIssueDay.sendKeys("23");
        new DrivingLicencePageObject().LicenceIssueMonth.clear();
        new DrivingLicencePageObject().LicenceIssueMonth.click();
        new DrivingLicencePageObject().LicenceIssueMonth.sendKeys("03");
        new DrivingLicencePageObject().LicenceIssueYear.clear();
        new DrivingLicencePageObject().LicenceIssueYear.click();
        new DrivingLicencePageObject().LicenceIssueYear.sendKeys("2032");
    }

    public void issueDateErrorWelsh() {
        String expectedText = "Gwall:\n" + "Rhaid i ddyddiad cyhoeddi fod yn y gorffennol";
        String actualText = pastIssueDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void issueDateErrorDVAWelshh() {
        String expectedText = "Gwall:\n" + "Rhaid i ddyddiad cyhoeddi fod yn y gorffennol";
        String actualText = pastIssueDateErrorDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
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

    public void inValidIssueDateText() {
        String expectedText =
                "Gwall:\n" + "Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = pastIssueDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void inValidIssueDateTextDVA() {
        String expectedText =
                "Gwall:\n" + "Rhowch y dyddiad fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = expiryDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterInValidUntilDate() {
        new DrivingLicencePageObject().LicenceValidToDay.click();
        new DrivingLicencePageObject().LicenceValidToDay.sendKeys("");
        new DrivingLicencePageObject().LicenceValidToMonth.click();
        new DrivingLicencePageObject().LicenceValidToMonth.sendKeys("");
        new DrivingLicencePageObject().LicenceValidToYear.click();
        new DrivingLicencePageObject().LicenceValidToYear.sendKeys("");
    }

    public void enterTheValidToExpiredYear() {
        new DrivingLicencePageObject().LicenceValidToDay.clear();
        new DrivingLicencePageObject().LicenceValidToDay.click();
        new DrivingLicencePageObject().LicenceValidToDay.sendKeys("12");
        new DrivingLicencePageObject().LicenceValidToMonth.clear();
        new DrivingLicencePageObject().LicenceValidToMonth.click();
        new DrivingLicencePageObject().LicenceValidToMonth.sendKeys("12");
        new DrivingLicencePageObject().LicenceValidToYear.clear();
        new DrivingLicencePageObject().LicenceValidToYear.click();
        new DrivingLicencePageObject().LicenceValidToYear.sendKeys("2012");
    }

    public void validToErrorWelsh() {
        String expectedText = "Gwall:\n" + "Ni allwch ddefnyddio trwydded yrru sydd wedi dod i ben";
        String actualText = expiryDateErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void invalidDrivingLicenceempty() {
        new DrivingLicencePageObject().LicenceNumber.click();
        new DrivingLicencePageObject().LicenceNumber.sendKeys("");
    }

    public void invalidDrivingLicenceDVLA() {
        new DrivingLicencePageObject().LicenceNumber.click();
        new DrivingLicencePageObject().LicenceNumber.sendKeys("PARKE610@$112");
    }

    public void invalidDrivingLicenceWithSplCharDVLA() {
        new DrivingLicencePageObject().LicenceNumber.clear();
        new DrivingLicencePageObject().LicenceNumber.click();
        new DrivingLicencePageObject().LicenceNumber.sendKeys("@@@@@@@@@@@@@@@@");
    }

    public void inValidDrivingLicenceDVA() {
        new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.clear();
        new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.click();
        new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.sendKeys("1acd1113756456");
    }

    public void invalidDrivingLicenceWithlessCharDVA() {
        new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.click();
        new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.sendKeys("111106");
    }

    public void licenceErrorWelshforExactonDL() {
        String expectedText =
                "Gwall:\n" + "Rhowch y rhif yn union fel mae’n ymddangos ar eich trwydded yrru";
        String actualText = exactLicenceErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceErrorWelshforSplChar() {
        String expectedText =
                "Gwall:\n" + "Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau";
        String actualText = exactLicenceErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceErrorWelshforSplCharForDVA() {
        String expectedText =
                "Gwall:\n" + "Ni ddylai rhif eich trwydded gynnwys unrhyw symbolau neu ofodau";
        String actualText = exactLicenceErrorDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void invalidIssueNumber() {
        new DrivingLicencePageObject().IssueNumber.click();
        new DrivingLicencePageObject().IssueNumber.sendKeys("7");
    }

    public void IssueNumberErrorWelsh() {
        String expectedText = "Gwall:\n" + "Dylai eich rhif cyhoeddi fod yn 2 rif o hyd";
        String actualText = IssueNumberErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void clearIssueNumber() {
        new DrivingLicencePageObject().IssueNumber.clear();
    }

    public void enterIssueNumberErrorWelsh() {
        String expectedText =
                "Gwall:\n" + "Rhowch y rhif cyhoeddi fel y mae'n ymddangos ar eich trwydded yrru";
        String actualText = IssueNumberErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterInValidPostCode() {
        new DrivingLicencePageObject().Postcode.clear();
        new DrivingLicencePageObject().Postcode.click();
        new DrivingLicencePageObject().Postcode.sendKeys("@@@$$$**");
    }

    public void invalidPostCode() {
        new DrivingLicencePageObject().Postcode.clear();
        new DrivingLicencePageObject().Postcode.click();
        new DrivingLicencePageObject().Postcode.sendKeys("BS98");
    }

    public void postCodeErrorWelsh() {
        String expectedText = "Gwall:\n" + "Dylai eich rhowch eich cod post fod rhwng 5 a 7 nod";
        String actualText = postcodeErrorMgs.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void postCodeErrorInvalidWelsh() {
        String expectedText =
                "Gwall:\n"
                        + "Dylai eich rhowch eich cod post ond cynnwys rhifau a llythrennau yn unig";
        String actualText = postcodeErrorMgs.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void enterYourPostCodeErrorWelsh() {
        String expectedText = "Gwall:\n" + "Rhowch eich cod post";
        String actualText = postcodeErrorMgs.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void invalidDobForDVAWelsh() {
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.sendKeys("ss");
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.sendKeys("aa");
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.sendKeys("aaaa");
    }

    public void dvaclearDOBandReEnterWelshtofuture() {
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.sendKeys("15");
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.sendKeys("04");
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.sendKeys("1968");
    }

    public void dvaPastErrorrWelsh() {
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().DayOfBirth.sendKeys("11");
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().MonthOfBirth.sendKeys("10");
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.clear();
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.click();
        new DVAEnterYourDetailsExactlyPage().YearOfBirth.sendKeys("2062");
    }

    public void invalidIssueDayForDVA() {
        new DVAEnterYourDetailsExactlyPage().LicenceIssueDay.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueDay.sendKeys("");
        new DVAEnterYourDetailsExactlyPage().LicenceIssueMonth.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueMonth.sendKeys("");
        new DVAEnterYourDetailsExactlyPage().LicenceIssueYear.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueYear.sendKeys("");
    }

    public void enterInValidIssueDateWithFutureYearDVA() {
        new DVAEnterYourDetailsExactlyPage().LicenceIssueDay.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueDay.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueDay.sendKeys("23");
        new DVAEnterYourDetailsExactlyPage().LicenceIssueMonth.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueMonth.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueMonth.sendKeys("03");
        new DVAEnterYourDetailsExactlyPage().LicenceIssueYear.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueYear.click();
        new DVAEnterYourDetailsExactlyPage().LicenceIssueYear.sendKeys("2062");
    }

    public void invalidValidUntilForDVA() {
        new DVAEnterYourDetailsExactlyPage().LicenceValidToDay.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToDay.sendKeys("");
        new DVAEnterYourDetailsExactlyPage().LicenceValidToMonth.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToMonth.sendKeys("");
        new DVAEnterYourDetailsExactlyPage().LicenceValidToYear.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToYear.sendKeys("");
    }

    public void enterTheValidToExpiredYearForDVA() {
        new DVAEnterYourDetailsExactlyPage().LicenceValidToDay.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToDay.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToDay.sendKeys("23");
        new DVAEnterYourDetailsExactlyPage().LicenceValidToMonth.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToMonth.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToMonth.sendKeys("03");
        new DVAEnterYourDetailsExactlyPage().LicenceValidToYear.clear();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToYear.click();
        new DVAEnterYourDetailsExactlyPage().LicenceValidToYear.sendKeys("2005");
    }

    public void licenceNumberErrorWelshForDVLA() {
        String expectedText = "Gwall:\n" + "Dylai rhif eich trwydded fod yn 16 nod o hyd";
        String actualText = exactLicenceErrorMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceNumberErrorWelshForDVA() {
        String expectedText = "Gwall:\n" + "Dylai rhif eich trwydded fod yn 8 nod o hyd";
        String actualText = exactLicenceErrorDVAMsg.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }

    public void licenceNumberErrorWelshforDVA() {
        String expectedText = "Dyma'r rhif hir yn adran 5 ar eich trwydded";
        String actualText = dvaLicenceHint.getText();
        Assert.assertEquals(expectedText, actualText);
        LOGGER.info(actualText);
    }
}
