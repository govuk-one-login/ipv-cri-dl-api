package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DrivingLicencePageObject;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DrivingLicenceStepDefs extends DrivingLicencePageObject {

    @When("^User enters DVLA data as a (.*)$")
    public void user_enters_and(String drivingLicenceSubject) {
        userEntersData("DVLA", drivingLicenceSubject);
    }

    @And("User re-enters last name as (.*)$")
    public void userEntersLastName(String invalidLastName) {
        userReEntersLastName(invalidLastName);
    }

    @And("User re-enters first name as (.*)$")
    public void userEntersFirstName(String invalidFirstName) {
        userReEntersFirstName(invalidFirstName);
    }

    @And("User re-enters birth day as (.*)$")
    public void userEntersBirthDay(String invalidBirthDay) {
        userReEntersBirthDay(invalidBirthDay);
    }

    @And("User re-enters birth month as (.*)$")
    public void userEntersBirthMonth(String invalidBirthMonth) {
        userReEntersBirthMonth(invalidBirthMonth);
    }

    @And("User re-enters birth year as (.*)$")
    public void userEntersBirthYear(String invalidBirthYear) {
        userReEntersBirthYear(invalidBirthYear);
    }

    @And("User re-enters DVA birth day as (.*)$")
    public void userEntersDvaBirthDay(String invalidBirthDay) {
        userReEntersDvaBirthDay(invalidBirthDay);
    }

    @And("User re-enters DVA birth month as (.*)$")
    public void userEntersDvaBirthMonth(String invalidBirthMonth) {
        userReEntersDvaBirthMonth(invalidBirthMonth);
    }

    @And("User re-enters DVA birth year as (.*)$")
    public void userEntersDvaBirthYear(String invalidBirthYear) {
        userReEntersDvaBirthYear(invalidBirthYear);
    }

    @And("User re-enters issue day as (.*)$")
    public void userEntersIssueDay(String invalidLicenceIssueDay) {
        userReEntersIssueDay(invalidLicenceIssueDay);
    }

    @And("User re-enters issue month as (.*)$")
    public void userEntersIssueMonth(String invalidLicenceIssueMonth) {
        userReEntersIssueMonth(invalidLicenceIssueMonth);
    }

    @And("User re-enters issue year as (.*)$")
    public void userEntersIssueYear(String invalidLicenceIssueYear) {
        userReEntersIssueYear(invalidLicenceIssueYear);
    }

    @And("User re-enters DVA issue day as (.*)$")
    public void userEntersDvaIssueDay(String invalidLicenceIssueDay) {
        userReEntersDvaIssueDay(invalidLicenceIssueDay);
    }

    @And("User re-enters DVA issue month as (.*)$")
    public void userEntersDvaIssueMonth(String invalidLicenceIssueMonth) {
        userReEntersDvaIssueMonth(invalidLicenceIssueMonth);
    }

    @And("User re-enters DVA issue year as (.*)$")
    public void userEntersDvaIssueYear(String invalidLicenceIssueYear) {
        userReEntersDvaIssueYear(invalidLicenceIssueYear);
    }

    @And("User re-enters license number as (.*)$")
    public void userEntersLicenceNumber(String invalidLicenceNumber) {
        userReEntersLicenceNumber(invalidLicenceNumber);
    }

    @And("User re-enters DVA license number as (.*)$")
    public void userEntersDVALicenceNumber(String invalidLicenceNumber) {
        userReEntersDvaLicenceNumber(invalidLicenceNumber);
    }

    @And("User re-enters valid issue number as (.*)$")
    public void userEntersIssueNumber(String invalidIssueNumber) {
        userReEntersIssueNumber(invalidIssueNumber);
    }

    @And("User re-enters valid to day as (.*)$")
    public void userEntersValidToDay(String invalidValidToDay) {
        userReEntersValidToDay(invalidValidToDay);
    }

    @And("User re-enters valid to month as (.*)$")
    public void userEntersValidToMonth(String invalidValidToMonth) {
        userReEntersValidToMonth(invalidValidToMonth);
    }

    @And("User re-enters valid to year as (.*)$")
    public void userEntersValidToYear(String invalidValidToYear) {
        userReEntersValidToYear(invalidValidToYear);
    }

    @And("User re-enters postcode as (.*)$")
    public void userEntersPostcode(String invalidPostcode) {
        userReEntersPostcode(invalidPostcode);
    }

    @Given("I can see a DVLA radio button titled (.*)$")
    public void i_can_see_a_radio_button_titled_dvla(String expectedText) {
        titleDVLAWithRadioBtn(expectedText);
    }

    @Then("I can see a DVA radio button titled (.*)$")
    public void i_can_see_a_radio_button_titled_dva(String expectedText) {
        titleDVAWithRadioBtn(expectedText);
    }

    @And("^I can see a I do not have a UK driving licence radio button titled (.*)$")
    public void iCanSeeAIDoNotHaveAUKDrivingLicenceRadioButton(String expectedText) {
        noDrivingLicenceBtn(expectedText);
    }

    @Then("I can see CTA (.*)$")
    public void i_can_see_cta(String expectedText) {
        ContinueButton(expectedText);
    }

    @Given("I click on DVLA radio button and continue")
    public void i_click_on_DVLA_radio_button_and_continue() {
        clickOnDVLARadioButton();
    }

    @Given("I click on DVA radio button and continue")
    public void i_select_dva_radio_button_and_continue() {
        clickOnDVARadioButton();
    }

    @Given("I click I do not have UK Driving License and continue")
    public void i_select_i_do_not_have_uk_driving_license() {
        clickOnIDoNotHaveAUKDrivingLicenceRadioButton();
    }

    @Given("I have not selected anything and continue")
    public void i_have_not_selected_anything() {
        noSelectContinue();
    }

    @When("I can see an error box highlighted red")
    public void i_can_see_an_error_box_highlighted_red() {
        errorMessage();
    }

    @And("^An error heading copy (.*)$")
    public void an_error_heading_copy_you_must_choose_an_option_to_continue(String expectedText) {
        assertErrorTitle(expectedText);
    }

    @Then("I can select a link which directs to the problem field")
    public void i_can_select_a_link_which_directs_to_the_problem_field() {
        errorLink();
    }

    @And("The field error copy (.*)$")
    public void the_field_error_copy_you_must_choose_an_option_to_continue(String expectedText) {
        validateErrorText(expectedText);
    }

    @Then("^I can see the error heading (.*)$")
    public void i_can_see_the_error_heading_page(String errorHeading) {
        validateErrorPageHeading(errorHeading);
    }

    @Given("^I delete the (.*) cookie to get the unexpected error$")
    public void iDeleteTheCookieToGetTheUnexpectedError(String cookieName) {
        BrowserUtils.deleteCookie(cookieName);
    }

    @Then("I see ‘Why we need to know this’ component is present")
    public void iSeeWhyWeNeedToKnowThisComponentIsPresent() {
        whyWeNeedToKnowThis();
    }

    @When("I click the drop-down on the component")
    public void iClickTheDropDownOnTheComponent() {
        clickOnWhyWeNeedLink();
    }

    @Then("I see the message begins with We need to make sure is shown")
    public void iSeeTheMessageBeginsWithWeNeedToMakeSureIsShown() {
        paragraphValidation();
    }

    @When("^User Re-enters DVLA data as a (.*)$")
    public void userReInputsDataAsADrivingLicenceSubject(String drivingLicenceSubject) {
        userReEntersDataAsADrivingLicenceSubject(drivingLicenceSubject);
    }

    @Given("User clicks on language toggle and switches to Welsh")
    public void userClickOnLanguageToggle() {
        languageToggle.click();
    }

    @Given("User clicks language toggle and switches to English")
    public void userClickOnLanguageToggleWales() {
        languageToggleWales.click();
    }
}
