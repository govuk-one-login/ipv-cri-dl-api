package gov.di_ipv_drivingpermit.utilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

public class Driver {
    // driver class will provide separate webdriver object per thread
    private static final InheritableThreadLocal<WebDriver> driverPool =
            new InheritableThreadLocal<>();

    // InheritableThreadLocal  --> this is like a container, bag, pool.
    // in this pool we can have separate objects for each thread
    // for each thread, in InheritableThreadLocal we can have separate object for that thread

    private Driver() {}

    public static WebDriver get() {
        // if this thread doesn't have driver - create it and add to pool
        if (driverPool.get() == null) {

            String browser = ConfigurationReader.getBrowser();
            System.setProperty("webdriver.chrome.logfile", "chromedriver.log");
            System.setProperty("webdriver.chrome.verboseLogging", "true");

            switch (browser) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions1 = new ChromeOptions();
                    chromeOptions1.addArguments("--remote-allow-origins=*");
                    driverPool.set(new ChromeDriver(chromeOptions1));
                    break;
                case "chrome-headless":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--remote-allow-origins=*");
                    chromeOptions.addArguments("--headless");

                    if (ConfigurationReader.noChromeSandbox()) {
                        // no-sandbox is needed for chrome-headless when running in a container due
                        // to restricted syscalls
                        chromeOptions.addArguments("--no-sandbox");
                        chromeOptions.addArguments("--whitelisted-ips= ");
                        chromeOptions.addArguments("--disable-dev-shm-usage");
                        chromeOptions.addArguments("--remote-debugging-port=9222");

                        chromeOptions.addArguments("start-maximized");
                        chromeOptions.addArguments("disable-infobars");
                        chromeOptions.addArguments("--disable-extensions");
                    }
                    driverPool.set(new ChromeDriver(chromeOptions));
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driverPool.set(new FirefoxDriver());
                    break;
                case "firefox-headless":
                    WebDriverManager.firefoxdriver().setup();

                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.addArguments("-headless");

                    driverPool.set(new FirefoxDriver(firefoxOptions));
                    break;
                case "ie":
                    if (!System.getProperty("os.name").toLowerCase().contains("windows"))
                        throw new WebDriverException("Your OS doesn't support Internet Explorer");
                    WebDriverManager.iedriver().setup();
                    driverPool.set(new InternetExplorerDriver());
                    break;

                case "edge":
                    if (!System.getProperty("os.name").toLowerCase().contains("windows"))
                        throw new WebDriverException("Your OS doesn't support Edge");
                    WebDriverManager.edgedriver().setup();
                    driverPool.set(new EdgeDriver());
                    break;

                case "safari":
                    if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                        throw new WebDriverException("Your OS doesn't support Safari");
                    WebDriverManager.getInstance(SafariDriver.class).setup();
                    driverPool.set(new SafariDriver());
                    break;
            }
        }

        return driverPool.get();
    }

    public static void closeDriver() {
        if (driverPool.get() != null) {
            driverPool.get().close();
            driverPool.get().quit();
            driverPool.remove();
        }
    }
}
