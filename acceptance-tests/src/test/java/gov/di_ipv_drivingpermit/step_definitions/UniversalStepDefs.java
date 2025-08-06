package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import java.io.IOException;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.changeLanguageTo;

public class UniversalStepDefs extends UniversalSteps {

    @And("The test is complete and I close the driver")
    public void closeDriver() {
        // Intended
        System.out.println("closeDriver() ignored");
    }

    @And("^I add a cookie to change the language to (.*)$")
    public void iAddACookieToChangeTheLanguageTo(String language) {
        String languageCode = changeLanguageTo(language);
        assertURLContains("?lng=" + languageCode);
    }

    @Given("User sends a GET request to the well-known jwks endpoint")
    public void user_sends_a_get_request_to_well_known_jwks_end_point()
            throws IOException, InterruptedException {
        getRequestToJwksEndpoint();
    }

    @And(
            "User sends a basic POST request to public (.*) endpoint without apiKey they get a forbidden error$")
    public void user_sends_basic_post_to_public_endpoint(String endpoint)
            throws IOException, InterruptedException {
        postRequestToPublicApiEndpointWithoutApiKey(endpoint);
    }

    @AfterAll
    public static void cleanUp() {
        System.out.println("CleanUp after tests");
        driverClose();
    }
}
