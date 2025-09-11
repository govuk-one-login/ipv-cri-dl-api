package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class DVLAAndDVADrivingLicenceStepDefs extends DrivingLicencePageObject {

    @When("User clicks on continue")
    public void user_clicks_on_continue() {
        continuebutton.click();
    }

    @When("User clicks the DVA consent checkbox")
    public void user_clicks_on_DVA_consent_box() {
        consentDVACheckbox.click();
    }

    @When("User clicks the DVLA consent checkbox")
    public void user_clicks_on_DVLA_consent_box() {
        consentDVLACheckbox.click();
    }

    @Then("User clicks selects the Yes Radio Button")
    public void user_clicks_on_radio_button_yes() {
        correctDetailsRadioButton.click();
    }

    @Then("User clicks selects the No Radio Button")
    public void user_clicks_on_radio_button_no() {
        incorrectDetailsRadioButton.click();
    }

    @Then("Proper error message for Could not find your details is displayed")
    public void properErrorMessageForCouldNotFindDVLADetailsIsDisplayed() {
        userNotFoundInThirdPartyErrorIsDisplayed();
    }

    @Then("Proper error message for dva Could not find your details is displayed")
    public void properErrorMessageForCouldNotFindDVLADetailsIsDisplayedDva() {
        userNotFoundInThirdPartyErrorIsDisplayedDva();
    }

    @Then("^I see enter the date as it appears above the field as (.*)$")
    public void fieldErrorMessageForNoDOBIsDisplayed(String expectedText) {
        assertInvalidDoBOnField(expectedText);
    }

    @Then("^I see the licence number error in the summary as (.*)$")
    public void shortDrivingLicenceNumberErrorMessageIsDisplayed(String expectedText) {
        assertInvalidLicenceNumberInErrorSummary(expectedText);
    }

    @Then("^I see the enter licence number as it appears above the field as (.*)$")
    public void fieldErrorMessageForInvalidLicenceNumberIsDisplayed(String expectedText) {
        assertInvalidLicenceNumberField(expectedText);
    }

    @Then("^I see the give your consent error in the summary as (.*)$")
    public void noConsentGivenErrorMessageIsDisplayed(String expectedText) {
        assertNoConsentGivenInErrorSummary(expectedText);
    }

    @Then("^I see the DVA give your consent error in the summary as (.*)$")
    public void noConsentGivenDVAErrorMessageIsDisplayed(String expectedText) {
        assertNoConsentGivenInDVAErrorSummary(expectedText);
    }

    @Given("User enters invalid Driving Licence DVLA details")
    public void userInputsInvalidDrivingDetails() {
        userEntersInvalidDrivingDetails();
    }

    @Given("User enters invalid Driving Licence DVA details")
    public void userInputsInvalidDVADrivingDetails() {
        new DVAEnterYourDetailsExactlyPageObject().userEntersInvalidDVADrivingDetails();
    }

    @Given("User click on ‘prove your identity another way' Link")
    public void userClickOnProveYourIdentityAnotherWayLink() {
        proveAnotherWay.click();
    }

    @When("User click on I do not have a UK driving licence radio button")
    public void selectIDoNotHaveAUKDrivingLicenceRadioButton() {
        clickOnIDoNotHaveAUKDrivingLicenceRadioButton();
    }

    @And("I see a form requesting DVA LicenceNumber")
    public void i_see_a_form_requesting_DVA_LicenceNumber() {
        Assert.assertTrue(
                new DVAEnterYourDetailsExactlyPageObject().dvaLicenceNumber.isDisplayed());
    }

    @And("I should see DVA as an option")
    public void i_should_see_DVA_as_an_option() {
        Assert.assertTrue(new DrivingLicencePageObject().optionDVA.isDisplayed());
    }

    @Given("User click on ‘Back' Link")
    public void userClickOnBackLink() {
        back.click();
    }

    @Then("^I see the consent section (.*)$")
    public void iSeeTheConsentSectionAllowDVLAToCheckYourDrivingLicenceDetails(
            String consentSection) {
        assertConsentSection(consentSection);
    }

    @Then("^I see privacy notice link (.*)$")
    public void iSeePrivacyNoticeLinkTheGOVUKOneLoginPrivacyNotice(String oneLoginPrivacyLink) {
        assertOneLoginPrivacyLink(oneLoginPrivacyLink);
    }

    @Then("^I see the DVLA privacy notice link (.*)$")
    public void iSeeTheDVLAPrivacyNoticeLinkTheDVLAPrivacyNoticeOpensInANewTab(
            String dvlaPrivacyLink) {
        assertDVLAPrivacyLink(dvlaPrivacyLink);
    }

    @And("I should see DVLA as an option")
    public void i_should_see_DVLA_as_an_option() {
        Assert.assertTrue(new DrivingLicencePageObject().optionDVLA.isDisplayed());
    }

    @And("I see a form requesting DVLA LicenceNumber")
    public void i_see_a_form_requesting_DVLA_LicenceNumber() {
        Assert.assertTrue(new DrivingLicencePageObject().licenceNumber.isDisplayed());
    }

    @And("^I see the sentence (.*)$")
    public void iSeeTheContentForDVLA(String contentDVLA) {
        assertDVLAContent(contentDVLA);
    }

    @And("^I see the second line (.*)$")
    public void iSeeTheLineToFindOutMoreAboutHowYourDrivingLicenceDetailsWillBeUsedYouCanRead(
            String contentDVLALine2) {
        assertDVLAContentLineTwo(contentDVLALine2);
    }
}
