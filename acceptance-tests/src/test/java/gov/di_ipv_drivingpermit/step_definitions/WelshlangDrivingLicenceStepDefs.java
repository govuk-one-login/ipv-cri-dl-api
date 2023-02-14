package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DrivingLicencePageObject;
import gov.di_ipv_drivingpermit.pages.WelshLangDrivingLicencePageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WelshlangDrivingLicenceStepDefs extends WelshLangDrivingLicencePageObject {

    @And("I add a cookie to change the language to Welsh")
    public void iAddACookieToChangeTheLanguageToWelsh() {
        changeLanguageToWelsh();
    }

    @Given("I view the Beta banner")
    public void iViewTheBetaBanner() {
        betaBanner();
    }

    @Then("^the text reads (.*)$")
    public void
            theTextReadsBETAMaeHwnYnWasanaethNewyddByddEichAdborthAgorMewnTabNewyddYnEinHelpuIWWella(String expectedText) {
        betaBannerSentenceWelsh(expectedText);
    }

    @Then("^I check the page title (.*)$")
    public void i_check_the_page_title_who_was_your_uk_driving_license_issued_by(String expectedTitle) {
        validateDLPageTitleWelsh(expectedTitle);
    }

    @And("I assert the URL is valid in Welsh")
    public void iAssertTheURLIsValidInWelsh() {
        drivingLicencePageURLValidationWelsh();
    }

    @And("^I can see a I do not have a UK driving licence radio button titled (.*)$")
    public void iCanSeeAIDoNotHaveAUKDrivingLicenceRadioButtonTitledNidOesGennyfDrwyddedYrruYDU(String expectedText) {
        noDrivingLicenceBtnWelsh(expectedText);
    }

    @And("I can see “Or”")
    public void iCanSeeOr() { orDisplayWelsh();}

    @Then("I can see CTA as Parhau")
    public void iCanSeeCTAAsParhau() {
        continueButtonWelsh();
    }

    @And("^I see the licence Selection sentence starts with (.*)$")
    public void iSeeTheSentenceISeeTheSentenceStartsWithGallwchDdodOHydIHwnYnAdran(String expectedText)
            throws Throwable {
        licenceSelectionSentence(expectedText);
    }

    @Then("I should on the page DVLA and validate title")
    public void iShouldOnThePageDVLAAndValidateTitle() {
        pageTitleDVLAValidationWelsh();
    }

    @Then("I should on the page DVA and validate title")
    public void iShouldOnThePageDVAAndValidateTitle() {
        pageTitleDVLAValidationWelsh();
    }

    @And("^I see the heading (.*)$")
    public void iSeeTheHeadingRhowchEichManylionYnUnionFelMaentYnYmddangosArEichTrwyddedYrru(String expectedText) {
        dvlaPageHeading(expectedText);
    }

    @And("^I see We will check your details as (.*)$")
    public void iSeeTheSentenceWeWillCheckYourDetails(String expectedText) {
        weWillCheckYourDetails(expectedText);
    }

    @And("^I see sentence (.*)$")
    public void iSeeTheSentenceBelowOsNadOesGennych(String expectedText) {
        dvlaProveYourIdentitySentence(expectedText);
    }

    @And("^I can see Check your details as (.*)$")
    public void iCanSeeCheckYourDetailsAsGwiriwchBodEich(String expectedText) {
        checkYourDetailsSentence(expectedText);
    }

    @Given("^I can see the lastname as (.*)$")
    public void ICanSeeTheLastnameAsEnwOlaf(String expectedText) {
        lastNameWelsh(expectedText);
    }

    @And("^I can see the givenName as (.*)$")
    public void ICanSeeTheGivenNameAsEnwauARoddwyd(String expectedText) {
        givenNameWelsh(expectedText);
    }

    @And("^I can see the firstName as (.*)$")
    public void iCanSeeTheFirstNameAsEnwCyntaf(String expectedText) {
        firstNameWelsh(expectedText);
    }

    @And("^I can see the middleName as (.*)$")
    public void iCanSeeTheMiddleNameAsEnwauCanol(String expectedText) {
        middleNameWelsh(expectedText);
    }

    @And("^I can see the first name sentence (.*)$")
    public void iCanSeeTheFirstNameSentenceMaeHwnYnAdra(String expectedText) {
        firstNameSentence(expectedText);
    }

    @And("^I can see the sentence (.*)$")
    public void iCanSeeTheSentenceGadewchHynYnWagOsNadOesGennychUnrhywEnwauCanol(String expectedText) {
        middleNameSentence(expectedText);
    }

    @Given("^I can see the DoB fields titled (.*)$")
    public void iCanSeeTheDoBFieldsTitledDyddiadGeni(String expectedText) {
        dateOfBirthField(expectedText);
    }

    @And("^I can see example as (.*)$")
    public void iCanSeeExampleAsErEnghraifft(String expectedText) {
        dateOfBirthFieldhint(expectedText);
    }

    @And("^I can see date as (.*)$")
    public void iCanSeeDateAsDiwrnod(String expectedText) {
        dateField(expectedText);
    }

    @And("^I can see date for DVA as (.*)$")
    public void iCanSeeDateForDVAAsDiwrnod(String expectedText) {
        dateFieldDVA(expectedText);
    }

    @And("^I can see month as (.*)$")
    public void iCanSeeMonthAsMis(String expectedText) {
        monthField(expectedText);
    }

    @And("^I can see month for DVA as (.*)$")
    public void iCanSeeMonthForDVAAsMis(String expectedText) {
        monthFieldDVA(expectedText);
    }

    @And("^I can see year as (.*)$")
    public void iCanSeeYearAsBlwyddyn(String expectedText) {
        yearField(expectedText);
    }

    @Given("^I can see the Issue date field titled (.*)$")
    public void iCanSeeTheIssueDateFieldTitledDyddiadCyhoeddi(String expectedText ) {
        issueDateField(expectedText);
    }

    @Then("^I can see date sentence as (.*)$")
    public void iCanSeeDateSentenceAs(String expectedText) {
        issueDateSentence(expectedText);
    }

    @And("^I can see Valid to date sentence as (.*)$")
    public void iCanSeeValidToDateSentence(String expectedText) {
        validityDateSentence(expectedText);
    }

    @Then("^I can see the Valid to date field titled (.*)$")
    public void iCanSeeTheValidToDateFieldTitledYnDdilysTan(String expectedText) {
        validToDateFieldTitle(expectedText);
    }

    @And("^I see valid until example for DVA as (.*)$")
    public void DymaRDyddiadYnAdranBOChTrwyddedErEnghraifft(String expectedText) {
        validityDateSentenceforDVA(expectedText);
    }

    @Given("^I can see the licence number field titled (.*)$")
    public void iSelectedDVLAOnThePreviousPage(String expectedText) {
        licenceNumberWelsh(expectedText);
    }

    @Then("^I see the Licence number sentence (.*)$")
    public void DymaRRhifHirYnAdranArEichTrwyddedErEnghraifftHARRIMJ(String expectedText) {
        licenceNumberSentence(expectedText);
    }

    @Given("^I can see the licence number field for DVA titled (.*)$")
    public void licenceFieldDVARhiftrwydded(String expectedText) {
        licenceNumberWelshDVA(expectedText);
    }

    @Then("^I can see the issue number field titled (.*)$")
    public void iCanSeeTheIssueNumberFieldTitledDyddiadCyhoeddi(String expectedText) {
        issueNumberWelsh(expectedText);
    }

    @And("^I can see issue sentence as (.*)$")
    public void iCanSeeIssueSentenceAsDymaRRhifDdigidArÔlYGofodYnAdranOChTrwydded(String expectedText) {
        issueNumberSentence(expectedText);
    }

    @Then("^I can see the postcode field titled (.*)$")
    public void iCanSeeThePostcodeFieldTitledCodPost(String expectedText) {
        postcodeWelsh(expectedText);
    }

    @Then("^I can see postcode sentence as (.*)$")
    public void iCanSeePostcodeSentenceAs(String expectedText) {
        postcodeSentence(expectedText);
    }

    @When("User clicks on Parhau")
    public void userClicksOnParhau() {
        new DrivingLicencePageObject().Continue.click();
    }

    @Given("I click on DVLA radio button and Parhau")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButtonWelsh();
    }

    @Given("I click on DVA radio button and Parhau")
    public void i_select_dva_radio_button_and_Parhau() {
        clickOnDVARadioButtonWelsh();
    }

    @Given("I click Nid oes gennyf drwydded yrru y DU and Parhau")
    public void i_select_i_do_not_have_uk_driving_license() {
        noDrivingLicenceOptionWelsh();
    }

    @Then("^I can see the Valid to date field error (.*) for DVA$")
    public void RhowchYDyddiadFelYMaenymddangosareichtrwyddedyrruforDVA(String expectedText) {
        inValidIssueDateTextDVA(expectedText);
    }

    @When("I enter the invalid last name and first name")
    public void iEnterTheInvalidLastNameAndFirstName() {
        invalidlastAndFirstNameWelsh();
    }

    @Then("^the validation text reads (.*)$")
    public void theValidationTextReadsMaeProblem(String expectedText) {
        thereIsaProblemText(expectedText);
    }

    @And("^I see lastName error sentence as (.*)$")
    public void iSeeLastNameErrorSentenceAsRhowchEichEnwOlafFelYMaeNYmddangosArEichTrwyddedYrru(String expectedText) {
        lastNameErrorSentenceWelsh(expectedText);
    }

    @And("^I see firstName error sentence as (.*)$")
    public void iSeeFirstNameErrorSentenceAsRhowchEichEnwCyntafFelYMaeNYmddangosArEichTrwyddedYrru(String expectedText) {
        firstNameErrorSentenceWelsh(expectedText);
    }

    @And("^I see middleName error sentence as (.*)$")
    public void iSeeMiddleNameErrorSentenceAsRhowchUnrhywEnwauCanolFelY(String expectedText) {
        middleNameErrorSentence(expectedText);
    }

    @When("I click Parhau without entering any details")
    public void iClickParhauWithoutEnteringAnyDetails() {
        new DrivingLicencePageObject().Continue.click();
    }

    @And("^I see Enter the date as it appears as (.*)$")
    public void iSeeEnterTheDateAsItAppearsAsRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrru(String expectedText) {
        enterDOBErrorTextWelsh(expectedText);
    }

    @And("^I see check date of birth sentence as (.*)$")
    public void iSeeCheckDateOfBirthSentenceAsGwiriwchEichBodWediRhoiEichDyddiadGeniYnGywir(String expectedText) {
        enterValidDOBErrorTextWelsh(expectedText);
    }

    @Then("I clear the data and re enter the date of birth")
    public void iClearTheDataAndReEnterTheDateOfBirth() {
        clearDOBandReEnterWelsh();
    }

    @Then("I clear the data and re enter the date of birth to enter futureDOB")
    public void iClearTheDataAndReEnterTheDateOfBirthTo() {
        clearDOBandReEnterWelshtofuture();
    }

    @Then("^I see Your date of birth must be in the past (.*)$")
    public void iSeeYourDateOfBirthMustBeInThePastRhaidIChDyddiadGeniFodYnYGorffennol(String expectedText) {
        errorMessageFutureDOBWelsh(expectedText);
    }

    @And("^I see enter the date as it appearing on the DL (.*)$")
    public void iSeeRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrruForDVA(String expectedText) {
        enterDOBErrorTextWelshDVA(expectedText);
    }

    @When("I enter the invalid issue date")
    public void iEnterTheInvalidIssueDate() {
        enterInValidIssueDate();
    }

    @And("^I see issue date must be in the past as (.*)$")
    public void iSeeIssueDateMustBeInThePastAsRhaidIDdyddiadCyhoeddiFodYnYGorffennol(String expectedText) {
        issueDateErrorWelsh(expectedText);
    }

    @Then("I clear the data and re enter the invalid future year")
    public void iClearTheDataAndReEnterTheInvalidFutureYear() {
        enterInValidIssueDateWithFutureYear();
    }

    @When("I enter the invalid Valid to date field")
    public void iEnterTheInvalidValidToDateField() {
        enterInValidUntilDate();
    }

    @And("^I see Enter the date as it appears on your driving licence as (.*)$")
    public void iSeeEnterTheDateAsItAppearsOnYourDrivingLicence(String expectedText) {
        inValidIssueDateText(expectedText);
    }

    @Then("I clear the data and re enter the valid to expired year")
    public void iClearTheDataAndReEnterTheValidToExpiredYear() {
        enterTheValidToExpiredYear();
    }

    @And("^I see You cannot use an expired driving licence as (.*)$")
    public void iNiAllwchDdefnyddioTrwyddedYrruSyddWediDodIBen(String expectedText) {
        validToErrorWelsh(expectedText);
    }

    @When("I enter driving licence field empty")
    public void iEnterInvalidDrivingLicenceFieldEmpty() {
        invalidDrivingLicenceempty();
    }

    @Then("I clear the licence number and enter Driving Licence with Special Char for DVLA")
    public void iClearTheLicenceNumberAndEnterDrivingLicenceWithSpecialCharForDVLA() {
        invalidDrivingLicenceWithSplCharDVLA();
    }

    @And("I clear the licence number enter the invalid Driving Licence")
    public void iClearTheLicenceNumberEnterTheInvalidDrivingLicence() {
        inValidDrivingLicenceDVA();
    }

    @And("I clear the licence number enter the invalid Driving Licence for DVLA")
    public void iClearTheLicenceNumberForDVLA() {
        invalidDrivingLicenceDVLA();
    }

    @And("^I see no special character error as (.*)$")
    public void NiDdylaiRhifEichTrwyddedGynnwysUnrhywSymbolauNeuOfodau(String expectedText) {
        licenceErrorWelshforSplChar(expectedText);
    }

    @And("^I see Enter the number exactly as (.*)$")
    public void RhowchYRhifYnUnionFelMaeNYmddangosArEichTrwyddedYrru(String expectedText) {
        licenceErrorWelshforExactonDL(expectedText);
    }


    @When("I enter inValid issue number")
    public void iEnterInValidIssueNumber() {
        invalidIssueNumber();
    }

    @And("^I see Issue Number Error as (.*)$")
    public void DylaiEichRhifCyhoeddiFodYnRifOHyd(String expectedText) {
        IssueNumberErrorWelsh(expectedText);
    }

    @And("I clear Issue number to see the error Enter Issue number")
    public void iClearIssueNumberToSeeTheErrorEnterIssueNumber() {
        clearIssueNumber();
    }

    @And("^I see enter the Issue Number error as (.*)$")
    public void RhowchYRhifCyhoeddiFelYMaeNYmddangosArEichTrwyddedYrru(String expectedText) {
        enterIssueNumberErrorWelsh(expectedText);
    }

    @When("I enter the invalid Postcode")
    public void iEnterTheInvalidPostcode() {
        enterInValidPostCode();
    }

    @And("^I see postcode should contain only number and letter as (.*)$")
    public void DylaiEichRhowchEichCodPostOndCynnwysRhifauALlythrennauYnUnig(String expectedText) {
        postCodeErrorInvalidWelsh(expectedText);
    }

    @And("^I see DVA postcode should contain only number and letter as (.*)$")
    public void DVADylaiEichRhowchEichCodPostOndCynnwysRhifauALlythrennauYnUnig(String expectedText) {
        postCodeErrorInvalidWelshDVA(expectedText);
    }

    @And("I clear the postcode to see the Enter your postcode error")
    public void iClearThePostcodeToSeeTheEnterYourPostcodeError() {
        new DrivingLicencePageObject().Postcode.clear();
    }

    @And("^I see postcode should be 5 and 7 characters as (.*)$")
    public void DylaiEichRhowchEichCodPostFodRhwngANod(String expectedText) {
        postCodeErrorWelsh(expectedText);
    }

    @And("^I see DVA postcode should be 5 and 7 characters as (.*)$")
    public void DylaiEichRhowchEichCodPostFodRhwngANodDVA(String expectedText) {
        postCodeErrorWelshDVA(expectedText);
    }

    @And("^I see Enter your postcode as (.*)$")
    public void RhowchEichCodPost(String expectedText) {
        enterYourPostCodeErrorWelsh(expectedText);
    }
    @And("^I see Enter your DVA postcode as (.*)$")
    public void RhowchEichCodPostDVA(String expectedText) {
        enterYourPostCodeErrorWelshDVA(expectedText);
    }


    @Then("I clear the postcode and enter the less character postcode")
    public void iClearThePostcodeAndEnterTheLessCharacterPostcode() {
        invalidPostCode();
    }

    @When("I enter the invalid date of birth for DVA")
    public void iEnterTheInvalidDateOfBirthForDVA() {
        invalidDobForDVAWelsh();
    }

    @Then("I clear the data and re enter the date of birth to enter futureDOB for DVA")
    public void iClearTheDataAndReEnterTheDateOfBirthToEnterFutureDOBForDVA() {
        dvaclearDOBandReEnterWelshtofuture();
    }

    @Then("I clear the data and re enter the date of birth to enter pastDOB for DVA")
    public void iClearTheDataAndReEnterTheDateOfBirthToEnterPastDOBForDVA() {
        dvaPastErrorrWelsh();
    }

    @When("I enter the invalid issue date for DVA")
    public void iEnterTheInvalidIssueDateForDVA() {
        invalidIssueDayForDVA();
    }

    @Then("I clear the data and re enter the invalid future year for DVA")
    public void iClearTheDataAndReEnterTheInvalidFutureYearForDVA() {
        enterInValidIssueDateWithFutureYearDVA();
    }

    @When("I enter invalid driving licence less than 8 char for DVA")
    public void iEnterInvalidDrivingLicenceLessThanCharForDVA() {
        invalidDrivingLicenceWithlessCharDVA();
    }

    @When("I enter the invalid Valid to date field for DVA")
    public void iEnterTheInvalidValidToDateFieldForDVA() {
        invalidValidUntilForDVA();
    }

    @Then("I clear the data and re enter the valid to expired year for DVA")
    public void iClearTheDataAndReEnterTheValidToExpiredYearForDVA() {
        enterTheValidToExpiredYearForDVA();
    }

    @And("^I see Your licence number character long as (.*) for DVLA$")
    public void DylaiRhifEichTrwyddedFodYnXNodOHydForDVLA(String expectedText) {
        licenceNumberErrorWelshForDVLA(expectedText);
    }

    @And("^I see your DVA licence should be 8 char (.*)$")
    public void DylaiRhifEichTrwyddedFodYnXNodOHydForDVA(String expectedText) {
        licenceNumberErrorWelshForDVA(expectedText);
    }

    @And("^I see the DVA licence sentence (.*)$")
    public void DymaRRhifHirYnAdranArEichTrwyddedForDVA(String expectedText) {
        licenceNumberErrorWelshforDVA(expectedText);
    }

    @Then("I validate the page error page title")
    public void iValidateThePageErrorPageTitle() {
        ErrorPageTitleDVLA();
    }

    @And("^I see you will not be able to change your details as (.*)$")
    public void iSeeNiFyddwchYnGalluNewidEichManylionEtoOs(String expectedText) {
        thereIsaProblemSentence(expectedText);
    }

    @And("^I see error word as (.*)$")
    public void iSeeErrorWordAsGwall(String expectedText) {
        errorWord(expectedText);
    }

    @And("^I see We could not find your details as (.*)$")
    public void iSeeNidOeddemYnGalluDodOHydIChManylion(String expectedText) {
        weCouldNotFindDetailsSentence(expectedText);
    }

    @And("^I see Check your details as (.*)$")
    public void iSeeRoeddYnaBroblemWrthINiWirioEichManylionGydaRDVLADVA(String expectedText) {
        youWillBeAbleToFindSentence(expectedText);
    }

    @And("^I see check your details for DVA as (.*)$")
    public void RoeddYnaBroblemWrthINiWirioEichManylionGydaRDVA(String expectedText) {
        youWillBeAbleToFindSentenceDVA(expectedText);
    }

    @When("^I can see the DoB fields for DVA titled (.*)$")
    public void iCanSeeTheDoBFieldsForDVATitledDyddiadGeni(String expectedText) {
        dateOfBirthFieldDVA(expectedText);
    }

    @Then("^I can see example  for DVA as (.*)$")
    public void iCanSeeExampleForDVAAsErEnghraifft(String expectedText) {
        dateOfBirthFieldHintDVA(expectedText);
    }

    @And("^I can see year for DVA as (.*)$")
    public void iCanSeeYearForDVAAsBlwyddyn(String expectedText) {
        yearFieldDVA(expectedText);
    }

    @When("^I see the Issue date field titled (.*) for DVA$")
    public void iCanSeeTheIssueDateFieldTitledForDVADyddiadCyhoeddi(String expectedText) {
        issueDateFieldDVA(expectedText);
    }

    @Then("^I see date section example as (.*)$")
    public void dymaRDyddiadYnAdranAOChTrwyddedErEnghraifftForDVA(String expectedText) {
        issueDateSentenceDVA(expectedText);
    }
}