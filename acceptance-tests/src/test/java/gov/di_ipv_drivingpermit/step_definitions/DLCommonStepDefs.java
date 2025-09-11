package gov.di_ipv_drivingpermit.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.di_ipv_drivingpermit.pages.DLCommonPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;

public class DLCommonStepDefs extends DLCommonPageObject {

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
        assertExpectedPage(pageTitle, false);
    }

    @When("I am directed to the IPV Core routing page")
    public void i_am_directed_to_the_ipv_core_routing_page() {
        assertUserRoutedToIpvCore();
    }

    @And("I validate the URL having access denied")
    public void iValidateTheURLHavingAccessDenied() {
        assertUserRoutedToIpvCoreErrorPage();
    }

    @And("^JSON response should contain error description (.*) and status code as (.*)$")
    public void errorInJsonResponse(String testErrorDescription, String testStatusCode)
            throws JsonProcessingException {
        jsonErrorResponse(testErrorDescription, testStatusCode);
    }

    @Then("^I navigate to the Driving Licence verifiable issuer to check for a (.*) response$")
    public void i_navigate_to_driving_licence_verifiable_issuer_for_valid_response(
            String validOrInvalid) {
        navigateToDrivingLicenceResponse(validOrInvalid);
    }

    @And("^JSON payload should contain validity score (.*), strength score (.*) and type (.*)$")
    public void scoresAndTypeInVc(String validityScore, String strengthScore, String type)
            throws IOException {
        checkScoresAndTypeInStubIs(validityScore, strengthScore, type);
    }

    @And(
            "^JSON payload should contain ci (.*), validity score (.*), strength score (.*) and type (.*)$")
    public void scoreAndCiAndTypeInVc(
            String ci, String validityScore, String strengthScore, String type) throws IOException {
        new DLCommonPageObject().ciInVC(ci);
        checkScoresAndTypeInStubIs(validityScore, strengthScore, type);
    }

    @And("^JSON response should contain personal number (.*) same as given Driving Licence$")
    public void errorInJsonResponse(String personalNumber) throws IOException {
        new DLCommonPageObject().assertPersonalNumberInVc(personalNumber);
    }

    @And("JSON response should contain JTI field")
    public void JtiFieldInResponse() throws IOException {
        assertJtiPresent();
    }

    @And("^I click the Driving Licence CRI for the testEnvironment$")
    public void navigateToDrivingLicenceOnTestEnv() {
        navigateToDrivingLicenceCRIOnTestEnv();
    }
}
