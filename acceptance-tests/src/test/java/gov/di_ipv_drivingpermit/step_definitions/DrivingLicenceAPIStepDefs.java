package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class DrivingLicenceAPIStepDefs extends DrivingLicenceAPIPage {

    @Given(
            "Driving Licence user has the user identity in the form of a signed JWT string for CRI Id (.*) and row number (.*)$")
    public void DL_user_has_the_user_identity_in_the_form_of_a_signed_jwt_string(
            String criId, Integer LindaDuffExperianRowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        dlUserIdentityAsJwtString(criId, LindaDuffExperianRowNumber);
    }

    @And("Driving Licence user sends a POST request to session endpoint")
    public void DL_user_sends_a_post_request_to_session_end_point()
            throws IOException, InterruptedException {
        dlPostRequestToSessionEndpoint();
    }

    @And("Driving Licence user gets a session-id")
    public void DL_user_gets_a_session_id() {
        getSessionIdForDL();
    }

    @When(
            "Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest (.*)$")
    public void DL_user_sends_a_post_request_to_driving_licence_end_point(String dlJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpoint(dlJsonRequestBody);
    }

    @And("Driving Licence check response should contain Retry value as (.*)$")
    public void DL_check_response_should_contain_Retry_value(Boolean retry) {
        retryValueInDLCheckResponse(retry);
    }

    @And("Driving Licence user gets authorisation code")
    public void DL_user_gets_authorisation_code() throws IOException, InterruptedException {
        getAuthorisationCodeforDL();
    }

    @And("Driving Licence user sends a POST request to Access Token endpoint (.*)$")
    public void DL_user_requests_access_token(String CRIId)
            throws IOException, InterruptedException {
        postRequestToAccessTokenEndpointForDL(CRIId);
    }

    @Then("User requests Driving Licence CRI VC")
    public void user_requests_DL_vc() throws IOException, InterruptedException, ParseException {
        postRequestToDrivingLicenceVCEndpoint();
    }

    @And("Driving Licence VC should contain validityScore (.*) and strengthScore (.*)$")
    public void DL_vc_should_contain_validity_score_and_strength_score(
            String validityScore, String strengthScore)
            throws IOException, InterruptedException, ParseException {
        validityScoreAndStrengthScoreInVC(validityScore, strengthScore);
    }

    @And("^Driving Licence VC should contain JTI field$")
    public void jsonPayloadShouldContainJtiField()
            throws IOException, ParseException, InterruptedException {
        assertJtiIsPresentAndNotNull();
    }

    @When(
            "Driving Licence user sends a editable POST request to Driving Licence endpoint using jsonRequest (.*) with edited fields (.*)$")
    public void passport_user_sends_a_post_request_to_passport_end_point(
            String passportJsonRequestBody, String jsonEdits)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpoint(passportJsonRequestBody, jsonEdits);
    }

    @Then(
            "Check response contains unexpected server error exception containing debug error code (.*) and debug error message (.*)$")
    public void dl_debug_check_fails_and_returns_unexpected_exception(
            String cri_internal_error_code, String cri_internal_error_message) {
        checkDebugDrivingPermitResponseContainsException(
                cri_internal_error_code, cri_internal_error_message);
    }

    @And("Driving Licence VC should contain ci (.*), validityScore (.*) and strengthScore (.*)$")
    public void DL_vc_should_contain_ci_validity_score_and_strength_score(
            String ci, String validityScore, String strengthScore)
            throws IOException, InterruptedException, ParseException {
        ciInDrivingLicenceCriVc(ci);
        validityScoreAndStrengthScoreInVC(validityScore, strengthScore);
    }

    @And(
            "Driving Licence VC should contain checkMethod (.*) and identityCheckPolicy (.*) in (.*) checkDetails$")
    public void dl_vc_should_contain_check_details(
            String checkMethod, String identityCheckPolicy, String checkDetailsType)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        assertCheckDetails(checkMethod, identityCheckPolicy, checkDetailsType);
    }

    @Given("Driving Licence CRI is functioning as expected for CRI Id (.*)$")
    public void dl_is_functioning_as_expected(String criId)
            throws IOException, InterruptedException, URISyntaxException, NoSuchFieldException,
                    IllegalAccessException {
        dlUserIdentityAsJwtString(criId, 6);
        dlPostRequestToSessionEndpoint();
        getSessionIdForDL();
        postRequestToDrivingLicenceEndpoint("DVLAValidKennethJsonPayload");
    }

    @And("The secret has been created")
    public void dl_time_is_past_rotation_window() {
        getLastTestedTime();
    }

    @Then("The DVLA password should be valid and rotated within the specified window")
    public void dl_password_has_rotated() {
        passwordHasRotatedSuccessfully();
    }
}
