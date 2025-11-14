package gov.di_ipv_drivingpermit.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.assertions.Template;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("InfraTest")
@DisplayName("Certificate CloudFormation Template Assertions")
class CertificateTemplateTest {

    private static Template cloudFormationTemplate;

    /** This method runs once before all tests to load and parse the CloudFormation template. */
    @BeforeAll
    static void setup() throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Path templateFilePath =
                Paths.get("..", "infrastructure", "certificate", "template.yaml").normalize();
        System.out.println(
                "Attempting to load template from: "
                        + templateFilePath.toAbsolutePath()); // Added for debugging

        try (InputStream inputStream = new FileInputStream(templateFilePath.toFile())) {
            Map<String, Object> templateMap =
                    yamlMapper.readValue(inputStream, new TypeReference<>() {});
            cloudFormationTemplate = Template.fromJSON(templateMap);
        }
        assertNotNull(cloudFormationTemplate, "CloudFormation template should be loaded.");
    }

    /**
     * Helper method to get the 'Properties' map for all resources of a given type.
     *
     * @param resourceType The AWS CloudFormation resource type (e.g., "AWS::Serverless::Api").
     * @return A map where keys are logical IDs and values are the 'Properties' map of each
     *     resource. Returns an empty map if no resources of the specified type are found. Asserts
     *     that 'Properties' map is not null for any found resource.
     */
    private static Map<String, Map<String, Object>> getAllResourceProperties(String resourceType) {
        Map<String, Map<String, Object>> resources =
                cloudFormationTemplate.findResources(resourceType);
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

            assertNotNull(
                    properties,
                    "Properties map should not be null for resource: "
                            + logicalId
                            + " of type "
                            + resourceType);
            propertiesMap.put(logicalId, properties);
        }
        return propertiesMap;
    }

    /**
     * Helper method to get the full resource definition for all resources of a given type. This
     * includes 'Type', 'Properties', 'Condition', etc.
     *
     * @param resourceType The AWS CloudFormation resource type (e.g.,
     *     "AWS::Logs::SubscriptionFilter").
     * @return A map where keys are logical IDs and values are the full resource definition map.
     *     Returns an empty map if no resources of the specified type are found.
     */
    private static Map<String, Map<String, Object>> getAllResources(String resourceType) {
        Map<String, Map<String, Object>> resources =
                cloudFormationTemplate.findResources(resourceType);
        if (resources.isEmpty()) {
            fail("Expected at least one " + resourceType + " resource in the template.");
        }
        return resources;
    }

    /**
     * Helper method to safely retrieve a property from a map using a Class object for simpler
     * types.
     *
     * @param <T> The generic type of the expected property.
     * @return An Optional containing the property value if found and of the correct type, otherwise
     *     empty.
     */
    private static <T> Optional<T> getProperty(
            Map<String, Object> properties, String key, Class<T> type) {
        if (properties != null && properties.containsKey(key)) {
            Object value = properties.get(key);
            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }
        }
        return Optional.empty();
    }

    // ACMPCA CertificateAuthority Resource Tests

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthority resources should contain Condition: IsCAEnvironment")
    void allCertificateAuthorityResourcesShouldContainConditionIsCAEnvironment() {
        Map<String, Map<String, Object>> certificateAuthorityProperties =
                getAllResources("AWS::ACMPCA::CertificateAuthority");

        assertFalse(
                certificateAuthorityProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthority resource in the template.");

        boolean allCertificateAuthoritiesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedConditionValue = "IsCAEnvironment";

        for (Map.Entry<String, Map<String, Object>> entry :
                certificateAuthorityProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> condition = getProperty(properties, "Condition", String.class);

            if (condition.isPresent()) {
                String actualConditionValue = condition.get();
                if (!expectedConditionValue.equals(actualConditionValue)) {
                    allCertificateAuthoritiesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate Authority resource '%s' has 'Condition' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualConditionValue, expectedConditionValue));
                }
            } else {
                allCertificateAuthoritiesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate Authority resource '%s' does not have a 'Condition' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificateAuthoritiesSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthority resources failed the Condition check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthority resources should contain KeyAlgorithm: RSA_2048")
    void allCertificateAuthorityResourcesShouldContainKeyAlgorithmRSA2048() {
        Map<String, Map<String, Object>> certificateAuthorityProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthority");

        assertFalse(
                certificateAuthorityProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthority resource in the template.");

        boolean allCertificateAuthoritiesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedKeyAlgorithmValue = "RSA_2048";

        for (Map.Entry<String, Map<String, Object>> entry :
                certificateAuthorityProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> keyAlgorithm = getProperty(properties, "KeyAlgorithm", String.class);

            if (keyAlgorithm.isPresent()) {
                String actualKeyAlgorithmValue = keyAlgorithm.get();
                if (!expectedKeyAlgorithmValue.equals(actualKeyAlgorithmValue)) {
                    allCertificateAuthoritiesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate Authority resource '%s' has 'KeyAlgorithm' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualKeyAlgorithmValue, expectedKeyAlgorithmValue));
                }
            } else {
                allCertificateAuthoritiesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate Authority resource '%s' does not have a 'KeyAlgorithm' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificateAuthoritiesSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthority resources failed the KeyAlgorithm check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthority resources should contain SigningAlgorithm: SHA256WITHRSA")
    void allCertificateAuthorityResourcesShouldContainSigningAlgorithmSHA256WITHRSA() {
        Map<String, Map<String, Object>> certificateAuthorityProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthority");

        assertFalse(
                certificateAuthorityProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthority resource in the template.");

        boolean allCertificateAuthoritiesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedSigningAlgorithmValue = "SHA256WITHRSA";

        for (Map.Entry<String, Map<String, Object>> entry :
                certificateAuthorityProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> signingAlgorithm =
                    getProperty(properties, "SigningAlgorithm", String.class);

            if (signingAlgorithm.isPresent()) {
                String actualSigningAlgorithmValue = signingAlgorithm.get();
                if (!expectedSigningAlgorithmValue.equals(actualSigningAlgorithmValue)) {
                    allCertificateAuthoritiesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate Authority resource '%s' has 'SigningAlgorithm' property set to '%s', but expected '%s'.%n",
                                    logicalId,
                                    actualSigningAlgorithmValue,
                                    expectedSigningAlgorithmValue));
                }
            } else {
                allCertificateAuthoritiesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate Authority resource '%s' does not have a 'SigningAlgorithm' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificateAuthoritiesSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthority resources failed the SigningAlgorithm check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthority resources should contain a Tag with Key 'CreatedBy' and Value 'InfraAsCode'")
    void allCertificateAuthorityResourcesShouldContainCreatedByInfraAsCodeTag() {
        Map<String, Map<String, Object>> certificateAuthorityProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthority");

        assertFalse(
                certificateAuthorityProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthority resource in the template.");

        boolean allCertificateAuthoritiesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedTagKey = "CreatedBy";
        String expectedTagValue = "InfraAsCode";

        for (Map.Entry<String, Map<String, Object>> entry :
                certificateAuthorityProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            @SuppressWarnings("unchecked")
            Optional<List<Object>> tagsOptional =
                    getProperty(properties, "Tags", (Class<List<Object>>) (Class<?>) List.class);

            if (tagsOptional.isPresent()) {
                List<Object> tagsList = tagsOptional.get();
                boolean foundMatchingCreatedByTag = false;

                for (Object tagObject : tagsList) {
                    if (tagObject instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tagMap = (Map<String, Object>) tagObject;
                        Optional<String> key = getProperty(tagMap, "Key", String.class);
                        Optional<String> value = getProperty(tagMap, "Value", String.class);

                        if (key.isPresent() && expectedTagKey.equals(key.get())) {
                            if (value.isPresent() && expectedTagValue.equals(value.get())) {
                                foundMatchingCreatedByTag = true;
                                break;
                            } else {
                                failureMessages.append(
                                        String.format(
                                                "Certificate Authority resource '%s' has a 'CreatedBy' tag, but its 'Value' is '%s' (expected '%s').%n",
                                                logicalId,
                                                value.orElse("missing/not a String"),
                                                expectedTagValue));
                            }
                        }
                    }
                }

                if (!foundMatchingCreatedByTag) {
                    allCertificateAuthoritiesSatisfyCondition = false;
                    if (!failureMessages
                            .toString()
                            .contains(
                                    String.format(
                                            "Certificate Authority resource '%s' has a 'CreatedBy' tag",
                                            logicalId))) {
                        failureMessages.append(
                                String.format(
                                        "Certificate Authority resource '%s' has 'Tags' property but does not contain a tag with Key '%s' and Value '%s'.%n",
                                        logicalId, expectedTagKey, expectedTagValue));
                    }
                }
            } else {
                allCertificateAuthoritiesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate Authority resource '%s' does not have a 'Tags' property or it's not a List of Maps.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificateAuthoritiesSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthority resources failed the 'CreatedBy: InfraAsCode' tag check:\n"
                        + failureMessages);
    }

    // ACMPCA Certificate Resource Tests

    @Test
    @DisplayName("All AWS::ACMPCA::Certificate resources should contain Condition: IsCAEnvironment")
    void allAcmpcaCertificateResourcesShouldContainConditionIsCAEnvironment() {
        Map<String, Map<String, Object>> acmpcaCertificateProperties =
                getAllResources("AWS::ACMPCA::Certificate");

        assertFalse(
                acmpcaCertificateProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::Certificate resource in the template.");

        boolean allAcmpcaCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedConditionValue = "IsCAEnvironment";

        for (Map.Entry<String, Map<String, Object>> entry :
                acmpcaCertificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> condition = getProperty(properties, "Condition", String.class);

            if (condition.isPresent()) {
                String actualConditionValue = condition.get();
                if (!expectedConditionValue.equals(actualConditionValue)) {
                    allAcmpcaCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Certificate resource '%s' has 'Condition' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualConditionValue, expectedConditionValue));
                }
            } else {
                allAcmpcaCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate resource '%s' does not have a 'Condition' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allAcmpcaCertificatesSatisfyCondition,
                "One or more AWS::ACMPCA::Certificate resources failed the Condition check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::Certificate resources should contain SigningAlgorithm: SHA256WITHRSA")
    void allAcmpcaCertificateResourcesShouldContainSigningAlgorithmSHA256WITHRSA() {
        Map<String, Map<String, Object>> acmpcaCertificateProperties =
                getAllResourceProperties("AWS::ACMPCA::Certificate");

        assertFalse(
                acmpcaCertificateProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::Certificate resource in the template.");

        boolean allAcmpcaCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedSigningAlgorithmValue = "SHA256WITHRSA";

        for (Map.Entry<String, Map<String, Object>> entry :
                acmpcaCertificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> signingAlgorithm =
                    getProperty(properties, "SigningAlgorithm", String.class);

            if (signingAlgorithm.isPresent()) {
                String actualSigningAlgorithmValue = signingAlgorithm.get();
                if (!expectedSigningAlgorithmValue.equals(actualSigningAlgorithmValue)) {
                    allAcmpcaCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Certificate resource '%s' has 'SigningAlgorithm' property set to '%s', but expected '%s'.%n",
                                    logicalId,
                                    actualSigningAlgorithmValue,
                                    expectedSigningAlgorithmValue));
                }
            } else {
                allAcmpcaCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate resource '%s' does not have a 'SigningAlgorithm' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allAcmpcaCertificatesSatisfyCondition,
                "One or more AWS::ACMPCA::Certificate resources failed the SigningAlgorithm check:\n"
                        + failureMessages);
    }

    // ACMPCA CertificateAuthorityActivation Resource Tests

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthorityActivation resources should contain Condition: IsCAEnvironment")
    void allAcmpcaCertificateAuthorityActivationResourcesShouldContainConditionIsCAEnvironment() {
        Map<String, Map<String, Object>> caActivationProperties =
                getAllResources("AWS::ACMPCA::CertificateAuthorityActivation");

        assertFalse(
                caActivationProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthorityActivation resource in the template.");

        boolean allCaActivationsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedConditionValue = "IsCAEnvironment";

        for (Map.Entry<String, Map<String, Object>> entry : caActivationProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> condition = getProperty(properties, "Condition", String.class);

            if (condition.isPresent()) {
                String actualConditionValue = condition.get();
                if (!expectedConditionValue.equals(actualConditionValue)) {
                    allCaActivationsSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Certificate Authority Activation resource '%s' has 'Condition' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualConditionValue, expectedConditionValue));
                }
            } else {
                allCaActivationsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate Authority Activation resource '%s' does not have a 'Condition' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCaActivationsSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthorityActivation resources failed the Condition check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthorityActivation resources should contain CertificateAuthorityArn")
    void allAcmpcaCertificateAuthorityActivationResourcesShouldContainCertificateAuthorityArn() {
        Map<String, Map<String, Object>> caActivationProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthorityActivation");

        assertFalse(
                caActivationProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthorityActivation resource in the template.");

        boolean allCaActivationsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : caActivationProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> certificateAuthorityArn =
                    getProperty(properties, "CertificateAuthorityArn", String.class);

            if (certificateAuthorityArn.isEmpty()) {
                allCaActivationsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate Authority Activation resource '%s' does not have a 'CertificateAuthorityArn' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCaActivationsSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthorityActivation resources failed the CertificateAuthorityArn existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthorityActivation resources should contain Certificate")
    void allAcmpcaCertificateAuthorityActivationResourcesShouldContainCertificate() {
        Map<String, Map<String, Object>> caActivationProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthorityActivation");

        assertFalse(
                caActivationProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthorityActivation resource in the template.");

        boolean allCaActivationsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : caActivationProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> certificate = getProperty(properties, "Certificate", String.class);

            if (certificate.isEmpty()) {
                allCaActivationsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate Authority Activation resource '%s' does not have a 'Certificate' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCaActivationsSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthorityActivation resources failed the Certificate existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::CertificateAuthorityActivation resources should contain Status: ACTIVE")
    void allAcmpcaCertificateAuthorityActivationResourcesShouldContainStatusActive() {
        Map<String, Map<String, Object>> caActivationProperties =
                getAllResourceProperties("AWS::ACMPCA::CertificateAuthorityActivation");

        assertFalse(
                caActivationProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::CertificateAuthorityActivation resource in the template.");

        boolean allCaActivationsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedStatusValue = "ACTIVE";

        for (Map.Entry<String, Map<String, Object>> entry : caActivationProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> status = getProperty(properties, "Status", String.class);

            if (status.isPresent()) {
                String actualStatusValue = status.get();
                if (!expectedStatusValue.equals(actualStatusValue)) {
                    allCaActivationsSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Certificate Authority Activation resource '%s' has 'Status' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualStatusValue, expectedStatusValue));
                }
            } else {
                allCaActivationsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Certificate Authority Activation resource '%s' does not have a 'Status' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCaActivationsSatisfyCondition,
                "One or more AWS::ACMPCA::CertificateAuthorityActivation resources failed the Status check:\n"
                        + failureMessages);
    }

    // ACMPCA Permission Resource Tests

    @Test
    @DisplayName("All AWS::ACMPCA::Permission resources should contain Condition: IsCAEnvironment")
    void allAcmpcaPermissionResourcesShouldContainConditionIsCAEnvironment() {
        Map<String, Map<String, Object>> permissionProperties =
                getAllResources("AWS::ACMPCA::Permission");

        assertFalse(
                permissionProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::Permission resource in the template.");

        boolean allPermissionsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedConditionValue = "IsCAEnvironment";

        for (Map.Entry<String, Map<String, Object>> entry : permissionProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> condition = getProperty(properties, "Condition", String.class);

            if (condition.isPresent()) {
                String actualConditionValue = condition.get();
                if (!expectedConditionValue.equals(actualConditionValue)) {
                    allPermissionsSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Permission resource '%s' has 'Condition' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualConditionValue, expectedConditionValue));
                }
            } else {
                allPermissionsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Permission resource '%s' does not have a 'Condition' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allPermissionsSatisfyCondition,
                "One or more AWS::ACMPCA::Permission resources failed the Condition check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName("All AWS::ACMPCA::Permission resources should contain CertificateAuthorityArn")
    void allAcmpcaPermissionResourcesShouldContainCertificateAuthorityArn() {
        Map<String, Map<String, Object>> permissionProperties =
                getAllResourceProperties("AWS::ACMPCA::Permission");

        assertFalse(
                permissionProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::Permission resource in the template.");

        boolean allPermissionsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : permissionProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> certificateAuthorityArn =
                    getProperty(properties, "CertificateAuthorityArn", String.class);

            if (certificateAuthorityArn.isEmpty()) {
                allPermissionsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Permission resource '%s' does not have a 'CertificateAuthorityArn' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allPermissionsSatisfyCondition,
                "One or more AWS::ACMPCA::Permission resources failed the CertificateAuthorityArn existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::ACMPCA::Permission resources should contain Principal: acm.amazonaws.com")
    void allAcmpcaPermissionResourcesShouldContainPrincipalAcmAmazonawsCom() {
        Map<String, Map<String, Object>> permissionProperties =
                getAllResourceProperties("AWS::ACMPCA::Permission");

        assertFalse(
                permissionProperties.isEmpty(),
                "Expected at least one AWS::ACMPCA::Permission resource in the template.");

        boolean allPermissionsSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedPrincipalValue = "acm.amazonaws.com";

        for (Map.Entry<String, Map<String, Object>> entry : permissionProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> principal = getProperty(properties, "Principal", String.class);

            if (principal.isPresent()) {
                String actualPrincipalValue = principal.get();
                if (!expectedPrincipalValue.equals(actualPrincipalValue)) {
                    allPermissionsSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "ACMPCA Permission resource '%s' has 'Principal' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualPrincipalValue, expectedPrincipalValue));
                }
            } else {
                allPermissionsSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "ACMPCA Permission resource '%s' does not have a 'Principal' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allPermissionsSatisfyCondition,
                "One or more AWS::ACMPCA::Permission resources failed the Principal check:\n"
                        + failureMessages);
    }

    // CertificateManager Certificate Resource Tests

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain Condition: IsCAEnvironment")
    void allCertificateResourcesShouldContainConditionIsCAEnvironment() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResources("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedConditionValue = "IsCAEnvironment";

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> condition = getProperty(properties, "Condition", String.class);

            if (condition.isPresent()) {
                String actualConditionValue = condition.get();
                if (!expectedConditionValue.equals(actualConditionValue)) {
                    allCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' has 'Condition' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualConditionValue, expectedConditionValue));
                }
            } else {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'Condition' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the Condition check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain ValidationMethod: DNS")
    void allCertificateResourcesShouldContainValidationMethodDNS() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedValidationMethodValue = "DNS";

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> validationMethod =
                    getProperty(properties, "ValidationMethod", String.class);

            if (validationMethod.isPresent()) {
                String actualValidationMethodValue = validationMethod.get();
                if (!expectedValidationMethodValue.equals(actualValidationMethodValue)) {
                    allCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' has 'ValidationMethod' property set to '%s', but expected '%s'.%n",
                                    logicalId,
                                    actualValidationMethodValue,
                                    expectedValidationMethodValue));
                }
            } else {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'ValidationMethod' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the ValidationMethod check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain KeyAlgorithm: RSA_2048")
    void allCertificateResourcesShouldContainKeyAlgorithmRSA2048() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedKeyAlgorithmValue = "RSA_2048";

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> keyAlgorithm = getProperty(properties, "KeyAlgorithm", String.class);

            if (keyAlgorithm.isPresent()) {
                String actualKeyAlgorithmValue = keyAlgorithm.get();
                if (!expectedKeyAlgorithmValue.equals(actualKeyAlgorithmValue)) {
                    allCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' has 'KeyAlgorithm' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualKeyAlgorithmValue, expectedKeyAlgorithmValue));
                }
            } else {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'KeyAlgorithm' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the KeyAlgorithm check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain CertificateAuthorityArn")
    void allCertificateResourcesShouldContainCertificateAuthorityArn() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> certificateAuthorityArn =
                    getProperty(properties, "CertificateAuthorityArn", String.class);

            if (certificateAuthorityArn.isEmpty()) {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'CertificateAuthorityArn' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the CertificateAuthorityArn existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName("All AWS::CertificateManager::Certificate resources should contain DomainName")
    void allCertificateResourcesShouldContainDomainName() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            @SuppressWarnings(
                    "unchecked") // Suppress the unchecked cast warning for this specific line
            Optional<List<Object>> domainNameOptional =
                    getProperty(
                            properties, "DomainName", (Class<List<Object>>) (Class<?>) List.class);

            if (domainNameOptional.isEmpty()) {
                allCertificatesSatisfyCondition = false;
                if (properties.containsKey("DomainName")) {
                    Object actualValue = properties.get("DomainName");
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' has 'DomainName' property, but it's of type '%s' (value: '%s'), expected a List (e.g., arguments for an intrinsic function).%n",
                                    logicalId,
                                    actualValue.getClass().getSimpleName(),
                                    actualValue));
                } else {
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' does not have a 'DomainName' property.%n",
                                    logicalId));
                }
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the DomainName existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain a Tag with Key 'CreatedBy' and Value 'InfraAsCode'")
    void allCertificateResourcesShouldContainCreatedByInfraAsCodeTag() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedTagKey = "CreatedBy";
        String expectedTagValue = "InfraAsCode";

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            @SuppressWarnings(
                    "unchecked") // Suppress unchecked warning for the cast to Class<List<Object>>
            Optional<List<Object>> tagsOptional =
                    getProperty(properties, "Tags", (Class<List<Object>>) (Class<?>) List.class);

            if (tagsOptional.isPresent()) {
                List<Object> tagsList = tagsOptional.get();
                boolean foundMatchingCreatedByTag = false;

                for (Object tagObject : tagsList) {
                    if (tagObject instanceof Map) {
                        @SuppressWarnings("unchecked") // Suppress unchecked warning for the cast to
                        // Map<String, Object>
                        Map<String, Object> tagMap = (Map<String, Object>) tagObject;
                        Optional<String> key = getProperty(tagMap, "Key", String.class);
                        Optional<String> value = getProperty(tagMap, "Value", String.class);

                        if (key.isPresent() && expectedTagKey.equals(key.get())) {
                            if (value.isPresent() && expectedTagValue.equals(value.get())) {
                                foundMatchingCreatedByTag = true;
                                break;
                            } else {
                                failureMessages.append(
                                        String.format(
                                                "Certificate resource '%s' has a 'CreatedBy' tag, but its 'Value' is '%s' (expected '%s').%n",
                                                logicalId,
                                                value.orElse("missing/not a String"),
                                                expectedTagValue));
                            }
                        }
                    }
                }

                if (!foundMatchingCreatedByTag) {
                    allCertificatesSatisfyCondition = false;
                    if (!failureMessages
                            .toString()
                            .contains(
                                    String.format(
                                            "Certificate resource '%s' has a 'CreatedBy' tag",
                                            logicalId))) {
                        failureMessages.append(
                                String.format(
                                        "Certificate resource '%s' has 'Tags' property but does not contain a tag with Key '%s' and Value '%s'.%n",
                                        logicalId, expectedTagKey, expectedTagValue));
                    }
                }
            } else {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'Tags' property or it's not a List of Maps.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the 'CreatedBy: InfraAsCode' tag check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::CertificateManager::Certificate resources should contain a Tag with Key 'certificateName'")
    void allCertificateResourcesShouldContainCertificateNameTag() {
        Map<String, Map<String, Object>> certificateProperties =
                getAllResourceProperties("AWS::CertificateManager::Certificate");

        assertFalse(
                certificateProperties.isEmpty(),
                "Expected at least one AWS::CertificateManager::Certificate resource in the template.");

        boolean allCertificatesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedTagKey = "certificateName"; // Define the expected key

        for (Map.Entry<String, Map<String, Object>> entry : certificateProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            // Retrieve the 'Tags' property, which is expected to be a List of Maps
            @SuppressWarnings(
                    "unchecked") // Suppress unchecked warning for the cast to Class<List<Object>>
            Optional<List<Object>> tagsOptional =
                    getProperty(properties, "Tags", (Class<List<Object>>) (Class<?>) List.class);

            if (tagsOptional.isPresent()) {
                List<Object> tagsList = tagsOptional.get();
                boolean foundCertificateNameKey =
                        false; // Flag to track if 'certificateName' key is found

                for (Object tagObject : tagsList) {
                    if (tagObject instanceof Map) {
                        @SuppressWarnings("unchecked") // Suppress unchecked warning for the cast to
                        // Map<String, Object>
                        Map<String, Object> tagMap = (Map<String, Object>) tagObject;
                        Optional<String> key = getProperty(tagMap, "Key", String.class);

                        if (key.isPresent() && expectedTagKey.equals(key.get())) {
                            foundCertificateNameKey = true;
                            break; // Found the 'certificateName' key, no need to check further tags
                            // for this resource
                        }
                    }
                }

                if (!foundCertificateNameKey) {
                    allCertificatesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "Certificate resource '%s' has 'Tags' property but does not contain a tag with Key '%s'.%n",
                                    logicalId, expectedTagKey));
                }
            } else {
                allCertificatesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "Certificate resource '%s' does not have a 'Tags' property or it's not a List of Maps.%n",
                                logicalId));
            }
        }

        assertTrue(
                allCertificatesSatisfyCondition,
                "One or more AWS::CertificateManager::Certificate resources failed the 'certificateName' tag check:\n"
                        + failureMessages);
    }

    // KMS Key Resource Tests

    @Test
    @DisplayName("All AWS::KMS::Key resources should contain a Description")
    void allKmsKeyResourcesShouldContainDescription() {
        // Change: Get properties for AWS::KMS::Key resources
        Map<String, Map<String, Object>> kmsKeyProperties =
                getAllResourceProperties("AWS::KMS::Key");

        assertFalse(
                kmsKeyProperties.isEmpty(),
                "Expected at least one AWS::KMS::Key resource in the template.");

        boolean allKmsKeysSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : kmsKeyProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> description = getProperty(properties, "Description", String.class);

            if (description.isEmpty()) { // Check if the property is NOT present
                allKmsKeysSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Key resource '%s' does not have a 'Description' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsKeysSatisfyCondition,
                "One or more AWS::KMS::Key resources failed the Description existence check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName("All AWS::KMS::Key resources should contain KeySpec: RSA_2048")
    void allKmsKeyResourcesShouldContainKeySpecRSA2048() {
        Map<String, Map<String, Object>> kmsKeyProperties =
                getAllResourceProperties("AWS::KMS::Key");

        assertFalse(
                kmsKeyProperties.isEmpty(),
                "Expected at least one AWS::KMS::Key resource in the template.");

        boolean allKmsKeysSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedKeySpecValue = "RSA_2048";

        for (Map.Entry<String, Map<String, Object>> entry : kmsKeyProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> keySpec = getProperty(properties, "KeySpec", String.class);

            if (keySpec.isPresent()) {
                String actualKeySpecValue = keySpec.get();
                if (!expectedKeySpecValue.equals(actualKeySpecValue)) {
                    allKmsKeysSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "KMS Key resource '%s' has 'KeySpec' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualKeySpecValue, expectedKeySpecValue));
                }
            } else {
                allKmsKeysSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Key resource '%s' does not have a 'KeySpec' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsKeysSatisfyCondition,
                "One or more AWS::KMS::Key resources failed the KeySpec check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName(
            "All AWS::KMS::Key resources should contain KeyUsage: ENCRYPT_DECRYPT or SIGN_VERIFY")
    void allKmsKeyResourcesShouldContainValidKeyUsage() {
        Map<String, Map<String, Object>> kmsKeyProperties =
                getAllResourceProperties("AWS::KMS::Key");

        assertFalse(
                kmsKeyProperties.isEmpty(),
                "Expected at least one AWS::KMS::Key resource in the template.");

        boolean allKmsKeysSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        List<String> allowedKeyUsageValues = List.of("ENCRYPT_DECRYPT", "SIGN_VERIFY");

        for (Map.Entry<String, Map<String, Object>> entry : kmsKeyProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> keyUsage = getProperty(properties, "KeyUsage", String.class);

            if (keyUsage.isPresent()) {
                String actualKeyUsageValue = keyUsage.get();
                if (!allowedKeyUsageValues.contains(actualKeyUsageValue)) {
                    allKmsKeysSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "KMS Key resource '%s' has 'KeyUsage' property set to '%s', but expected one of %s.%n",
                                    logicalId, actualKeyUsageValue, allowedKeyUsageValues));
                }
            } else {
                allKmsKeysSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Key resource '%s' does not have a 'KeyUsage' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsKeysSatisfyCondition,
                "One or more AWS::KMS::Key resources failed the KeyUsage check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName("All AWS::KMS::Key resources should contain Origin: EXTERNAL")
    void allKmsKeyResourcesShouldContainOriginExternal() {
        Map<String, Map<String, Object>> kmsKeyProperties =
                getAllResourceProperties("AWS::KMS::Key");

        assertFalse(
                kmsKeyProperties.isEmpty(),
                "Expected at least one AWS::KMS::Key resource in the template.");

        boolean allKmsKeysSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedOriginValue = "EXTERNAL";

        for (Map.Entry<String, Map<String, Object>> entry : kmsKeyProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> origin = getProperty(properties, "Origin", String.class);

            if (origin.isPresent()) {
                String actualOriginValue = origin.get();
                if (!expectedOriginValue.equals(actualOriginValue)) {
                    allKmsKeysSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "KMS Key resource '%s' has 'Origin' property set to '%s', but expected '%s'.%n",
                                    logicalId, actualOriginValue, expectedOriginValue));
                }
            } else {
                allKmsKeysSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Key resource '%s' does not have an 'Origin' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsKeysSatisfyCondition,
                "One or more AWS::KMS::Key resources failed the Origin check:\n" + failureMessages);
    }

    // KMS Alias Resource Tests

    @Test
    @DisplayName(
            "All AWS::KMS::Alias resources should contain AliasName starting with 'alias/${AWS::StackName}/'")
    void allKmsAliasResourcesShouldContainAliasNameWithPrefix() {
        Map<String, Map<String, Object>> kmsAliasProperties =
                getAllResourceProperties("AWS::KMS::Alias");

        assertFalse(
                kmsAliasProperties.isEmpty(),
                "Expected at least one AWS::KMS::Alias resource in the template.");

        boolean allKmsAliasesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        String expectedPrefix = "alias/${AWS::StackName}/";

        for (Map.Entry<String, Map<String, Object>> entry : kmsAliasProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> aliasName = getProperty(properties, "AliasName", String.class);

            if (aliasName.isPresent()) {
                String actualAliasNameValue = aliasName.get();
                if (!actualAliasNameValue.startsWith(expectedPrefix)) {
                    allKmsAliasesSatisfyCondition = false;
                    failureMessages.append(
                            String.format(
                                    "KMS Alias resource '%s' has 'AliasName' property '%s', which does not start with '%s'.%n",
                                    logicalId, actualAliasNameValue, expectedPrefix));
                }
            } else {
                allKmsAliasesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Alias resource '%s' does not have an 'AliasName' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsAliasesSatisfyCondition,
                "One or more AWS::KMS::Alias resources failed the AliasName prefix check:\n"
                        + failureMessages);
    }

    @Test
    @DisplayName("All AWS::KMS::Alias resources should contain TargetKeyId")
    void allKmsAliasResourcesShouldContainTargetKeyId() {
        Map<String, Map<String, Object>> kmsAliasProperties =
                getAllResourceProperties("AWS::KMS::Alias");

        assertFalse(
                kmsAliasProperties.isEmpty(),
                "Expected at least one AWS::KMS::Alias resource in the template.");

        boolean allKmsAliasesSatisfyCondition = true;
        StringBuilder failureMessages = new StringBuilder();

        for (Map.Entry<String, Map<String, Object>> entry : kmsAliasProperties.entrySet()) {
            String logicalId = entry.getKey();
            Map<String, Object> properties = entry.getValue();

            Optional<String> targetKeyId = getProperty(properties, "TargetKeyId", String.class);

            if (targetKeyId.isEmpty()) {
                allKmsAliasesSatisfyCondition = false;
                failureMessages.append(
                        String.format(
                                "KMS Alias resource '%s' does not have a 'TargetKeyId' property or it's not a String.%n",
                                logicalId));
            }
        }

        assertTrue(
                allKmsAliasesSatisfyCondition,
                "One or more AWS::KMS::Alias resources failed the TargetKeyId existence check:\n"
                        + failureMessages);
    }
}
