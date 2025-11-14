package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.DrivingLicenceAPIPage;
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
            String criId, Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        dlUserIdentityAsJwtString(criId, rowNumber);
    }

    @Given(
            "DVA Driving Licence with a signed JWT string with (.*), (.*), (.*), (.*), (.*) and (.*) for CRI Id (.*) and JSON Shared Claims (.*)$")
    public void
            DVA_DL_user_has_the_user_identity_in_the_form_of_a_signed_jwt_string_with_check_details_flag(
                    String context,
                    String drivingPermitPersonalNumber,
                    String drivingPermitExpiryDate,
                    String drivingPermitIssueDate,
                    String drivingPermitIssuedBy,
                    String drivingPermitFullAddress,
                    String criId,
                    Integer rowNumber)
                    throws URISyntaxException, IOException, InterruptedException {
        dlUserIdentityDVAWithDrivingPermitAsJwtString(
                context,
                drivingPermitPersonalNumber,
                drivingPermitExpiryDate,
                drivingPermitIssueDate,
                drivingPermitIssuedBy,
                drivingPermitFullAddress,
                criId,
                rowNumber);
    }

    @Given(
            "DVLA Driving Licence with a signed JWT string with (.*), (.*), (.*), (.*), (.*), (.*) and (.*) for CRI Id (.*) and JSON Shared Claims (.*)$")
    public void
            DVLA_DL_user_has_the_user_identity_in_the_form_of_a_signed_jwt_string_with_check_details_flag(
                    String context,
                    String drivingPermitPersonalNumber,
                    String drivingPermitExpiryDate,
                    String drivingPermitIssueDate,
                    String drivingPermitIssueNumber,
                    String drivingPermitIssuedBy,
                    String drivingPermitFullAddress,
                    String criId,
                    Integer rowNumber)
                    throws URISyntaxException, IOException, InterruptedException {
        dlUserIdentityDVLAWithDrivingPermitAsJwtString(
                context,
                drivingPermitPersonalNumber,
                drivingPermitExpiryDate,
                drivingPermitIssueDate,
                drivingPermitIssueNumber,
                drivingPermitIssuedBy,
                drivingPermitFullAddress,
                criId,
                rowNumber);
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

    @And("Driving Licence user sends a GET request to the personInfo endpoint")
    public void DL_user_sends_a_get_request_to_person_info_end_point()
            throws IOException, InterruptedException {
        dlGetRequestToPersonInfoEndpoint();
    }

    @When(
            "Driving Licence user sends a POST request to Driving Licence endpoint using jsonRequest (.*)$")
    public void DL_user_sends_a_post_request_to_driving_licence_end_point(String dlJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpoint(dlJsonRequestBody);
    }

    @When(
            "Driving Licence user sends a POST request to Driving Licence endpoint using updated jsonRequest returned from the personInfo Table (.*)$")
    public void DL_user_sends_a_post_request_to_driving_licence_end_point_with_person_info_details(
            String dlJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpointWithPersonInfoDetails(dlJsonRequestBody, "");
    }

    @When(
            "Driving Licence user sends a POST request to Driving Licence endpoint with a invalid (.*) value using jsonRequest (.*)$")
    public void DL_user_sends_a_post_request_to_driving_licence_end_point_with_invalid_session_id(
            String invalidHeaderValue, String dlJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpointWithInvalidSessionId(
                invalidHeaderValue, dlJsonRequestBody);
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
    public void DL_user_requests_access_token(String criId)
            throws IOException, InterruptedException {
        postRequestToAccessTokenEndpointForDL(criId);
    }

    @Then("User requests Driving Licence CRI VC")
    public void user_requests_DL_vc() throws IOException, InterruptedException, ParseException {
        postRequestToDrivingLicenceVCEndpoint();
    }

    @Then(
            "User requests Driving Licence CRI VC from the Credential Issuer Endpoint with a invalid Bearer Token value")
    public void user_requests_DL_vc_with_invalid_headers()
            throws IOException, InterruptedException {
        postRequestToDrivingLicenceVCEndpointWithInvalidAuthCode();
    }

    @And("Driving Licence VC should contain validityScore (.*) and strengthScore (.*)$")
    public void DL_vc_should_contain_validity_score_and_strength_score(
            String validityScore, String strengthScore) throws IOException {
        validityScoreAndStrengthScoreInVC(validityScore, strengthScore);
    }

    @And("^Driving Licence VC should contain JTI field$")
    public void jsonPayloadShouldContainJtiField() throws IOException {
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
            String criInternalErrorCode, String criInternalErrorMessage) {
        checkDebugDrivingPermitResponseContainsException(
                criInternalErrorCode, criInternalErrorMessage);
    }

    @And("Driving Licence VC should contain ci (.*), validityScore (.*) and strengthScore (.*)$")
    public void DL_vc_should_contain_ci_validity_score_and_strength_score(
            String ci, String validityScore, String strengthScore) throws IOException {
        ciInDrivingLicenceCriVc(ci);
        validityScoreAndStrengthScoreInVC(validityScore, strengthScore);
    }

    @And(
            "Driving Licence VC should contain checkMethod (.*) and identityCheckPolicy (.*) in (.*) checkDetails$")
    public void dl_vc_should_contain_check_details(
            String checkMethod, String identityCheckPolicy, String checkDetailsType)
            throws IOException {
        assertCheckDetails(checkMethod, identityCheckPolicy, checkDetailsType);
    }

    @Given("Driving Licence CRI is functioning as expected for CRI Id (.*)$")
    public void dl_is_functioning_as_expected(String criId)
            throws IOException,
                    InterruptedException,
                    URISyntaxException,
                    NoSuchFieldException,
                    IllegalAccessException {
        dlUserIdentityAsJwtString(criId, 6);
        dlPostRequestToSessionEndpoint();
        getSessionIdForDL();
        postRequestToDrivingLicenceEndpoint("DVLAValidKennethJsonPayload");
    }

    @And("The secret has been created")
    public void dl_time_is_past_rotation_window() {
        getLastTestedTime("DVLA/password");
    }

    @Then("The DVLA password should be valid and rotated within the specified window")
    public void dl_password_has_rotated() {
        passwordHasRotatedSuccessfully();
    }

    @And("The DVLA API Key secret has been created")
    public void dvla_api_key_time_is_past_rotation_window() {
        getLastTestedTime("DVLA/apiKey");
    }

    @Then("The DVLA API Key should be valid and rotated within the specified window")
    public void dvla_api_key_has_rotated() {
        apiKeyHasRotatedSuccessfully();
    }
}
