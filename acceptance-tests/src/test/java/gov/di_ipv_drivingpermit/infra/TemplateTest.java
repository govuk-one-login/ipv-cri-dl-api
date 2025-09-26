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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CloudFormation Template Assertions")
class TemplateTest {

    private static Template cloudFormationTemplate;

    /**
     * This method runs once before all tests to load and parse the CloudFormation template.
     * It now loads 'template.yaml' from the 'infrastructure/lambda/' directory relative to the project root.
     */
    @BeforeAll
    static void setup() throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        // Construct the path to the template.yaml file
        // Navigate up one directory from the current working directory (acceptance-tests)
        // then down into infrastructure/lambda
        Path templateFilePath = Paths.get(System.getProperty("user.dir"), "..", "infrastructure", "lambda", "template.yaml").normalize();
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
    @DisplayName("The template contains one API gateway resource")
    void shouldContainAPIResource() {
        cloudFormationTemplate.hasResource("AWS::Serverless::Api", Map.of());
        System.out.println("Assertion Passed: API gateway resource exists.");
    }

    @Test
    @DisplayName("Should contain PublicDrivingPermitApi with correct properties")
    void shouldContainPublicDrivingPermitApi() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "Name", "${AWS::StackName}-PublicDLApi",
                "StageName", "Environment",
                "TracingEnabled", true,
                "EndpointConfiguration", Map.of("Type", "REGIONAL")
        ));
        System.out.println("Assertion Passed: PublicDrivingPermitApi exists with expected properties.");
    }

    @Test
    @DisplayName("Should contain PrivateDrivingPermitApi with correct properties")
    void shouldContainPrivateDrivingPermitApi() {
        cloudFormationTemplate.hasResourceProperties("AWS::Serverless::Api", Map.of(
                "Name", "${AWS::StackName}-PrivateDLApi",
                "StageName", "Environment",
                "TracingEnabled", true,
                "EndpointConfiguration", Map.of("Type", "REGIONAL")
        ));
        System.out.println("Assertion Passed: PublicDrivingPermitApi exists with expected properties.");
    }
}
