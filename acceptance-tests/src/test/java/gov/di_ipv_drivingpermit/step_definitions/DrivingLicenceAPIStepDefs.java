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
    public void DL_user_sends_a_post_request_to_driving_licence_end_point(
            String dlJsonRequestBody, String documentCheckingRoute)
            throws IOException, InterruptedException {
        postRequestToDrivingLicenceEndpoint(dlJsonRequestBody, documentCheckingRoute);
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
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        validityScoreAndStrengthScoreInVC(validityScore, strengthScore);
    }
}
