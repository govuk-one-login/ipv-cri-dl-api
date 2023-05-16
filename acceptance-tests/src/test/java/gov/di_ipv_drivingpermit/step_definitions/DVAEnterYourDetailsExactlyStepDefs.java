package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DVAEnterYourDetailsExactlyPageObject;
import gov.di_ipv_drivingpermit.pages.DrivingLicencePageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DVAEnterYourDetailsExactlyStepDefs extends DVAEnterYourDetailsExactlyPageObject {

    @When("^User enters DVA data as a (.*)$")
    public void user_enters_and(String drivingLicenceSubject) {
        userEntersData("DVA", drivingLicenceSubject);
    }

    @When("^I see the Issue date field titled (.*) for DVA$")
    public void iCanSeeTheIssueDateFieldTitledForDVADyddiadCyhoeddi(String expectedText) {
        assertDateOfIssueLegendText(expectedText);
    }

    @Then("^I see date section example as (.*)$")
    public void dymaRDyddiadYnAdranAOChTrwyddedErEnghraifftForDVA(String expectedText) {
        assertDateOfIssueHintText(expectedText);
    }

    @When("^I can see the DoB fields for DVA titled (.*)$")
    public void iCanSeeTheDoBFieldsForDVATitledDyddiadGeni(String expectedText) {
        assertDateOfBirthLegendTextDVLA(expectedText);
    }

    @Then("^I can see example  for DVA as (.*)$")
    public void iCanSeeExampleForDVAAsErEnghraifft(String expectedText) {
        assertDateOfBirthHintTextDVLA(expectedText);
    }

    @And("^I can see year for DVA as (.*)$")
    public void iCanSeeYearForDVAAsBlwyddyn(String expectedText) {
        assertBirthYearLabelTextDVA(expectedText);
    }

    @And("^I see your DVA licence should be 8 char (.*)$")
    public void DylaiRhifEichTrwyddedFodYnXNodOHydForDVA(String expectedText) {
        assertLicenceNumberErrorText(expectedText);
    }

    @And("^I see the DVA licence sentence (.*)$")
    public void DymaRRhifHirYnAdranArEichTrwyddedForDVA(String expectedText) {
        assertLicenceNumberHintforDVA(expectedText);
    }

    @And("^I see DVA enter the date as it appears above the field as (.*)$")
    public void iSeeRhowchYDyddiadFelYMaeNYmddangosArEichTrwyddedYrruForDVA(String expectedText) {
        assertDOBErrorText(expectedText);
    }

    @Given("^I can see the licence number field for DVA titled (.*)$")
    public void licenceFieldDVARhiftrwydded(String expectedText) {
        assertLicenceNumberLabelTextDVA(expectedText);
    }

    @Then(
            "^I should be on the page DVA Enter your details exactly as they appear on your UK driving licence - Prove your identity - GOV.UK$")
    public void i_should_be_on_the_DVA_page() {
        pageTitleDVAValidation();
    }

    @And("^I can see month for DVA as (.*)$")
    public void iCanSeeMonthForDVAAsMis(String expectedText) {
        assertBirthMonthLabelTextDVA(expectedText);
    }

    @And("^I can see date for DVA as (.*)$")
    public void iCanSeeDateForDVAAsDiwrnod(String expectedText) {
        assertBirthDayLabelTextDVA(expectedText);
    }

    @Given("I click on DVA radio button and Parhau")
    public void i_select_dva_radio_button_and_Parhau() {
        clickOnDVARadioButton();
    }

    @Given("I click on DVLA radio button and Parhau")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButton();
    }

    @Given("I click Nid oes gennyf drwydded yrru y DU and Parhau")
    public void i_select_i_do_not_have_uk_driving_license() {
        clickOnIDoNotHaveAUKDrivingLicenceRadioButton();
    }

    @When("I enter the invalid Postcode")
    public void iEnterTheInvalidPostcode() {
        enterInValidPostCode();
    }

    @And("I clear the licence number enter the invalid Driving Licence")
    public void iClearTheLicenceNumberEnterTheInvalidDrivingLicence() {
        enterDvaLicenceNumber("1acd1113756456");
    }

    @When("I enter the invalid date of birth for DVA")
    public void iEnterTheInvalidDateOfBirthForDVA() {
        enterDVADateOfBirth("ss", "aa", "aaaa");
    }

    @Then("I clear the data and re enter the date of birth to enter pastDOB for DVA")
    public void iClearTheDataAndReEnterTheDateOfBirthToEnterPastDOBForDVA() {
        enterDateOfBirthInThePast();
    }

    @When("I enter the invalid issue date for DVA")
    public void iEnterTheInvalidIssueDateForDVA() {
        enterInvalidIssueDayForDVA("", "", "");
    }

    @Then("I clear the data and re enter the invalid future year for DVA")
    public void iClearTheDataAndReEnterTheInvalidFutureYearForDVA() {
        enterInvalidIssueDayForDVA("23", "03", "2062");
    }

    @When("I enter invalid driving licence less than 8 char for DVA")
    public void iEnterInvalidDrivingLicenceLessThanCharForDVA() {
        enterDvaLicenceNumber("111106");
    }

    @When("I enter the invalid Valid to date field for DVA")
    public void iEnterTheInvalidValidToDateFieldForDVA() {
        enterDVAValidToDate("", "", "");
    }

    @Then("I clear the data and re enter the valid to expired year for DVA")
    public void iClearTheDataAndReEnterTheValidToExpiredYearForDVA() {
        enterDVAValidToDate("23", "03", "2005");
    }

    // Copied from welsh

    @And("I assert the URL is valid in Welsh")
    public void iAssertTheURLIsValidInWelsh() {
        drivingLicencePageURLValidationWelsh();
    }

    @And("^I see valid until example for DVA as (.*)$")
    public void DymaRDyddiadYnAdranBOChTrwyddedErEnghraifft(String expectedText) {
        assertValidToHintTextDVA(expectedText);
    }

    @And("^I see check your details for DVA as (.*)$")
    public void RoeddYnaBroblemWrthINiWirioEichManylionGydaRDVA(String expectedText) {
        youWillBeAbleToFindSentence(expectedText);
    }

    @And("^I see check date of birth sentence as (.*)$")
    public void iSeeCheckDateOfBirthSentenceAsGwiriwchEichBodWediRhoiEichDyddiadGeniYnGywir(
            String expectedText) {
        assertInvalidDoBInErrorSummary(expectedText);
    }

    @Then("I clear the data and re enter the date of birth")
    public void iClearTheDataAndReEnterTheDateOfBirth() {
        enterDvlaBirthYear("15", "04", "1968");
    }

    @When("I enter the invalid issue date")
    public void iEnterTheInvalidIssueDate() {
        enterIssueDate("", "", "");
    }

    @Then("I clear the data and re enter the invalid future year")
    public void iClearTheDataAndReEnterTheInvalidFutureYear() {
        enterIssueDate("23", "03", "2032");
    }

    @When("I enter the invalid Valid to date field")
    public void iEnterTheInvalidValidToDateField() {
        enterValidToDate("", "", "");
    }

    @Then("I clear the data and re enter the valid to expired year")
    public void iClearTheDataAndReEnterTheValidToExpiredYear() {
        enterValidToDate("12", "12", "2012");
    }

    @When("I enter driving licence field empty")
    public void iEnterInvalidDrivingLicenceFieldEmpty() {
        enterLicenceNumber("");
    }

    @Then("I clear the licence number and enter Driving Licence with Special Char for DVLA")
    public void iClearTheLicenceNumberAndEnterDrivingLicenceWithSpecialCharForDVLA() {
        enterLicenceNumber("@@@@@@@@@@@@@@@@");
    }

    @And("I clear the licence number enter the invalid Driving Licence for DVLA")
    public void iClearTheLicenceNumberForDVLA() {
        enterLicenceNumber("PARKE610@$112");
    }

    @And("^I see Issue Number Error as (.*)$")
    public void DylaiEichRhifCyhoeddiFodYnRifOHyd(String expectedText) {
        assertIssueNumberDescriptionText(expectedText);
    }

    @And("I clear Issue number to see the error Enter Issue number")
    public void iClearIssueNumberToSeeTheErrorEnterIssueNumber() {
        clearIssueNumber();
    }

    @And("I clear the postcode to see the Enter your postcode error")
    public void iClearThePostcodeToSeeTheEnterYourPostcodeError() {
        new DrivingLicencePageObject().Postcode.clear();
    }

    @Then("I clear the postcode and enter the less character postcode")
    public void iClearThePostcodeAndEnterTheLessCharacterPostcode() {
        enterPostcode("BS98");
    }

    @When("^User Re-enters DVA data as a (.*)$")
    public void userReInputsDataAsADrivingLicenceSubject(String drivingLicenceSubject) {
        userReEntersDataAsDVADrivingLicenceSubject(drivingLicenceSubject);
    }

    @When("^DVA consent checkbox is unselected$")
    public void dva_consent_checkbox_click() {
        consentDVACheckbox.click();
    }

    @Then("^User can see the DVA consent error in summary as (.*)$")
    public void shortDVAConsentErrorMessageIsDisplayed(String expectedText) {
        assertDVAConsentErrorInErrorSummary(expectedText);
    }

    @Then("^User can see the DVA consent error on the checkbox as (.*)$")
    public void shortDVAConsentCheckboxErrorMessageIsDisplayed(String expectedText) {
        assertDVAConsentErrorOnCheckbox(expectedText);
    }
}
