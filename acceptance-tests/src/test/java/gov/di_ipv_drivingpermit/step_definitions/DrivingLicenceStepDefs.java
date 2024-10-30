package gov.di_ipv_drivingpermit.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public void userEntersLastName(String InvalidLastName) {
        userReEntersLastName(InvalidLastName);
    }

    @And("User re-enters first name as (.*)$")
    public void userEntersFirstName(String InvalidFirstName) {
        userReEntersFirstName(InvalidFirstName);
    }

    @And("User re-enters middle names as (.*)$")
    public void userEntersMiddleNames(String InvalidMiddleNames) {
        userReEntersMiddleNames(InvalidMiddleNames);
    }

    @And("User re-enters birth day as (.*)$")
    public void userEntersBirthDay(String InvalidBirthDay) {
        userReEntersBirthDay(InvalidBirthDay);
    }

    @And("User re-enters birth month as (.*)$")
    public void userEntersBirthMonth(String InvalidBirthMonth) {
        userReEntersBirthMonth(InvalidBirthMonth);
    }

    @And("User re-enters birth year as (.*)$")
    public void userEntersBirthYear(String InvalidBirthYear) {
        userReEntersBirthYear(InvalidBirthYear);
    }

    @And("User re-enters DVA birth day as (.*)$")
    public void userEntersDvaBirthDay(String InvalidBirthDay) {
        userReEntersDvaBirthDay(InvalidBirthDay);
    }

    @And("User re-enters DVA birth month as (.*)$")
    public void userEntersDvaBirthMonth(String InvalidBirthMonth) {
        userReEntersDvaBirthMonth(InvalidBirthMonth);
    }

    @And("User re-enters DVA birth year as (.*)$")
    public void userEntersDvaBirthYear(String InvalidBirthYear) {
        userReEntersDvaBirthYear(InvalidBirthYear);
    }

    @And("User re-enters issue day as (.*)$")
    public void userEntersIssueDay(String invalidLicenceIssueDay) {
        userReEntersIssueDay(invalidLicenceIssueDay);
    }

    @And("User re-enters issue month as (.*)$")
    public void userEntersIssueMonth(String InvalidLicenceIssueMonth) {
        userReEntersIssueMonth(InvalidLicenceIssueMonth);
    }

    @And("User re-enters issue year as (.*)$")
    public void userEntersIssueYear(String InvalidLicenceIssueYear) {
        userReEntersIssueYear(InvalidLicenceIssueYear);
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
    public void userEntersLicenceNumber(String InvalidLicenceNumber) {
        userReEntersLicenceNumber(InvalidLicenceNumber);
    }

    @And("User re-enters DVA license number as (.*)$")
    public void userEntersDVALicenceNumber(String InvalidLicenceNumber) {
        userReEntersDvaLicenceNumber(InvalidLicenceNumber);
    }

    @And("User re-enters valid issue number as (.*)$")
    public void userEntersIssueNumber(String invalidIssueNumber) {
        userReEntersIssueNumber(invalidIssueNumber);
    }

    @And("User re-enters valid to day as (.*)$")
    public void userEntersValidToDay(String InvalidValidToDay) {
        userReEntersValidToDay(InvalidValidToDay);
    }

    @And("User re-enters valid to month as (.*)$")
    public void userEntersValidToMonth(String InvalidValidToMonth) {
        userReEntersValidToMonth(InvalidValidToMonth);
    }

    @And("User re-enters valid to year as (.*)$")
    public void userEntersValidToYear(String InvalidValidToYear) {
        userReEntersValidToYear(InvalidValidToYear);
    }

    @And("User re-enters postcode as (.*)$")
    public void userEntersPostcode(String invalidPostcode) {
        userReEntersPostcode(invalidPostcode);
    }

    @Given("I navigate to the IPV Core Stub")
    public void navigateToStub() {
        navigateToIPVCoreStub();
    }

    @Given("I navigate to the IPV Core Stub and select Driving Licence CRI for the testEnvironment")
    public void navigateToStubAndDrivingLicenceCRIOnTestEnv() {
        navigateToIPVCoreStub();
        navigateToDrivingLicenceCRIOnTestEnv();
    }

    @Then("^I search for Driving Licence user number (.*) in the Experian table$")
    public void i_search_for_DL_user_number(String number) {
        searchForUATUser(number);
    }

    @Then("^I enter the context value (.*) in the Input context value as a string$")
    public void i_enter_a_context_value(String contextValue) {
        enterContextValue(contextValue);
    }

    @Then("^I enter the shared claims raw JSON (.*) in the Input shared claims raw JSON$")
    public void i_enter_shared_claims_raw_json_data(String jsonFileName) {
        enterSharedClaimsRawJSONValue(jsonFileName);
    }

    @And("I assert the url path contains (.*)$")
    public void i_assert_the_url_path_contains(String path) {
        drivingLicencePageURLValidation(path);
    }

    @Given("^I check the page title is (.*)$")
    public void i_check_the_page_titled(String pageTitle) {
        assertPageTitle(pageTitle, false);
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
    public void iCanSeeAIDoNotHaveAUKDrivingLicenceRadioButtonTitledNidOesGennyfDrwyddedYrruYDU(
            String expectedText) {
        noDrivingLicenceBtn(expectedText);
    }

    @Then("I can see CTA (.*)$")
    public void i_can_see_cta(String expectedText) {
        ContinueButton(expectedText);
    }

    @Given("I click on DVLA radio button and Continue")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButton();
    }

    @Then(
            "^I should on the page DVLA Enter your details exactly as they appear on your UK driving licence - Prove your identity - GOV.UK$")
    public void i_should_be_on_the_DVLA_page() {
        pageTitleDVLAValidation();
    }

    @Given("I click on DVA radio button and Continue")
    public void i_select_dva_radio_button_and_Continue() {
        clickOnDVARadioButton();
    }

    @Given("I click I do not have UK Driving License and continue")
    public void i_select_i_do_not_have_uk_driving_license() {
        clickOnIDoNotHaveAUKDrivingLicenceRadioButton();
    }

    @When("I am directed to the IPV Core routing page")
    public void i_am_directed_to_the_ipv_core_routing_page() {
        assertUserRoutedToIpvCore();
    }

    @Given("I have not selected anything and Continue")
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

    @And("I validate the URL having access denied")
    public void iValidateTheURLHavingAccessDenied() {
        assertUserRoutedToIpvCoreErrorPage();
    }

    @Then("^I navigate to the Driving Licence verifiable issuer to check for a (.*) response$")
    public void i_navigate_to_driving_licence_verifiable_issuer_for_valid_response(
            String validOrInvalid) {
        navigateToDrivingLicenceResponse(validOrInvalid);
    }

    @And("^JSON response should contain error description (.*) and status code as (.*)$")
    public void errorInJsonResponse(String testErrorDescription, String testStatusCode)
            throws JsonProcessingException {
        jsonErrorResponse(testErrorDescription, testStatusCode);
    }

    @And("I click Go to Driving Licence CRI dev button")
    public void i_click_go_to_driving_licence_cri_dev_button() {
        navigateToDrivingLicenceCRI();
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

    @And("^I click the Driving Licence CRI for the testEnvironment$")
    public void navigateToDrivingLicenceOnTestEnv() {
        navigateToDrivingLicenceCRIOnTestEnv();
    }

    @Given("I view the Beta banner")
    public void iViewTheBetaBanner() {
        betaBanner();
    }

    @Then("^the beta banner reads (.*)$")
    public void betaBannerContainsText(String expectedText) {
        betaBannerSentence(expectedText);
    }

    @And("^I select (.*) cookie$")
    public void selectRejectAnalysisCookie(String rejectAnalysis) {
        rejectAnalysisCookie(rejectAnalysis);
    }

    @Then("^I see the Reject Analysis sentence (.*)$")
    public void
            iSeeTheSenetenceYouVeRejectedAdditionalCookiesYouCanChangeYourCookieSettingsAtAnyTime(
                    String rejectanalysisSentence) {
        rejectCookieSentence(rejectanalysisSentence);
    }

    @And("^I select the (.*) link$")
    public void iSelectTheChangeYourCookieSettingsLink(String changeCookieLink) {
        AssertChangeCookieLink(changeCookieLink);
    }

    @Then("^I check the page to change cookie preferences opens$")
    public void iCheckThePageToChangeCookiePreferencesOpens() {
        AssertcookiePreferencePage();
    }

    @And("^I can see OR options as (.*)$")
    public void ICanSeeTheOrDividerTextAs(String expectedText) {
        assertOrLabelText(expectedText);
    }

    @And("^I see the licence Selection sentence starts with (.*)$")
    public void ICanSeeThePageDescriptionAs(String expectedText) throws Throwable {
        assertPageDescription(expectedText);
    }

    @And("^I see the heading (.*)$")
    public void ICanSeeTheHeadingTextAs(String expectedText) {
        assertPageHeading(expectedText);
    }

    @And("^I see We will check your details as (.*)$")
    public void iSeeTheSentenceWeWillCheckYourDetails(String expectedText) {
        assertPageSourceContains(expectedText);
    }

    @And("^I see sentence (.*)$")
    public void ICanSeeProveAnotherWayLinkTextAs(String expectedText) {
        assertProveAnotherWayLinkText(expectedText);
    }

    @And("^I can see Check your details as (.*)$")
    public void ICanSeeTitleAs(String expectedText) {
        assertPageHeading(expectedText);
    }

    @Given("^I can see the lastname as (.*)$")
    public void ICanSeeLastNameLegendAs(String expectedText) {
        assertLastNameLabelText(expectedText);
    }

    @And("^I can see the givenName as (.*)$")
    public void ICanSeeGivenNameLegendAs(String expectedText) {
        assertGivenNameLegendText(expectedText);
    }

    @And("^I can see the firstName as (.*)$")
    public void ICanSeeFirstNameLabelAs(String expectedText) {
        assertGivenNameDescription(expectedText);
    }

    @And("^I can see the middleName as (.*)$")
    public void ICanSeeMiddleNameLabelAs(String expectedText) {
        assertMiddleNameLabelText(expectedText);
    }

    @And("^I can see the first name sentence (.*)$")
    public void ICanSeeTheFirstNameHintAs(String expectedText) {
        assertGivenNameHint(expectedText);
    }

    @And("^I can see the sentence (.*)$")
    public void ICanSeeMiddleNameHintAs(String expectedText) {
        assertMiddleNameHint(expectedText);
    }

    @Given("^I can see the DoB fields titled (.*)$")
    public void ICanSeeDateOfBirthLegendAs(String expectedText) {
        assertDateOfBirthLegendText(expectedText);
    }

    @And("^I can see example as (.*)$")
    public void ICanSeeDateOfBirthHintTextAs(String expectedText) {
        assertDateOfBirthHintText(expectedText);
    }

    @And("^I can see date as (.*)$")
    public void ICanSeeBirthDayAs(String expectedText) {
        assertBirthDayLabelText(expectedText);
    }

    @And("^I can see month as (.*)$")
    public void ICanSeeMonthAs(String expectedText) {
        assertBirthMonthLabelText(expectedText);
    }

    @And("^I can see year as (.*)$")
    public void ICanSeeIssueYearAs(String expectedText) {
        assertBirthYearLabelText(expectedText);
    }

    @Given("^I can see the Issue date field titled (.*)$")
    public void ICanSeeTheIssueDateFieldAs(String expectedText) {
        assertIssueDateLegendText(expectedText);
    }

    @Then("^I can see date sentence as (.*)$")
    public void iCanSeeDateSentenceAs(String expectedText) {
        assertIssueDateHintText(expectedText);
    }

    @And("^I can see Valid to date sentence as (.*)$")
    public void iCanSeeValidToDateSentence(String expectedText) {
        assertValidToHintText(expectedText);
    }

    @Then("^I can see the Valid to date field titled (.*)$")
    public void ICanSeeTheValidToDateFieldAs(String expectedText) {
        assertValidToLegend(expectedText);
    }

    @Given("^I can see the licence number field titled (.*)$")
    public void iSelectedDVLAOnThePreviousPage(String expectedText) {
        assertLicenceNumberLabelText(expectedText);
    }

    @Then("^I see the Licence number sentence (.*)$")
    public void ISeeTheLicenceNumberSentenceAs(String expectedText) {
        assertLicenceNumberHintText(expectedText);
    }

    @Then("^I can see the issue number field titled (.*)$")
    public void ICanSeeIssueNumberNumberFieldTitledAs(String expectedText) {
        assertIssueNumberLabelText(expectedText);
    }

    @And("^I can see issue sentence as (.*)$")
    public void ICanSeeIssueSentenceAs(String expectedText) {
        assertIssueNumberHintText(expectedText);
    }

    @Then("^I can see the postcode field titled (.*)$")
    public void iCanSeeThePostcodeFieldTitledCodPost(String expectedText) {
        assertPostcodeLabelText(expectedText);
    }

    @Then("^I can see postcode sentence as (.*)$")
    public void iCanSeePostcodeSentenceAs(String expectedText) {
        assertPostcodeHintText(expectedText);
    }

    @When("I enter the invalid last name and first name")
    public void iEnterTheInvalidLastNameAndFirstName() {
        enterInvalidLastAndFirstName();
    }

    @Then("^the validation text reads (.*)$")
    public void theValidationTextReadsMaeProblem(String expectedText) {
        assertErrorSummaryText(expectedText);
    }

    @And("^I see Check your details as (.*)$")
    public void ISeeCheckYourDetailsAs(String expectedText) {
        youWillBeAbleToFindSentence(expectedText);
    }

    @And("^I see We could not find your details as (.*)$")
    public void ISeeWeCouldNotFindYourDetailsAs(String expectedText) {
        assertFirstLineOfUserNotFoundText(expectedText);
    }

    @And("^I see you will not be able to change your details as (.*)$")
    public void ISeeYouWillNotBeAbleToChangeYourDetailsAs(String expectedText) {
        assertPageSourceContains(expectedText);
    }

    @And("^I see error word as (.*)$")
    public void iSeeErrorWordAsGwall(String expectedText) {
        assertErrorPrefix(expectedText);
    }

    @When("^User Re-enters DVLA data as a (.*)$")
    public void userReInputsDataAsADrivingLicenceSubject(String drivingLicenceSubject) {
        userReEntersDataAsADrivingLicenceSubject(drivingLicenceSubject);
    }

    @And("^I navigate to the page (.*)$")
    public void navigateToPage(String page) {
        goToPage(page);
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
