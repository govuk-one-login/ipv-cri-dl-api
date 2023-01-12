package gov.di_ipv_drivingpermit.step_definitions;

import gov.di_ipv_drivingpermit.pages.UniversalSteps;
import io.cucumber.java.en.And;

public class UniversalStepDefs extends UniversalSteps {

    @And("The test is complete and I close the driver")
    public void closeDriver() {
        driverClose();
    }
}
