package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;

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

    @AfterAll
    public static void cleanUp() {
        System.out.println("CleanUp after tests");
        driverClose();
    }
}
