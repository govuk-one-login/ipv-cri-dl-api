package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.io.IOException;

public class DVLAAndDVADrivingLicenceStepDefs extends DrivingLicencePageObject {

    @When("User clicks on continue")
    public void user_clicks_on_continue() {
        Continue.click();
    }

    @Then("Proper error message for Could not find your details is displayed")
    public void properErrorMessageForCouldNotFindDVLADetailsIsDisplayed() {
        userNotFoundInThirdPartyErrorIsDisplayed();
    }

    @Then("^I see enter the date as it appears above the field as (.*)$")
    public void fieldErrorMessageForNoDOBIsDisplayed(String expectedText) {
        assertInvalidDoBOnField(expectedText);
    }

    @Then("^I see issue date error in summary as (.*)$")
    public void properErrorMessageForInvalidIssueDateIsDisplayed(String expectedText) {
        assertInvalidIssueDateInErrorSummary(expectedText);
    }

    @Then("^I see invalid issue date field error as (.*)$")
    public void fieldErrorMessageForInvalidIssueDateIsDisplayed(String expectedText) {
        assertInvalidIssueDateOnField(expectedText);
    }

    @Then("^I can see the valid to date error in the error summary as (.*)$")
    public void properErrorMessageForInvalidValidToDateIsDisplayed(String expectedText) {
        assertInvalidValidToDateInErrorSummary(expectedText);
    }

    @Then("^I can see the Valid to date field error as (.*)$")
    public void fieldErrorMessageForInvalidValidToDateIsDisplayed(String expectedText) {
        assertInvalidValidToDateOnField(expectedText);
    }

    @Then("^I see the licence number error in the summary as (.*)$")
    public void shortDrivingLicenceNumberErrorMessageIsDisplayed(String expectedText) {
        assertInvalidLicenceNumberInErrorSummary(expectedText);
    }

    @Then("^I can see the licence number error in the field as (.*)$")
    public void shortDrivingLicenceNumberFieldErrorMessageIsDisplayed(String expectedText) {
        assertInvalidLicenceNumberOnField(expectedText);
    }

    @Then("^I see the issue number error in summary as (.*)$")
    public void shortIssueNumberErrorMessageIsDisplayed(String expectedText) {
        assertInvalidIssueNumberInErrorSummary(expectedText);
    }

    @Then("^I see the issue number error in field as (.*)$")
    public void shortIssueNumberFieldErrorMessageIsDisplayed(String expectedText) {
        assertInvalidIssueNumberOnField(expectedText);
    }

    @Then("^I see the postcode error in summary as (.*)$")
    public void shortPostcodeErrorMessageIsDisplayed(String expectedText) {
        assertInvalidPostcodeInErrorSummary(expectedText);
    }

    @Then("^I see the postcode error in the field as (.*)$")
    public void shortPostcodeFieldErrorMessageIsDisplayed(String expectedText) {
        assertInvalidPostcodeOnField(expectedText);
    }

    @Then("^I see the Lastname error in the error summary as (.*)$")
    public void properErrorMessageForInvalidLastNameIsDisplayed(String expectedText) {
        assertInvalidLastNameInErrorSummary(expectedText);
    }

    @Then("^I see the Lastname error in the error field as (.*)$")
    public void fieldErrorMessageForInvalidLastNameIsDisplayed(String expectedText) {
        assertInvalidLastNameOnField(expectedText);
    }

    @Then("^I see the firstname error summary as (.*)$")
    public void properErrorMessageForInvalidFirstNameIsDisplayed(String expectedText) {
        assertInvalidFirstNameInErrorSummary(expectedText);
    }

    @Then("^I see the firstname error in the error field as (.*)$")
    public void fieldErrorMessageForInvalidFirstNameIsDisplayed(String expectedText) {
        assertInvalidFirstNameOnField(expectedText);
    }

    @Then("^I see the middlenames error summary as (.*)")
    public void properErrorMessageForInvalidMiddleNamesIsDisplayed(String expectedText) {
        assertInvalidMiddleNameInErrorSummary(expectedText);
    }

