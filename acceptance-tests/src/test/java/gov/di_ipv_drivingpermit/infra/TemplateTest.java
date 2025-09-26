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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("CloudFormation Template Assertions")
class TemplateTest {

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
            Map<String, Object> templateMap = yamlMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
            cloudFormationTemplate = Template.fromJSON(templateMap);
        }
        assertNotNull(cloudFormationTemplate, "CloudFormation template should be loaded.");
    }



    @Test
    @DisplayName("The template contains at least one API gateway resource")
    void shouldContainAPIResource() {
        cloudFormationTemplate.hasResource("AWS::Serverless::Api", Map.of());
        System.out.println("Assertion Passed: API gateway resource exists.");
    }

    // --- API Gateway Validation ---

    @Test
    @DisplayName("API Gateway Resource should contain correct properties Description")
    void shouldContainAPIGatewayDescription() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "Description", notNullValue()
        ));
        System.out.println("Assertion Passed: Resource exists with expected Name properties.");
    }



    @Test
    @DisplayName("API Gateway Resource should contain correct properties Name")
    void shouldContainAPIGatewayName() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "Name", notNullValue()
        ));
        System.out.println("Assertion Passed: Resource exists with expected Name properties.");
    }

    @Test
    @DisplayName("API Gateway Resource should contain correct properties Stage Name")
    void shouldContainAPIGatewayStageName() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "StageName", "Environment"
        ));
        System.out.println("Assertion Passed: Resource exists with expected StageName properties.");
    }

    @Test
    @DisplayName("API Gateway Resource should contain correct properties Tracing Enabled")
    void shouldContainAPIGatewayTracingEnabled() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "TracingEnabled", true
        ));
        System.out.println("Assertion Passed: Resource exists with expected TracingEnabled properties and values.");
    }

    @Test
    @DisplayName("API Gateway Resource should contain correct properties Endpoint Configuration")
    void shouldContainAPIGatewayStageNamed() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "EndpointConfiguration", Map.of(
                        "Type", notNullValue()
                )
        ));
        System.out.println("Assertion Passed: Resource exists with expected EndpointConfiguration properties.");
    }


    // --- Tests for DrivingPermitCheckingFunction ---

//    @Test
//    @DisplayName("DrivingPermitCheckingFunction should be a Serverless Function with properties")
//    void shouldContainDrivingPermitCheckingFunctionBasicProperties() {
//        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Function", Map.of(
//                "CodeUri", "lambdas/drivingpermitcheck",
//                "Handler", "uk.gov.di.ipv.cri.drivingpermit.api.handler.DrivingPermitHandler::handleRequest",
//                "AutoPublishAlias", "live",
//                "AutoPublishAliasAllProperties", true
//        ));
//        System.out.println("Assertion Passed: DrivingPermitCheckingFunction basic properties are correct.");
//    }

//    @Test
//    @DisplayName("DrivingPermitCheckingFunction should have correct Environment Variables")
//    void shouldContainDrivingPermitCheckingFunctionEnvironmentVariables() {
//        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Function", Map.of(
//                "Properties", Map.of(
//                        "Environment", Map.of(
//                                "Variables", Map.of(
//                                        "POWERTOOLS_SERVICE_NAME", Map.of("Fn::Sub", "${CriIdentifier}-drivingpermitcheck"),
//                                        "DVA_PERFORMANCE_STUB_IN_USE", Map.of("Fn::FindInMap", List.of("DVAPerformanceStubEnabledEnvVar", "Environment", Map.of("Ref", "Environment"))),
//                                        "LOG_DVA_RESPONSE", Map.of("Fn::FindInMap", List.of("LogDVAResponseEnvVar", "Environment", Map.of("Ref", "Environment"))),
//                                        "DVLA_PASSWORD_ROTATION_ENABLED", Map.of("Fn::FindInMap", List.of("DVLAPasswordRotationEnabledEnvVar", "Environment", Map.of("Ref", "Environment"))),
//                                        "DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG", Map.of("Fn::FindInMap", List.of("DevEnvironmentOnlyEnhancedDebugMappingEnvVar", "Environment", Map.of("Ref", "Environment"))),
//                                        "HAS_CA", Map.of("Fn::FindInMap", List.of("FeatureFlagMapping", Map.of("Ref", "Environment"), "hasCA")),
//                                        "SIGNING_CERTIFICATE_ARN", Map.of("Fn::If", List.of(
//                                                "IsCAEnvironment",
//                                                Map.of("Fn::ImportValue", "acm-infra-DLCRISigningCertificateArn"),
//                                                Map.of("Ref", "AWS::NoValue")
//                                        )),
//                                        "TLS_CERTIFICATE_ARN", Map.of("Fn::If", List.of(
//                                                "IsCAEnvironment",
//                                                Map.of("Fn::ImportValue", "acm-infra-DLCRIMtlsCertificateArn"),
//                                                Map.of("Ref", "AWS::NoValue")
//                                        )),
//                                        "SIGNING_KEY_ID", Map.of("Fn::ImportValue", "acm-infra-DvaSigningKeyId"),
//                                        "ENCRYPTION_KEY_ID", Map.of("Fn::ImportValue", "acm-infra-DvaEncryptionKeyId")
//                                )
//                        )
//                )
//        ));
//        System.out.println("Assertion Passed: DrivingPermitCheckingFunction environment variables are correct.");
//    }

