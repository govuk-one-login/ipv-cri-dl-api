package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.waitForPageToLoad;
import static org.junit.Assert.assertTrue;

public class UniversalSteps {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int MAX_WAIT_SEC = 60;

    public UniversalSteps() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void assertPageTitle(String expTitle, boolean fuzzy) {
        waitForPageToLoad(MAX_WAIT_SEC);

        String title = Driver.get().getTitle();
        if (title == null) {
            title = "Driver had no page title";
        }

        final boolean match = fuzzy ? title.contains(expTitle) : title.equals(expTitle);

        LOGGER.info(
                "{} match - Page title: {}, Expected {}",
                fuzzy ? "Fuzzy" : "Match",
                title,
                expTitle);

        if (!match) {
            // Log the entire page content if title match fails
            // Body logged as there are several error pages
            LOGGER.error(
                    "Error page content - : {}",
                    Driver.get().findElement(By.tagName("body")).getText());
        }

        Assert.assertTrue(match);
    }

    public void driverClose() {
        Driver.closeDriver();
    }

    public void assertURLContains(String expected) {
        waitForPageToLoad(MAX_WAIT_SEC);

        String url = Driver.get().getCurrentUrl();

        LOGGER.info("Page url: " + url);
        assertTrue(url.contains(expected));
    }

    // Method to read the JSON from a file
    public static String getJsonPayload(String fileName) {
        String filePath = "src/test/resources/Data/" + fileName + ".json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
