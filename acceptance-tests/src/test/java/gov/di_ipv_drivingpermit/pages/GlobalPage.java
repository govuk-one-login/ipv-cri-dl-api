package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.PageObjectSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GlobalPage extends PageObjectSupport {

    WebDriver driver;

    public GlobalPage() {
        this.driver = getCurrentDriver();
    }

    public void populateField(By selector, String value) {
        waitForElementVisible(selector, 60);
        WebElement field = getCurrentDriver().findElement(selector);
        field.sendKeys(value);
    }
}
