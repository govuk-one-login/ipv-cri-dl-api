package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.TestDataCreator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static gov.di_ipv_drivingpermit.pages.Headers.IPV_CORE_STUB;
import static java.lang.System.getenv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DLCommonPageObject extends UniversalSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(DLCommonPageObject.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConfigurationService configurationService;

    private static final String STUB_VC_PAGE_TITLE = "IPV Core Stub Credential Result - GOV.UK";
    private static final String STUB_ERROR_PAGE_TITLE = "IPV Core Stub - GOV.UK";

    private static String vcTxn;
    private static Instant feTestStartTime;

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI dev\"]")
    public WebElement drivingLicenceCRIDev;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Build\"]")
    public WebElement drivingLicenceCRIBuild;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Staging\"]")
    public WebElement drivingLicenceCRIStaging;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Integration\"]")
    public WebElement drivingLicenceCRIIntegration;

    @FindBy(id = "rowNumber")
    public WebElement selectRow;

    @FindBy(xpath = "//*[@id=\"context\"]")
    public WebElement selectInputContextValue;

    @FindBy(id = "claimsText")
    public WebElement selectInputSharedClaimsValue;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/div/pre")
    public WebElement jsonPayload;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details")
    public WebElement errorResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/summary/span")
    public WebElement viewResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement searchButton;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[3]/div/button")
    public WebElement searchButtonRawJson;

    // ---------------------

    public DLCommonPageObject() {
        this.configurationService = new ConfigurationService(getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
        TestDataCreator.createDefaultResponses();
    }

    public void navigateToIPVCoreStub() {
        Driver.get().manage().deleteAllCookies();

        String coreStubUrl = configurationService.getCoreStubUrl(true);
        Driver.get().get(coreStubUrl);
        assertExpectedPage(IPV_CORE_STUB, false);
    }

    public void navigateToDrivingLicenceCRIOnTestEnv() {
        BrowserUtils.clickAndWaitForNavigation(visitCredentialIssuers);
        assertExpectedPage(IPV_CORE_STUB, false);
        String dlCRITestEnvironment = configurationService.getDlCRITestEnvironment();
        LOGGER.info("dlCRITestEnvironment = {}", dlCRITestEnvironment);

        switch (dlCRITestEnvironment.toLowerCase()) {
            case "dev", "local" -> BrowserUtils.clickAndWaitForNavigation(drivingLicenceCRIDev);
            case "build" -> BrowserUtils.clickAndWaitForNavigation(drivingLicenceCRIBuild);
            case "staging" -> BrowserUtils.clickAndWaitForNavigation(drivingLicenceCRIStaging);
            case "integration" ->
                    BrowserUtils.clickAndWaitForNavigation(drivingLicenceCRIIntegration);
            default -> LOGGER.info("No test environment is set");
        }

        assertURLContains("credential-issuer?cri=driving-licence-cri");
    }

    // Selects a UAT user row and clicks search, triggering a redirect chain
    // through to the driving licence credential issuer.
    public void searchForUATUser(String number) {
        selectRow.sendKeys(number);
        BrowserUtils.clickAndWaitForNavigation(searchButton);
        assertURLContains("licence-issuer");
    }

    public void enterContextValue(String contextValue) {
        assertURLContains("credential-issuer?cri=driving-licence");
        selectInputContextValue.sendKeys(contextValue);
    }

    public void enterSharedClaimsRawJSONValue(String jsonFileName) {
        String sharedClaimsRawJson = getJsonPayload(jsonFileName);
        if (sharedClaimsRawJson != null) {
            selectInputSharedClaimsValue.sendKeys(sharedClaimsRawJson);
            BrowserUtils.clickAndWaitForNavigation(searchButtonRawJson);
        } else {
            throw new RuntimeException("Failed to load JSON from file: " + jsonFileName);
        }
    }

    public void navigateToDrivingLicenceResponse(String validOrInvalid) {
        assertURLContains(configurationService.getCoreStubEndpoint() + "/callback");

        if ("Invalid".equalsIgnoreCase(validOrInvalid)) {
            assertExpectedPage(STUB_ERROR_PAGE_TITLE, true);
            assertURLContains("callback");
            BrowserUtils.waitForVisibility(errorResponse, MAX_WAIT_SEC);
            errorResponse.click();
        } else {
            assertExpectedPage(STUB_VC_PAGE_TITLE, true);
            assertURLContains("callback");
            feTestStartTime = Instant.now().minusSeconds(30);
            BrowserUtils.waitForVisibility(viewResponse, MAX_WAIT_SEC);
            viewResponse.click();
            extractAndStoreTxnFromVc();
        }
    }

    private void extractAndStoreTxnFromVc() {
        try {
            BrowserUtils.waitForVisibility(jsonPayload, MAX_WAIT_SEC);
            String payloadText = jsonPayload.getText();
            JsonNode vcNode = getJsonNode(payloadText, "vc");
            if (vcNode != null) {
                List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");
                JsonNode txnNode = evidence.get(0).get("txn");
                if (txnNode != null && !txnNode.isNull()) {
                    vcTxn = txnNode.asText();
                    LOGGER.info("Captured txn from VC: '{}'", vcTxn);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not extract txn from VC: {}", e.getMessage());
        }
    }

    public static void resetFeTestContext() {
        vcTxn = null;
        feTestStartTime = null;
    }

    public static String getVcTxn() {
        return vcTxn;
    }

    public static Instant getFeTestStartTime() {
        return feTestStartTime;
    }

    // -----------------------

    public void drivingLicencePageURLValidation(String path) {
        assertURLContains(path);
    }

    public void assertUserRoutedToIpvCore() {
        assertExpectedPage(IPV_CORE_STUB, true);
    }

    public void assertUserRoutedToIpvCoreErrorPage() {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        String expUrl =
                coreStubUrl
                        + "/callback?error=access_denied&error_description=Authorization+permission+denied";
        String actUrl = Driver.get().getCurrentUrl();
        LOGGER.info("expectedUrl = {}", expUrl);
        LOGGER.info("actualUrl = {}", actUrl);
        assertEquals(actUrl, expUrl);
    }

    public void jsonErrorResponse(String expectedErrorDescription, String expectedErrorStatusCode)
            throws JsonProcessingException {
        String result = jsonPayload.getText();
        LOGGER.info("result = {}", result);

        JsonNode insideError = getJsonNode(result, "errorObject");
        LOGGER.info("insideError = {}", insideError);

        JsonNode errorDescription = insideError.get("description");
        JsonNode statusCode = insideError.get("httpstatusCode");
        String actualErrorDescription = insideError.get("description").asText();
        String actualStatusCode = insideError.get("httpstatusCode").asText();

        LOGGER.info("errorDescription = {}", errorDescription);
        LOGGER.info("statusCode = {}", statusCode);
        LOGGER.info("testErrorDescription = {}", expectedErrorDescription);
        LOGGER.info("testStatusCode = {}", expectedErrorStatusCode);

        assertEquals(expectedErrorDescription, actualErrorDescription);
        assertEquals(expectedErrorStatusCode, actualStatusCode);
    }

    public void checkScoresAndTypeInStubIs(String validityScore, String strengthScore, String type)
            throws IOException {
        scoreAndTypeIs(validityScore, strengthScore, type, jsonPayload.getText());
    }

    public void scoreIs(
            String expectedValidityScore, String expectedStrengthScore, String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String validityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(expectedValidityScore, validityScore);

        String strengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(expectedStrengthScore, strengthScore);
    }

    public void scoreAndTypeIs(
            String expectedValidityScore,
            String expectedStrengthScore,
            String expectedType,
            String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String validityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(expectedValidityScore, validityScore);

        String strengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(expectedStrengthScore, strengthScore);

        String type = evidence.get(0).get("type").asText();
        assertEquals(expectedType, type);
    }

    public void assertCheckDetailsWithinVc(
            String checkMethod,
            String identityCheckPolicy,
            String checkDetailsType,
            String drivingLicenceCRIVC)
            throws IOException {
        JsonNode vcNode = getJsonNode(drivingLicenceCRIVC, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");
        JsonNode firstItemInEvidenceArray = evidence.get(0);
        LOGGER.info("firstItemInEvidenceArray = {}", firstItemInEvidenceArray);
        if (checkDetailsType.equals("success")) {
            JsonNode checkDetailsNode = firstItemInEvidenceArray.get("checkDetails");
            JsonNode checkMethodNode = checkDetailsNode.get(0).get("checkMethod");
            String actualCheckMethod = checkMethodNode.asText();
            LOGGER.info("actualCheckMethod = {}", actualCheckMethod);
            JsonNode identityCheckPolicyNode = checkDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = {}", actualidentityCheckPolicy);
            JsonNode activityFromNode = checkDetailsNode.get(0).get("activityFrom");
            String actualactivityFrom = activityFromNode.asText();
            LOGGER.info("actualactivityFrom = {}", actualactivityFrom);
            Assert.assertEquals(checkMethod, actualCheckMethod);
            Assert.assertEquals(identityCheckPolicy, actualidentityCheckPolicy);
            if (!StringUtils.isEmpty(activityFromNode.toString())) {
                assertEquals(
                        "[{\"checkMethod\":"
                                + checkMethodNode.toString()
                                + ","
                                + "\"identityCheckPolicy\":"
                                + identityCheckPolicyNode.toString()
                                + ","
                                + "\"activityFrom\":"
                                + activityFromNode.toString()
                                + "}]",
                        checkDetailsNode.toString());
            }
        } else {
            JsonNode failedCheckDetailsNode = firstItemInEvidenceArray.get("failedCheckDetails");
            JsonNode checkMethodNode = failedCheckDetailsNode.get(0).get("checkMethod");
            String actualCheckMethod = checkMethodNode.asText();
            LOGGER.info("actualCheckMethod = {}", actualCheckMethod);
            JsonNode identityCheckPolicyNode =
                    failedCheckDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = {}", actualidentityCheckPolicy);
            Assert.assertEquals(checkMethod, actualCheckMethod);
            Assert.assertEquals(identityCheckPolicy, actualidentityCheckPolicy);
            assertEquals(
                    "[{\"checkMethod\":"
                            + checkMethodNode.toString()
                            + ","
                            + "\"identityCheckPolicy\":"
                            + identityCheckPolicyNode.toString()
                            + "}]",
                    failedCheckDetailsNode.toString());
        }
    }

    public void ciInVC(String ci) throws IOException {
        String result = jsonPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        List<String> cis = getCIsFromEvidence(evidenceNode);

        if (StringUtils.isNotEmpty(ci)) {
            if (cis.size() > 0) {
                assertTrue(cis.contains(ci));
            } else {
                fail("No CIs found");
            }
        }
    }

    public void assertPersonalNumberInVc(String personalNumber) throws IOException {
        String result = jsonPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        String licenceNumber = getPersonalNumberFromVc(vcNode);
        assertEquals(personalNumber, licenceNumber);
    }

    public void assertJtiPresent() throws IOException {
        String result = jsonPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode jtiNode = getJsonNode(result, "jti");
        String jti = jtiNode.asText();
        assertNotNull(jti);
    }

    private List<String> getCIsFromEvidence(JsonNode evidenceNode) throws IOException {
        ObjectReader objectReader = OBJECT_MAPPER.readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        return getListOfNodes(evidence.getFirst(), "ci").stream().map(JsonNode::asText).toList();
    }

    @Override
    public JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(result);
        return jsonNode.get(vc);
    }

    private String getPersonalNumberFromVc(JsonNode vcNode) throws IOException {
        JsonNode credentialSubject = vcNode.findValue("credentialSubject");
        List<JsonNode> evidence = getListOfNodes(credentialSubject, "drivingPermit");

        return evidence.getFirst().get("personalNumber").asText();
    }

    public List<JsonNode> getListOfNodes(JsonNode vcNode, String evidence) throws IOException {
        JsonNode evidenceNode = vcNode.get(evidence);

        ObjectReader objectReader = OBJECT_MAPPER.readerFor(new TypeReference<List<JsonNode>>() {});
        return objectReader.readValue(evidenceNode);
    }

    private JsonNode getVCFromJson(String vc) throws JsonProcessingException {
        String result = jsonPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode jsonNode = OBJECT_MAPPER.readTree(result);
        return jsonNode.get(vc);
    }
}
