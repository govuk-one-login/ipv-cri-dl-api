package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.changeLanguageTo;
import static gov.di_ipv_drivingpermit.utilities.Driver.closeAllDrivers;

public class UniversalStepDefs extends UniversalSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniversalStepDefs.class);
    private static final int DELAY_BETWEEN_SCENARIOS_MS = 500;

    @After("@stub or @uat or @traffic or @smoke")
    @SuppressWarnings("java:S2925")
    public void waitBetweenScenarios() {
        try {
            // We want to wait between tests enough to not overload smaller F.E
            // But not so long we push the test duration up
            LOGGER.info(
                    "Waiting {}ms between scenarios to avoid overloading F.E.",
                    DELAY_BETWEEN_SCENARIOS_MS);
            Thread.sleep(DELAY_BETWEEN_SCENARIOS_MS);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    @And("^I add a cookie to change the language to (.*)$")
    public void updateCookieToChangeTheLanguageTo(String language) {
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
        closeAllDrivers();
    }
}