    @Then("^I see the middlenames error in the error field as (.*)$")
    public void fieldErrorMessageForInvalidMiddleNamesIsDisplayed(String expectedText) {
        assertInvalidMiddleNameOnField(expectedText);
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

    @Then(
            "I should be on `Enter your details exactly as they appear on your UK driving licence - Prove your identity - GOV.UK` page")
    public void
            i_should_be_on_enter_your_details_exactly_as_they_appear_on_your_uk_driving_licence_prove_your_identity_gov_uk_page() {
        Assert.assertTrue(new DrivingLicencePageObject().LicenceNumber.isDisplayed());
    }

    @Then(
            "I should be on DVA `Enter your details exactly as they appear on your UK driving licence - Prove your identity - GOV.UK` page")
    public void
            i_should_be_on_DVA_enter_your_details_exactly_as_they_appear_on_your_uk_driving_licence_prove_your_identity_gov_uk_page() {
        Assert.assertTrue(
                new DVAEnterYourDetailsExactlyPageObject().dvaLicenceNumber.isDisplayed());
    }

    @Then(
            "I should be on `Who was your UK driving licence issued by? - Prove your identity - GOV.UK` page")
    public void
            i_should_be_on_who_was_your_uk_driving_licence_issued_by_prove_your_identity_gov_uk_page() {
        Assert.assertTrue(new DrivingLicencePageObject().optionDVLA.isDisplayed());
    }

    @And("^JSON payload should contain ci (.*), validity score (.*) and strength score (.*)$")
    public void contraIndicatorInVerifiableCredential(
            String ci, String validityScore, String strengthScore) throws IOException {
        new DrivingLicencePageObject().ciInVC(ci);
        checkScoreInStubIs(validityScore, strengthScore);
    }

    @And("^JSON payload should contain validity score (.*) and strength score (.*)$")
    public void scoresInVerifiableCredential(String validityScore, String strengthScore)
            throws IOException {
        checkScoreInStubIs(validityScore, strengthScore);
    }

    @Given("User click on ‘Back' Link")
    public void userClickOnBackLink() {
        back.click();
    }

    @Then("^As a DVA user I see enter the date as it appears above the field as (.*)$")
    public void fieldErrorForFutureDVADOBIsDisplayed(String expectedText) {
        new DVAEnterYourDetailsExactlyPageObject().assertInvalidDateOfBirthErrorText(expectedText);
    }

    @Then("^As a DVA user I see issue date error in summary as (.*)$")
    public void properDVAErrorForInvalidIssueDateIsDisplayed(String expectedText) {
        new DVAEnterYourDetailsExactlyPageObject().assertInvalidIssueDateErrorText(expectedText);
    }

    @Then("^As a DVA user I see invalid issue date field error as (.*)$")
    public void fieldDVAErrorForInvalidIssueDateIsDisplayed(String expectedText) {
        new DVAEnterYourDetailsExactlyPageObject()
                .assertInvalidIssueDateErrorFieldText(expectedText);
    }

    @Then("^As a DVA user I see the licence number error in the summary as (.*)$")
    public void shortDVADrivingLicenceNumberErrorMessageIsDisplayed(String expectedText) {
        new DVAEnterYourDetailsExactlyPageObject()
                .assertInvalidDrivingLicenceErrorSummaryText(expectedText);
    }

    @Then("^As a DVA user I can see the licence number error in the field as (.*)$")
    public void shortDVADrivingLicenceNumberFieldErrorIsDisplayed(String expectedText) {
        new DVAEnterYourDetailsExactlyPageObject()
                .assertInvalidDrivingLicenceFieldText(expectedText);
    }

    @And("^JSON response should contain personal number (.*) same as given Driving Licence$")
    public void errorInJsonResponse(String personalNumber) throws IOException {
        new DrivingLicencePageObject().assertPersonalNumberInVc(personalNumber);
    }

    @When("^DVLA consent checkbox is unselected$")
    public void dvla_consent_checkbox_click() {
        consentDVLACheckbox.click();
    }

    @Then("^User can see the DVLA consent error in summary as (.*)$")
    public void shortDVLAConsentErrorMessageIsDisplayed(String expectedText) {
        assertDVLAConsentErrorInErrorSummary(expectedText);
    }

    @Then("^User can see the DVLA consent error on the checkbox as (.*)$")
    public void shortDVLAConsentCheckboxErrorMessageIsDisplayed(String expectedText) {
        assertDVLAConsentErrorOnCheckbox(expectedText);
    }

    @Then("User clears the driving licence number and enters the new value as (.*)$")
    public void iReEnterTheLicenceNumberForDVLA(String licenceNumber) {
        enterLicenceNumber(licenceNumber);
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

    @And(
            "I should be on `Who was your UK driving licence issued by? – Prove your identity – GOV.UK` page")
    public void iShouldBeOnWhoWasYourUKDrivingLicenceIssuedByProveYourIdentityGOVUKPage() {
        Assert.assertTrue(new DrivingLicencePageObject().optionDVA.isDisplayed());
    }

    @And(
            "I should be on `Enter your details exactly as they appear on your UK driving licence – Prove your identity – GOV.UK` page")
    public void
            iShouldBeOnEnterYourDetailsExactlyAsTheyAppearOnYourUKDrivingLicenceProveYourIdentityGOVUKPage() {
        Assert.assertTrue(new DrivingLicencePageObject().LicenceNumber.isDisplayed());
    }
}