//    @Test
//    @DisplayName("DrivingPermitCheckingFunction should have correct SnapStart configuration")
//    void shouldContainDrivingPermitCheckingFunctionSnapStart() {
//        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Function", Map.of(
//                "SnapStart", Map.of( // <--- Removed the outer "Properties" wrapper here
//                        "ApplyOn", List.of("Fn::FindInMap", List.of("SnapStartMapping", "Environment", Map.of("Ref", "Environment")))
//                )));
//        System.out.println("Assertion Passed: DrivingPermitCheckingFunction SnapStart configuration is correct.");
//    }


//    @Test
//    @DisplayName("DrivingPermitCheckingFunction should have correct SnapStart configuration")
//    void shouldContainDrivingPermitCheckingFunctionSnapStart() {
//        // Step 1: Use findResources to locate the specific AWS::Serverless::Function
//        // by a unique property, such as its Handler.
//        // The `findResources` method returns a Map where keys are logical IDs and values are the resource definitions.
//        // The `Map.of("Properties", Map.of("Handler", ...))` part specifies that we are looking for
//        // a resource of type "AWS::Serverless::Function" whose "Properties" block contains a "Handler" key
//        // with the specified value.
//        Map<String, Map<String, Object>> foundResources = cloudFormationTemplate.findResources(
//                "AWS::Serverless::Function",
//                Map.of("Properties", Map.of("Handler", "uk.gov.di.ipv.cri.drivingpermit.api.handler.DrivingPermitHandler::handleRequest"))
//        );
//
//        // Step 2: Assert that exactly one resource was found matching the criteria.
//        assertNotNull(foundResources, "No resources found matching the criteria for DrivingPermitCheckingFunction.");
//        assertEquals(1, foundResources.size(), "Expected exactly one DrivingPermitCheckingFunction, but found " + foundResources.size());
//
//        // Step 3: Get the logical ID of the found resource.
//        // Since we expect exactly one, we can safely get the first (and only) key.
//        String logicalId = foundResources.keySet().iterator().next();
//        assertEquals("DrivingPermitCheckingFunction", logicalId, "The found resource's logical ID does not match 'DrivingPermitCheckingFunction'.");
//
//        // Step 4: Extract the full resource definition for the identified function.
//        Map<String, Object> drivingPermitFunctionResource = foundResources.get(logicalId);
//        assertNotNull(drivingPermitFunctionResource, "DrivingPermitCheckingFunction resource definition should not be null.");
//
//        // Step 5: Assert its type (optional, but good for robustness).
//        assertEquals("AWS::Serverless::Function", drivingPermitFunctionResource.get("Type"),
//                "DrivingPermitCheckingFunction resource type should be AWS::Serverless::Function.");
//
//        // Step 6: Extract the 'Properties' block from the resource.
//        @SuppressWarnings("unchecked") // Cast is safe if template structure is as expected
//        Map<String, Object> functionProperties = (Map<String, Object>) drivingPermitFunctionResource.get("Properties");
//        assertNotNull(functionProperties, "DrivingPermitCheckingFunction should have a 'Properties' block.");
//
//        // Step 7: Extract the 'SnapStart' configuration from the 'Properties'.
//        @SuppressWarnings("unchecked")
//        Map<String, Object> snapStartConfig = (Map<String, Object>) functionProperties.get("SnapStart");
//        assertNotNull(snapStartConfig, "DrivingPermitCheckingFunction should have a 'SnapStart' configuration.");
//
//        // Step 8: Extract the 'ApplyOn' property from 'SnapStart'.
//        @SuppressWarnings("unchecked")
//        List<Object> applyOn = (List<Object>) snapStartConfig.get("ApplyOn");
//        assertNotNull(applyOn, "SnapStart configuration should have an 'ApplyOn' property.");
//
//        // Step 9: Construct the expected 'ApplyOn' value.
//        // This needs to precisely match the structure of !FindInMap [ SnapStartMapping, Environment, !Ref Environment ]
//        // when it's parsed into a Java Map/List structure by Jackson.
//        List<Object> expectedApplyOn = List.of(
//                "Fn::FindInMap",
//                List.of("SnapStartMapping", "Environment", Map.of("Ref", "Environment"))
//        );
//
////        // Step 10: Assert that the actual 'ApplyOn' matches the expected value.
////        assertEquals(expectedApplyOn, applyOn, "SnapStart 'ApplyOn' configuration does not match expected value.");
//
//        System.out.println("Assertion Passed: DrivingPermitCheckingFunction SnapStart configuration is correct.");
//    }

}
