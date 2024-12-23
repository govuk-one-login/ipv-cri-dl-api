package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.After;
import io.cucumber.java.en.And;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.changeLanguageTo;

public class UniversalStepDefs extends UniversalSteps {

    @And("The test is complete and I close the driver")
    public void closeDriver() {
        driverClose();
    }

    @And("^I add a cookie to change the language to (.*)$")
    public void iAddACookieToChangeTheLanguageToWelsh(String language) {
        changeLanguageTo(language);
    }

    @After
    public void cleanUp() {
        System.out.println("CleanUp after test");
        driverClose();
    }
}
