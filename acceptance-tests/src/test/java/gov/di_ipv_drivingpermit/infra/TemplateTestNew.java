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
    private static TypeReference<?> typeRef;

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
            Map<String, Object> templateMap = yamlMapper.readValue(inputStream, new TypeReference<>() {
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
     * Helper method to get the full resource definition for all resources of a given type.
     * This includes 'Type', 'Properties', 'Condition', etc.
     *
     * @param resourceType The AWS CloudFormation resource type (e.g., "AWS::Logs::SubscriptionFilter").
     * @return A map where keys are logical IDs and values are the full resource definition map.
     * Returns an empty map if no resources of the specified type are found.
     */
    private static Map<String, Map<String, Object>> getAllResources(String resourceType) {
        Map<String, Map<String, Object>> resources = cloudFormationTemplate.findResources(resourceType);
        if (resources.isEmpty()) {
            fail("Expected at least one " + resourceType + " resource in the template.");
        }
        return resources;
    }

    /**
     * Helper method to safely retrieve a property from a map using a Class object for simpler types.
     * This version works for properties at the top-level of a resource definition (like 'Condition').
     *
     * @param sourceMap The map to retrieve the property from (e.g., the full resource definition).
     * @param propertyName The name of the property to retrieve.
     * @param expectedType The expected class type of the property.
     * @param <T> The generic type of the expected property.
     * @return An Optional containing the property value if found and of the correct type, otherwise empty.
     */
    private static <T> Optional<T> getTopLevelProperty(Map<String, Object> sourceMap, String propertyName, Class<T> expectedType) {
        if (sourceMap != null && sourceMap.containsKey(propertyName)) {
            Object value = sourceMap.get(propertyName);
            if (expectedType.isInstance(value)) {
                return Optional.of(expectedType.cast(value));
            }
        }
        return Optional.empty();
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
        TemplateTestNew.typeRef = typeRef;
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
    @DisplayName("API Gateway Resource should contain correct properties Name prefix")
    void shouldContainAPIGatewayNameWithStackNamePrefix() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");
        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean nameFoundAndValid = false;
        String expectedPrefix = "${AWS::StackName}-"; // Define the expected prefix

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            // Change: Retrieve the 'Name' property
            Optional<String> name = getProperty(properties, "Name", String.class);

            if (name.isPresent()) {
                String nameValue = name.get();
                assertNotNull(nameValue, "Name property for resource '" + logicalId + "' should not be null.");
                // Change: Validate that the name starts with the expected prefix
                assertTrue(nameValue.startsWith(expectedPrefix),
                        "Name for resource '" + logicalId + "' should start with '" + expectedPrefix + "'. Found: '" + nameValue + "'");
                nameFoundAndValid = true;
                System.out.println("Found API Gateway resource '" + logicalId + "' with valid Name: " + nameValue);
            } else {
                // Update message to reflect checking for 'Name'
                System.out.println("API Gateway resource '" + logicalId + "' does not have a 'Name' property or it's not a String.");
            }
        }

        assertTrue(nameFoundAndValid, "At least one AWS::Serverless::Api resource must have a non-null and valid 'Name' property starting with '" + expectedPrefix + "'.");
        System.out.println("Assertion Passed: All API Gateway resources checked for Name property prefix.");
    }

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
    @DisplayName("API Gateway Resource should contain correct properties Stage Name")
    void shouldContainAPIGatewayStageName() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean stageNameFoundAndValid = false;
        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> stageName = getProperty(properties, "StageName", String.class);

            if (stageName.isPresent()) {
                String descValue = stageName.get();
                assertNotNull(descValue, "StageName property for resource '" + logicalId + "' should not be null.");
                assertTrue(descValue.endsWith("Environment"),
                        "StageName for resource '" + logicalId + "' should end with 'Environment'. Found: '" + descValue + "'");
                stageNameFoundAndValid = true;
                System.out.println("Found API Gateway resource '" + logicalId + "' with valid StageName: " + descValue);
            } else {
                System.out.println("API Gateway resource '" + logicalId + "' does not have a 'StageName' property or it's not a String.");
            }
        }
        assertTrue(stageNameFoundAndValid, "At least one AWS::Serverless::Api resource must have a non-null and valid 'StageName' property.");
        System.out.println("Assertion Passed: All API Gateway resources checked for StageName property.");
    }

    @Test
    @DisplayName("API Gateway Resource should contain correct properties Tracing Enabled")
    void shouldContainAPIGatewayTracingEnabled() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean tracingenabledFoundAndValid = false;
        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<Boolean> tracingenabled = getProperty(properties, "TracingEnabled", Boolean.class);

            if (tracingenabled.isPresent()) {
                Boolean tracingEnabledValue = tracingenabled.get();
                assertNotNull(tracingEnabledValue, "TracingEnabled property for resource '" + logicalId + "' should not be null.");
                assertTrue(tracingEnabledValue, "TracingEnabled for resource '" + logicalId + "' should be 'true'. Found: '" + tracingEnabledValue + "'");
                tracingenabledFoundAndValid = true;
                System.out.println("Found API Gateway resource '" + logicalId + "' with valid TracingEnabled: " + tracingEnabledValue);
            } else {
                // Update message to reflect checking for a Boolean type
                System.out.println("API Gateway resource '" + logicalId + "' does not have a 'TracingEnabled' property or it's not a Boolean.");
            }
        }
        assertTrue(tracingenabledFoundAndValid, "At least one AWS::Serverless::Api resource must have a non-null and valid 'TracingEnabled' property.");
        System.out.println("Assertion Passed: All API Gateway resources checked for TracingEnabled property.");
    }


    @Test
    @DisplayName("API Gateway Resource should have a non-null EndpointConfiguration")
    void shouldContainAPIGatewayEndpointConfiguration() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");
        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean endpointConfigurationFoundAndValid = false;

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<Object> endpointConfiguration = getProperty(properties, "EndpointConfiguration", Object.class);

            if (endpointConfiguration.isPresent()) {
                Object configValue = endpointConfiguration.get();
                // Assert that the retrieved value is not null
                assertNotNull(configValue, "EndpointConfiguration property for resource '" + logicalId + "' should not be null.");
                endpointConfigurationFoundAndValid = true;
                System.out.println("Found API Gateway resource '" + logicalId + "' with non-null EndpointConfiguration: " + configValue);
            } else {
                System.out.println("API Gateway resource '" + logicalId + "' does not have an 'EndpointConfiguration' property or it is null.");
            }
        }

        assertTrue(endpointConfigurationFoundAndValid, "At least one AWS::Serverless::Api resource must have a non-null 'EndpointConfiguration' property.");
        System.out.println("Assertion Passed: All API Gateway resources checked for non-null EndpointConfiguration property.");
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
                // If MethodSettings is mandatory, change this to an assertion failure.
                // For now, it logs and skips the check for this specific API.
                System.out.println(String.format("API resource '%s' does not define MethodSettings or it's not a List, skipping check.", logicalId));
            }
        }
        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the MethodSettings check:\n" + failureMessages.toString());
        System.out.println("Assertion Passed: All relevant AWS::Serverless::Api resources checked for 'LoggingLevel: INFO'.");
    }

    @Test
    @DisplayName("All AWS::Serverless::Api resources with MethodSettings should contain MetricsEnabled: true")
    void allApiMethodSettingsShouldContainMetricsEnabledTrue() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertTrue(apiGatewayProperties.size() > 0, "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean allRelevantApisSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<List<Map<String, Object>>> methodSettingsOptional = getPropertyTypeReference(properties, new TypeReference<List<Map<String, Object>>>() {});

            if (methodSettingsOptional.isPresent()) {
                List<Map<String, Object>> methodSettings = methodSettingsOptional.get();

                boolean foundMetricsEnabledTrueForThisApi = false;
                for (Map<String, Object> setting : methodSettings) {
                    Optional<Boolean> metricsEnabled = getProperty(setting, "MetricsEnabled", Boolean.class);

                    if (metricsEnabled.isPresent()) {
                        if (Boolean.TRUE.equals(metricsEnabled.get())) {
                            foundMetricsEnabledTrueForThisApi = true;
                            break;
                        }
                    }
                }

                if (!foundMetricsEnabledTrueForThisApi) {
                    allRelevantApisSatisfyCondition = false;
                    failureMessages.append(String.format("API resource '%s' has MethodSettings but does not contain 'MetricsEnabled: true'.%n", logicalId));
                }
            }
        }

        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the MetricsEnabled check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Serverless::Api resources with MethodSettings should contain ThrottlingRateLimit")
    void allApiMethodSettingsShouldContainThrottlingRateLimit() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertTrue(apiGatewayProperties.size() > 0, "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean allRelevantApisSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<List<Map<String, Object>>> methodSettingsOptional = getPropertyTypeReference(properties, new TypeReference<List<Map<String, Object>>>() {});

            if (methodSettingsOptional.isPresent()) {
                List<Map<String, Object>> methodSettings = methodSettingsOptional.get();

                boolean foundThrottlingRateLimitForThisApi = false;
                for (Map<String, Object> setting : methodSettings) {
                    // Check if 'ThrottlingRateLimit' property exists and is of type Integer
                    Optional<Integer> throttlingRateLimit = getProperty(setting, "ThrottlingRateLimit", Integer.class);

                    if (throttlingRateLimit.isPresent()) {
                        foundThrottlingRateLimitForThisApi = true;
                        break; // Found it for this API, no need to check other settings
                    }
                }

                if (!foundThrottlingRateLimitForThisApi) {
                    allRelevantApisSatisfyCondition = false;
                    failureMessages.append(String.format("API resource '%s' has MethodSettings but does not contain 'ThrottlingRateLimit'.%n", logicalId));
                }
            }
        }

        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the ThrottlingRateLimit check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Serverless::Api resources with MethodSettings should contain ThrottlingBurstLimit")
    void allApiMethodSettingsShouldContainThrottlingBurstLimit() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");

        assertTrue(apiGatewayProperties.size() > 0, "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean allRelevantApisSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<List<Map<String, Object>>> methodSettingsOptional = getPropertyTypeReference(properties, new TypeReference<List<Map<String, Object>>>() {});

            if (methodSettingsOptional.isPresent()) {
                List<Map<String, Object>> methodSettings = methodSettingsOptional.get();

                boolean foundThrottlingBurstLimitForThisApi = false;
                for (Map<String, Object> setting : methodSettings) {
                    // Check if 'ThrottlingBurstLimit' property exists and is of type Integer
                    Optional<Integer> throttlingBurstLimit = getProperty(setting, "ThrottlingBurstLimit", Integer.class);

                    if (throttlingBurstLimit.isPresent()) {
                        foundThrottlingBurstLimitForThisApi = true;
                        break; // Found it for this API, no need to check other settings
                    }
                }

                if (!foundThrottlingBurstLimitForThisApi) {
                    allRelevantApisSatisfyCondition = false;
                    failureMessages.append(String.format("API resource '%s' has MethodSettings but does not contain 'ThrottlingBurstLimit'.%n", logicalId));
                }
            }
        }

        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the ThrottlingBurstLimit check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Serverless::Api resources with MethodSettings should contain DataTraceEnabled: [IsProdEnvironment, false, true]")
    void allApiMethodSettingsShouldContainDataTraceEnabled() {
        Map<String, Map<String, Object>> apiGatewayProperties = getAllResourceProperties("AWS::Serverless::Api");
        assertFalse(apiGatewayProperties.isEmpty(), "Expected at least one AWS::Serverless::Api resource in the template.");

        boolean allRelevantApisSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        List<Object> expectedDataTraceEnabledList = List.of("IsProdEnvironment", false, true);

        for (Map.Entry<String, Map<String, Object>> entry : apiGatewayProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<List<Map<String, Object>>> methodSettingsOptional = getPropertyTypeReference(properties, new TypeReference<List<Map<String, Object>>>() {});

            if (methodSettingsOptional.isPresent()) {
                List<Map<String, Object>> methodSettings = methodSettingsOptional.get();

                boolean foundDataTraceEnabledValuesForThisApi = false;
                for (Map<String, Object> setting : methodSettings) {

                    @SuppressWarnings("unchecked")
                    Optional<List<Object>> dataTraceEnabledListOptional = getProperty(setting, "DataTraceEnabled", (Class<List<Object>>)(Class<?>)List.class);

                    if (dataTraceEnabledListOptional.isPresent()) {
                        List<Object> actualDataTraceEnabledList = dataTraceEnabledListOptional.get();

                        // Compare the actual list with the expected list
                        if (expectedDataTraceEnabledList.equals(actualDataTraceEnabledList)) {
                            foundDataTraceEnabledValuesForThisApi = true;
                            break;
                        } else {
                            System.out.printf("    Mismatch for DataTraceEnabled in '%s'. Expected: %s, Actual: %s%n",
                                    logicalId, expectedDataTraceEnabledList, actualDataTraceEnabledList);
                        }
                    } else {
                        System.out.println("    'DataTraceEnabled' property not found or not a List in this MethodSetting.");
                    }
                }

                if (!foundDataTraceEnabledValuesForThisApi) {
                    allRelevantApisSatisfyCondition = false;
                    failureMessages.append(String.format("API resource '%s' has MethodSettings but does not contain 'DataTraceEnabled: %s'.%n", logicalId, expectedDataTraceEnabledList));
                } else {
                    System.out.println(String.format("API resource '%s' correctly contains 'DataTraceEnabled: %s' in its MethodSettings.", logicalId, expectedDataTraceEnabledList));
                }
            } else {
                // If MethodSettings is mandatory, you might want to change this to an assertion failure.
                // For now, it logs and skips the check for this specific API.
                System.out.println(String.format("API resource '%s' does not define MethodSettings or it's not a List, skipping check.", logicalId));
            }
        }

        assertTrue(allRelevantApisSatisfyCondition, "One or more AWS::Serverless::Api resources failed the MethodSettings check:\n" + failureMessages.toString());
        System.out.println("\nAssertion Passed: All relevant AWS::Serverless::Api resources checked for 'DataTraceEnabled: [IsProdEnvironment, false, true]'.");
        System.out.println("--- AWS::Serverless::Api MethodSettings Validation Complete ---");
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
    @DisplayName("Lambda Function Resource should contain correct properties Handler prefix")
    void lambdaHandlerShouldStartWithPrefix() {
        Map<String, Map<String, Object>> lambdaProperties = getAllResourceProperties("AWS::Serverless::Function");

        // Changed from assertFalse(lambdaFunctions.isEmpty()) to assertTrue(lambdaProperties.size() > 0)
        assertFalse(lambdaProperties.isEmpty(), "Expected at least one AWS::Serverless::Function resource.");

        boolean allHandlersMatchPrefix = true; // Changed logic to check if ALL handlers match, not just one
        String expectedPrefix = "uk.gov.di.ipv.cri.";
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


    // Logs - Log Group Resource Tests

    @Test
    @DisplayName("All AWS::Logs::LogGroup resources should contain a LogGroupName")
    void allLogGroupResourcesShouldContainLogGroupName() {
        Map<String, Map<String, Object>> logGroupProperties = getAllResourceProperties("AWS::Logs::LogGroup");

        assertFalse(logGroupProperties.isEmpty(), "Expected at least one AWS::Logs::LogGroup resource in the template.");

        boolean allLogGroupsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : logGroupProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> logGroupName = getProperty(properties, "LogGroupName", String.class);

            if (!logGroupName.isPresent()) {
                allLogGroupsSatisfyCondition = false;
                failureMessages.append(String.format("LogGroup resource '%s' does not have a 'LogGroupName' property or it's not a String.%n", logicalId));
            }
        }

        assertTrue(allLogGroupsSatisfyCondition, "One or more AWS::Logs::LogGroup resources failed the LogGroupName existence check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Logs::LogGroup resources should contain a RetentionInDays")
    void allLogGroupResourcesShouldContainRetentionInDays() {
        Map<String, Map<String, Object>> logGroupProperties = getAllResourceProperties("AWS::Logs::LogGroup");

        assertFalse(logGroupProperties.isEmpty(), "Expected at least one AWS::Logs::LogGroup resource in the template.");

        boolean allLogGroupsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : logGroupProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            // Retrieve the 'RetentionInDays' property, typically an Integer
            Optional<String> retentionInDays = getProperty(properties, "RetentionInDays", String.class);

            if (retentionInDays.isEmpty()) { // Check if the property is NOT present
                allLogGroupsSatisfyCondition = false;
                failureMessages.append(String.format("LogGroup resource '%s' does not have a 'RetentionInDays' property or it's not an Integer.%n", logicalId));
            }
        }

        assertTrue(allLogGroupsSatisfyCondition, "One or more AWS::Logs::LogGroup resources failed the RetentionInDays existence check:\n" + failureMessages.toString());
    }

    // Logs - Subscription Filter Resource Tests

    @Test
    @DisplayName("All AWS::Logs::SubscriptionFilter resources should contain Condition: LogSendingEnabled")
    void allSubscriptionFilterResourcesShouldContainLogSendingEnabledCondition() {
        Map<String, Map<String, Object>> subscriptionFilterResources = getAllResources("AWS::Logs::SubscriptionFilter");

        assertFalse(subscriptionFilterResources.isEmpty(), "Expected at least one AWS::Logs::SubscriptionFilter resource in the template.");

        boolean allSubscriptionFiltersSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : subscriptionFilterResources.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> resourceDefinition = entry.getValue();

            Optional<String> condition = getTopLevelProperty(resourceDefinition, "Condition", String.class);

            if (condition.isPresent()) {
                String conditionValue = condition.get();
                if (!"LogSendingEnabled".equals(conditionValue)) {
                    allSubscriptionFiltersSatisfyCondition = false;
                    failureMessages.append(String.format("SubscriptionFilter resource '%s' has Condition '%s' but expected 'LogSendingEnabled'.%n", logicalId, conditionValue));
                }
            } else {
                allSubscriptionFiltersSatisfyCondition = false;
                failureMessages.append(String.format("SubscriptionFilter resource '%s' does not have a 'Condition' property or it's not a String.%n", logicalId));
            }
        }

        assertTrue(allSubscriptionFiltersSatisfyCondition, "One or more AWS::Logs::SubscriptionFilter resources failed the Condition check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Logs::SubscriptionFilter resources should contain a LogGroupName")
    void allSubscriptionFilterResourcesShouldContainLogGroupName() {
        Map<String, Map<String, Object>> logGroupProperties = getAllResourceProperties("AWS::Logs::SubscriptionFilter");

        assertFalse(logGroupProperties.isEmpty(), "Expected at least one AWS::Logs::SubscriptionFilter resource in the template.");

        boolean allLogGroupsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : logGroupProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> logGroupName = getProperty(properties, "LogGroupName", String.class);

            if (!logGroupName.isPresent()) {
                allLogGroupsSatisfyCondition = false;
                failureMessages.append(String.format("LogGroup resource '%s' does not have a 'LogGroupName' property or it's not a String.%n", logicalId));
            }
        }

        assertTrue(allLogGroupsSatisfyCondition, "One or more AWS::Logs::SubscriptionFilter resources failed the LogGroupName existence check:\n" + failureMessages.toString());
    }

    @Test
    @DisplayName("All AWS::Logs::SubscriptionFilter resources should contain DestinationArn starting with 'arn:aws:logs:eu-west-2:'")
    void allSubscriptionFilterResourcesShouldContainDestinationArnWithPrefix() {
        Map<String, Map<String, Object>> subscriptionFilterProperties = getAllResourceProperties("AWS::Logs::SubscriptionFilter");

        assertFalse(subscriptionFilterProperties.isEmpty(), "Expected at least one AWS::Logs::SubscriptionFilter resource in the template.");

        boolean allSubscriptionFiltersSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedPrefix = "arn:aws:logs:eu-west-2:";

        for (Map.Entry<String, Map<String, Object>> entry : subscriptionFilterProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            // Retrieve the 'DestinationArn' property from the 'Properties' map
            Optional<String> destinationArn = getProperty(properties, "DestinationArn", String.class);

            if (destinationArn.isPresent()) {
                String destinationArnValue = destinationArn.get();
                if (!destinationArnValue.startsWith(expectedPrefix)) {
                    allSubscriptionFiltersSatisfyCondition = false;
                    failureMessages.append(String.format("SubscriptionFilter resource '%s' has DestinationArn '%s' which does not start with '%s'.%n",
                            logicalId, destinationArnValue, expectedPrefix));
                }
            } else {
                allSubscriptionFiltersSatisfyCondition = false;
                failureMessages.append(String.format("SubscriptionFilter resource '%s' does not have a 'DestinationArn' property or it's not a String.%n", logicalId));
            }
        }

        assertTrue(allSubscriptionFiltersSatisfyCondition, "One or more AWS::Logs::SubscriptionFilter resources failed the DestinationArn prefix check:\n" + failureMessages.toString());
    }


    // Lambda - Permission Resource Tests


}
