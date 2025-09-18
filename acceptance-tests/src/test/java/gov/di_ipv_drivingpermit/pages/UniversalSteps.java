package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.waitForSpecificPageWithTitleToFullyLoad;
import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.waitForUrlToContain;
import static org.junit.Assert.assertTrue;

public class UniversalSteps {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private final ConfigurationService configurationService =
            new ConfigurationService(System.getenv("ENVIRONMENT"));

    private static final Logger LOGGER = LogManager.getLogger();

    public static final int MAX_WAIT_SEC = 15;

    public UniversalSteps() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void assertExpectedPage(String expectedTitle, boolean exactMatch) {
        boolean success =
                waitForSpecificPageWithTitleToFullyLoad(expectedTitle, exactMatch, MAX_WAIT_SEC);

        String title = Driver.get().getTitle();
        if (title == null) {
            title = "Driver had no page title";
        }

        LOGGER.info(
                "Page title is: {}, Expected {} - ComparingWithExactMatch {}",
                title,
                expectedTitle,
                exactMatch);

        assertTrue(success);
    }

    public static void driverClose() {
        Driver.closeDriver();
    }

    public void assertURLContains(String expected) {
        boolean status = waitForUrlToContain(expected, MAX_WAIT_SEC);

        String url = Driver.get().getCurrentUrl();
        LOGGER.info("Page url: " + url);

        assertTrue(status);
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

    private HttpResponse<String> sendHttpRequest(HttpRequest request)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public void getRequestToJwksEndpoint() throws IOException, InterruptedException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        LOGGER.info("getPublicAPIEndpoint() ==> {}", publicApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/.well-known/jwks.json"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .GET()
                        .build();
        String wellKnownJWKSResponse = sendHttpRequest(request).body();
        LOGGER.info("wellKnownJWKSResponse = {}", wellKnownJWKSResponse);

        try {
            JsonNode rootNode = objectMapper.readTree(wellKnownJWKSResponse);
            JsonNode keysNode = rootNode.path("keys").get(0);

            // Assertions for each key-value pair
            Assertions.assertTrue(keysNode.has("kty"), "kty field is missing");
            Assertions.assertEquals("RSA", keysNode.path("kty").asText(), "kty value is incorrect");
            Assertions.assertTrue(keysNode.has("n"), "n field is missing");
            Assertions.assertTrue(keysNode.has("e"), "e field is missing");
            Assertions.assertEquals("AQAB", keysNode.path("e").asText(), "e value is incorrect");
            Assertions.assertTrue(keysNode.has("use"), "use field is missing");
            Assertions.assertEquals("enc", keysNode.path("use").asText(), "use value is incorrect");
            Assertions.assertTrue(keysNode.has("kid"), "kid field is missing");
            Assertions.assertTrue(keysNode.has("alg"), "alg field is missing");
            Assertions.assertEquals(
                    "RSA-OAEP-256", keysNode.path("alg").asText(), "alg value is incorrect");

        } catch (IOException e) {
            LOGGER.error("Error parsing JSON response: {}", e.getMessage());
            // Handle the exception appropriately, e.g., throw a custom exception or fail the test
            Assertions.fail("Error parsing JSON response: " + e.getMessage()); // Fail the test
        } catch (NullPointerException e) {
            LOGGER.error("Error accessing JSON node: {}", e.getMessage());
            Assertions.fail("Error accessing JSON node: " + e.getMessage()); // Fail the test
        }
    }

    public void postRequestToPublicApiEndpointWithoutApiKey(String endpoint)
            throws IOException, InterruptedException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        LOGGER.info("getPublicAPIEndpoint() ==> " + publicApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + endpoint))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
        String publicAPIGatewayEndpointPostResponse = sendHttpRequest(request).body();
        LOGGER.info(
                "publicAPIGatewayEndpointPostResponse = " + publicAPIGatewayEndpointPostResponse);
        try {
            ObjectMapper objectMapper =
                    new ObjectMapper(); // Assuming you have ObjectMapper defined elsewhere
            JsonNode rootNode = objectMapper.readTree(publicAPIGatewayEndpointPostResponse);

            // Assertion for the expected error message
            Assertions.assertEquals(
                    "Forbidden",
                    rootNode.path("message").asText(),
                    "Unexpected error message received");

        } catch (IOException e) {
            LOGGER.error("Error parsing JSON response: {}", e.getMessage());
            Assertions.fail(
                    "Error parsing JSON response: "
                            + e.getMessage()); // Fail the test if parsing fails
        } catch (NullPointerException e) {
            LOGGER.error("Error accessing JSON node: {}", e.getMessage());
            Assertions.fail(
                    "Error accessing JSON node: "
                            + e.getMessage()); // Fail the test if node is missing
        }
    }

    public JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        return jsonNode.get(vc);
    }
}
