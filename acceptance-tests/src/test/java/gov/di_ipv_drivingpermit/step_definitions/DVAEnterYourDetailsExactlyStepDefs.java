package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DVAEnterYourDetailsExactlyPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DVAEnterYourDetailsExactlyStepDefs extends DVAEnterYourDetailsExactlyPageObject {

    @When("^User enters DVA data as a (.*)$")
    public void user_enters_and(String drivingLicenceSubject) {
        userEntersData("DVA", drivingLicenceSubject);
    }

    @Given("I click on DVA radio button and Parhau")
    public void i_select_dva_radio_button_and_Parhau() {
        clickOnDVARadioButton();
    }

    @Given("I click on DVLA radio button and Parhau")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButton();
    }

    @And("I assert the URL is valid in Welsh")
    public void iAssertTheURLIsValidInWelsh() {
        drivingLicencePageURLValidationWelsh();
    }

    @And("^I see check date of birth sentence as (.*)$")
    public void iSeeCheckDateOfBirthText(String expectedText) {
        assertInvalidDoBInErrorSummary(expectedText);
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

    @Then("^I see the DVA consent section (.*)$")
    public void iSeeTheDVAConsentSectionAllowDVAToCheckYourDrivingLicenceDetails(
            String consentSectionDVA) {
        assertDVAConsentSection(consentSectionDVA);
    }

    @And("^I see the Consent sentence in DVA page (.*)$")
    public void iSeeTheSentenceInDVAPage(String contentDVA) {
        assertDVAContent(contentDVA);
    }

    @And("^I see the Consent second line in DVA page (.*)$")
    public void iSeeTheComsentSecondLineInDVAPage(String contentDVALine2) {
        assertDVAContentLineTwo(contentDVALine2);
    }

    @And("^I see privacy DVA notice link (.*)$")
    public void iSeePrivacyDVANoticeLinkTheGOVUKOneLoginPrivacyNoticeOpensInANewTab(
            String oneLoginPrivacyLinkDVA) {
        assertDVAOneLoginPrivacyLink(oneLoginPrivacyLinkDVA);
    }

    @Then("^I see the DVA privacy notice link (.*)$")
    public void iSeeTheDVAPrivacyNoticeLinkTheDVAPrivacyNoticeOpensInANewTab(
            String dvaPrivacyLink) {
        assertDVAPrivacyLink(dvaPrivacyLink);
    }
}
