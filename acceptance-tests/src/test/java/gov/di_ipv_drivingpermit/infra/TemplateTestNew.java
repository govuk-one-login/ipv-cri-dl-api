package gov.di_ipv_drivingpermit.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.assertions.Template;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("CloudFormation Template Assertions")
class TemplateTestNew {

    private static Template cloudFormationTemplate;

    /**
     * This method runs once before all tests to load and parse the CloudFormation template.
     */
    @BeforeAll
    static void setup() throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Path templateFilePath = Paths.get("..", "infrastructure", "lambda", "template.yaml").normalize();
        System.out.println("Attempting to load template from: " + templateFilePath.toAbsolutePath()); // Added for debugging

        try (InputStream inputStream = new FileInputStream(templateFilePath.toFile())) {
            if (inputStream == null) {
                throw new IOException("CloudFormation template.yaml not found at " + templateFilePath.toAbsolutePath());
            }
            Map<String, Object> templateMap = yamlMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {
            });
            cloudFormationTemplate = Template.fromJSON(templateMap);
        }
        assertNotNull(cloudFormationTemplate, "CloudFormation template should be loaded.");
    }


    /**
     * Helper method to get the 'Properties' map for all resources of a given type.
     *
     * @param resourceType The AWS CloudFormation resource type (e.g., "AWS::Serverless::Api").
     * @return A map where keys are logical IDs and values are the 'Properties' map of each resource.
     * Returns an empty map if no resources of the specified type are found.
     * Asserts that 'Properties' map is not null for any found resource.
     */
    private static Map<String, Map<String, Object>> getAllResourceProperties(String resourceType) {
        Map<String, Map<String, Object>> resources = cloudFormationTemplate.findResources(resourceType);
        Map<String, Map<String, Object>> propertiesMap = new HashMap<>();

        if (resources.isEmpty()) {
            fail("Expected at least one " + resourceType + " resource in the template.");
            return propertiesMap;
        }

        for (Map.Entry<String, Map<String, Object>> entry : resources.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> resource = entry.getValue();

            @SuppressWarnings("unchecked") // Cast is safe if template structure is as expected
            Map<String, Object> properties = (Map<String, Object>) resource.get("Properties");

            assertNotNull(properties, "Properties map should not be null for resource: " + logicalId + " of type " + resourceType);
            propertiesMap.put(logicalId, properties);
        }
        return propertiesMap;
    }

    /**
     * Helper method to safely retrieve a property from a map using TypeReference for complex generic types.
     *
     * @param <T>       The generic type of the expected property.
     * @param sourceMap The map to retrieve the property from.
     * @param typeRef   The TypeReference capturing the full generic type information.
     * @return An Optional containing the property value if found and of the correct type, otherwise empty.
     */
    private static <T> Optional<T> getPropertyTypeReference(Map<String, Object> sourceMap, TypeReference<T> typeRef) {
        if (sourceMap != null && sourceMap.containsKey("MethodSettings")) {
            Object value = sourceMap.get("MethodSettings");
            try {
                @SuppressWarnings("unchecked") // This unchecked cast is necessary here
                T castValue = (T) value;
                return Optional.of(castValue);
            } catch (ClassCastException e) {
                // Log or handle if the type doesn't match at runtime
                System.err.println("Type mismatch for property '" + "MethodSettings" + "': " + e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    /**
     * Helper method to safely retrieve a property from a map using a Class object for simpler types.
     * (Keep this if you still use it for non-generic types like String, Integer, etc.)
     *
     * @param sourceMap The map to retrieve the property from.
     * @param propertyName The name of the property to retrieve.
     * @param expectedType The expected class type of the property.
     * @param <T> The generic type of the expected property.
     * @return An Optional containing the property value if found and of the correct type, otherwise empty.
     */
    private static <T> Optional<T> getProperty(Map<String, Object> sourceMap, String propertyName, Class<T> expectedType) {
        if (sourceMap != null && sourceMap.containsKey(propertyName)) {
            Object value = sourceMap.get(propertyName);
            if (expectedType.isInstance(value)) {
                return Optional.of(expectedType.cast(value));
            }
        }
        return Optional.empty();
    }

    // API Gateway Resource Tests
    @Test
    @DisplayName("API Gateway Resource should contain correct properties Description")
    void shouldContainAPIGatewayDescription() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean descriptionFoundAndValid = false;
        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> description = getProperty(properties, "Description", String.class);

            if (description.isPresent()) {
                String descValue = description.get();
                assertNotNull(descValue, "Description property for resource '" + logicalId + "' should not be null.");
                assertTrue(descValue.endsWith("Driving Permit CRI API"),
                        "Description for resource '" + logicalId + "' should end with 'Driving Permit CRI API'. Found: '" + descValue + "'");
                descriptionFoundAndValid = true;
                System.out.println("Found API Gateway resource '" + logicalId + "' with valid Description: " + descValue);
            } else {
                System.out.println("API Gateway resource '" + logicalId + "' does not have a 'Description' property or it's not a String.");
            }
        }
        assertTrue(descriptionFoundAndValid, "At least one AWS::Serverless::Api resource must have a non-null and valid 'Description' property.");
        System.out.println("Assertion Passed: All API Gateway resources checked for Description property.");
    }


    @Test
    @DisplayName("All AWS::Serverless::Api resources with MethodSettings should contain LoggingLevel: INFO")
    void allApiMethodSettingsShouldContainLoggingLevelInfo() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertTrue(apiGatewayProperties.size() > 0, "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean allRelevantApisSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            // Using TypeReference to correctly capture the generic type List<Map<String, Object>>
            Optional<List<Map<String, Object>>> methodSettingsOptional =
                    getPropertyTypeReference(properties, new TypeReference<List<Map<String, Object>>>() {});

            if (methodSettingsOptional.isPresent()) {
                List<Map<String, Object>> methodSettings = methodSettingsOptional.get();
                boolean foundLoggingLevelInfoForThisApi = false;

                for (Map<String, Object> setting : methodSettings) {
                    // This call uses the simpler getProperty(Map, String, Class) for String type
                    Optional<String> loggingLevel = getProperty(setting, "LoggingLevel", String.class);
                    if (loggingLevel.isPresent() && "INFO".equals(loggingLevel.get())) {
                        foundLoggingLevelInfoForThisApi = true;
                        break;
                    }
                }

                if (!foundLoggingLevelInfoForThisApi) {
                    allRelevantApisSatisfyCondition = false;
                    failureMessages.append(String.format("API resource '%s' has MethodSettings but does not contain 'LoggingLevel: INFO'.%n", logicalId));
                } else {
                    System.out.println(String.format("API resource '%s' correctly contains 'LoggingLevel: INFO' in its MethodSettings.", logicalId));
                }
            } else {
                // If MethodSettings is mandatory, you might want to change this to an assertion failure.
                // For now, it logs and skips the check for this specific API.
                System.out.println(String.format("API resource '%s' does not define MethodSettings or it's not a List, skipping check.", logicalId));
            }
        }
        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the MethodSettings check:\n" + failureMessages.toString());
        System.out.println("Assertion Passed: All relevant AWS::Serverless::Api resources checked for 'LoggingLevel: INFO'.");
    }

    // Lambda Function Resource Tests

    // Test Fails - We have MemorySize set as a Global, however it is not used within any of the functions
    @Test
    @DisplayName("Lambda Functions should have a 'MemorySize' property")
    void lambdaFunctionsShouldHaveMemorySize() {
        Map<String, Map<String, Object>> lambdaProperties = getAllResourceProperties("AWS::Serverless::Function");

        assertFalse(lambdaProperties.isEmpty(), "Expected at least one AWS::Serverless::Function resource in the template.");

        for (Map.Entry<String, Map<String, Object>> entry : lambdaProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<Integer> memorySize = getProperty(properties, "MemorySize", Integer.class);

            assertTrue(memorySize.isPresent(), "Lambda function '" + logicalId + "' must have a 'MemorySize' property.");
            assertTrue(memorySize.get() > 0, "MemorySize for Lambda function '" + logicalId + "' must be a positive integer.");
            System.out.println("Lambda function '" + logicalId + "' has MemorySize: " + memorySize.get());
        }
        System.out.println("Assertion Passed: All Lambda functions checked for 'MemorySize' property.");
    }

    @Test
    @DisplayName("Lambda Function Handler should start with a specific prefix")
    void lambdaHandlerShouldStartWithPrefix1() {
        Map<String, Map<String, Object>> lambdaProperties = getAllResourceProperties("AWS::Serverless::Function");

        // Changed from assertFalse(lambdaFunctions.isEmpty()) to assertTrue(lambdaProperties.size() > 0)
        assertFalse(lambdaProperties.isEmpty(), "Expected at least one AWS::Serverless::Function resource.");

        boolean allHandlersMatchPrefix = true; // Changed logic to check if ALL handlers match, not just one
        String expectedPrefix = "uk.gov.di.ipv.cri.drivingpermit.";
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : lambdaProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> handlerOptional = getProperty(properties, "Handler", String.class);

            if (handlerOptional.isPresent()) {
                String handler = handlerOptional.get();
                if (!handler.startsWith(expectedPrefix)) {
                    allHandlersMatchPrefix = false;
                    failureMessages.append(String.format("Lambda function '%s' Handler '%s' does not start with '%s'.%n", logicalId, handler, expectedPrefix));
                } else {
                    System.out.println("Found Lambda function '" + logicalId + "' with Handler: " + handler + " (starts with '" + expectedPrefix + "')");
                }
            } else {
                allHandlersMatchPrefix = false;
                failureMessages.append(String.format("Lambda function '%s' is missing the mandatory 'Handler' property or it's not a String.%n", logicalId));
            }
        }
        assertTrue(allHandlersMatchPrefix, "One or more Lambda function handlers failed the prefix check:\n" + failureMessages.toString());
        System.out.println("Assertion Passed: All relevant Lambda functions checked for Handler prefix.");
    }
}
