package gov.di_ipv_drivingpermit.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        features = "src/test/resources/features",
        glue = "gov/di_ipv_drivingpermit/step_definitions",
        dryRun = false)
public class TestRunner {}
