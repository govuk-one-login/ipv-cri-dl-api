package gov.di_ipv_drivingpermit.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/** reads the properties file configuration.properties */
public class ConfigurationReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);

    private static Properties properties;

    static {
        try {
            String path = "configuration.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);

            input.close();
        } catch (Exception e) {
            LOGGER.error("Error: ", e);
        }
    }

    public static String get(String keyName) {
        return properties.getProperty(keyName);
    }

    public static String getBrowser() {
        return System.getenv("BROWSER") != null ? System.getenv("BROWSER") : "chrome";
    }

    public static boolean noChromeSandbox() {
        return "true".equalsIgnoreCase(System.getenv("NO_CHROME_SANDBOX"));
    }
}
