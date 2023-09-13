package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.en.And;

import java.util.Objects;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.changeLanguageTo;
import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.setFeatureSet;

public class UniversalStepDefs extends UniversalSteps {

    @And("The test is complete and I close the driver")
    public void closeDriver() {
        driverClose();
    }

    @And("^I add a cookie to change the language to (.*)$")
    public void iAddACookieToChangeTheLanguageToWelsh(String language) {
        changeLanguageTo(language);
    }

    @And("^I set the document checking route$")
    public void setDocumentCheckingRoute() {
        if (getProperty("cucumber.tags").equals("@direct")) {
            setFeatureSet("direct");
        }
    }

    private static String getProperty(String propertyName) {
        String property = System.getProperty(propertyName);
        return Objects.requireNonNullElse(property, "");
    }
}
