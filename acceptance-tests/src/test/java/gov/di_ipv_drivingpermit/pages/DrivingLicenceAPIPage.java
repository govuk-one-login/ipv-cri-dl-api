package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
import gov.di_ipv_drivingpermit.model.AuthorisationResponse;
import gov.di_ipv_drivingpermit.model.DocumentCheckResponse;
import gov.di_ipv_drivingpermit.model.DrivingPermitForm;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.sendHttpRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicenceAPIPage extends DrivingLicencePageObject {

    private static String SESSION_REQUEST_BODY;
    private static String SESSION_ID;
    private static String STATE;
    private static String AUTHCODE;
    private static String ACCESS_TOKEN;

    private static String VC;

    private static Boolean RETRY;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModules(new JavaTimeModule());

    private final ConfigurationService configurationService =
            new ConfigurationService(System.getenv("ENVIRONMENT"));
    private static final Logger LOGGER = LogManager.getLogger();

    public String getAuthorisationJwtFromStub(String criId, Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        if (coreStubUrl == null) {
            throw new IllegalArgumentException("Environment variable IPV_CORE_STUB_URL is not set");
        }
        return getClaimsForUser(coreStubUrl, criId, rowNumber);
    }

    public void dlUserIdentityAsJwtString(String criId, Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String jsonString = getAuthorisationJwtFromStub(criId, rowNumber);
        LOGGER.info("jsonString = " + jsonString);
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY = " + SESSION_REQUEST_BODY);
    }

    public void dlPostRequestToSessionEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        LOGGER.info("getPrivateAPIEndpoint() ==> " + privateApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/session"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("X-Forwarded-For", "123456789")
                        .POST(HttpRequest.BodyPublishers.ofString(SESSION_REQUEST_BODY))
                        .build();
        String sessionResponse = sendHttpRequest(request).body();
        LOGGER.info("sessionResponse = " + sessionResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(sessionResponse, new TypeReference<>() {});
        SESSION_ID = deserialisedResponse.get("session_id");
        STATE = deserialisedResponse.get("state");
    }

    public void getSessionIdForDL() {
        LOGGER.info("SESSION_ID = " + SESSION_ID);
        assertTrue(StringUtils.isNotBlank(SESSION_ID));
    }

    public void postRequestToDrivingLicenceEndpoint(
            String dlJsonRequestBody, String jsonEditsString, String documentCheckingRoute)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        JsonNode dlJsonNode =
                objectMapper.readTree(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"));
        String dlInputJsonString = dlJsonNode.toString();

        Map<String, String> jsonEdits = new HashMap<>();
        if (!StringUtils.isEmpty(jsonEditsString)) {
            jsonEdits = objectMapper.readValue(jsonEditsString, Map.class);
        }

        DrivingPermitForm drivingPermitFormJson =
                objectMapper.readValue(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"),
                        DrivingPermitForm.class);

        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);

            field.set(drivingPermitFormJson, entry.getValue());
        }
        String drivingPermitInputJsonString =
                objectMapper.writeValueAsString(drivingPermitFormJson);

        HttpRequest.Builder request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", SESSION_ID)
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));
        if (documentCheckingRoute != null && !"not-provided".equals(documentCheckingRoute)) {
            request.setHeader("document-checking-route", documentCheckingRoute);
        }
        LOGGER.info("drivingLicenceRequestBody = " + dlInputJsonString);
        String drivingLicenceCheckResponse = sendHttpRequest(request.build()).body();
        LOGGER.info("drivingLicenceCheckResponse = " + drivingLicenceCheckResponse);
        DocumentCheckResponse documentCheckResponse =
                objectMapper.readValue(drivingLicenceCheckResponse, DocumentCheckResponse.class);
        RETRY = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = " + RETRY);
    }

    public void postRequestToDrivingLicenceEndpoint(
            String drivingPermitJsonRequestBody, String documentCheckingRoute)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpoint(
                drivingPermitJsonRequestBody, "", documentCheckingRoute);
    }

    public void retryValueInDLCheckResponse(Boolean retry) {
        assertEquals(RETRY, retry);
    }

    public void getAuthorisationCodeforDL() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        String coreStubClientId = "ipv-core-stub";
        if (!configurationService.isUsingLocalStub()) {
            coreStubClientId += "-aws-prod";
        }

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        privateApiGatewayUrl
                                                + "/authorization?redirect_uri="
                                                + coreStubUrl
                                                + "/callback&state="
                                                + STATE
                                                + "&scope=openid&response_type=code&client_id="
                                                + coreStubClientId))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session-id", SESSION_ID)
                        .GET()
                        .build();
        String authCallResponse = sendHttpRequest(request).body();
        LOGGER.info("authCallResponse = " + authCallResponse);
        AuthorisationResponse deserialisedResponse =
                objectMapper.readValue(authCallResponse, AuthorisationResponse.class);
        if (null != deserialisedResponse.getAuthorizationCode()) {
            AUTHCODE = deserialisedResponse.getAuthorizationCode().getValue();
            LOGGER.info("authorizationCode = " + AUTHCODE);
        }
    }

    public void postRequestToAccessTokenEndpointForDL(String criId)
            throws IOException, InterruptedException {
        String accessTokenRequestBody = getAccessTokenRequest(criId);
        LOGGER.info("Access Token Request Body = " + accessTokenRequestBody);
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        LOGGER.info("getPublicAPIEndpoint() ==> " + publicApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/token"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(accessTokenRequestBody))
                        .build();
        String accessTokenPostCallResponse = sendHttpRequest(request).body();
        LOGGER.info("accessTokenPostCallResponse = " + accessTokenPostCallResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(accessTokenPostCallResponse, new TypeReference<>() {});
        ACCESS_TOKEN = deserialisedResponse.get("access_token");
    }

    public String postRequestToDrivingLicenceVCEndpoint()
            throws IOException, InterruptedException, ParseException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/credential/issue"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
        String requestDrivingLicenceVCResponse = sendHttpRequest(request).body();
        LOGGER.info("requestDrivingLicenceVCResponse = " + requestDrivingLicenceVCResponse);
        SignedJWT signedJWT = SignedJWT.parse(requestDrivingLicenceVCResponse);
        return signedJWT.getJWTClaimsSet().toString();
    }

    public void validityScoreAndStrengthScoreInVC(String validityScore, String strengthScore)
            throws IOException, InterruptedException, ParseException {
        VC = postRequestToDrivingLicenceVCEndpoint();
        scoreIs(validityScore, strengthScore, VC);
    }

    public void assertCheckDetailsWithinVc(String checkDetailsType) throws IOException {

        JsonNode vcNode = getJsonNode(VC, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        JsonNode checkDetails = null;
        if (checkDetailsType.equals("success")) {
            checkDetails = evidence.get(0).get("checkDetails");
        } else {
            checkDetails = evidence.get(0).get("failedCheckDetails");
        }
        JsonNode activityFromNode = evidence.get(0).findPath("activityFrom");
        if (!StringUtils.isEmpty(activityFromNode.toString())) {
            assertEquals(
                    "[{\"checkMethod\":\"data\",\"identityCheckPolicy\":\"published\",\"activityFrom\":"
                            + activityFromNode.toString()
                            + "}]",
                    checkDetails.toString());
        } else {
            assertEquals(
                    "[{\"checkMethod\":\"data\",\"identityCheckPolicy\":\"published\"}]",
                    checkDetails.toString());
        }
    }

    public void ciInDrivingLicenceCriVc(String ci)
            throws IOException, InterruptedException, ParseException {
        String drivingLicenceCRIVC = postRequestToDrivingLicenceVCEndpoint();
        JsonNode jsonNode = objectMapper.readTree((drivingLicenceCRIVC));
        JsonNode evidenceArray = jsonNode.get("vc").get("evidence");
        JsonNode ciInEvidenceArray = evidenceArray.get(0);
        LOGGER.info("ciInEvidenceArray = " + ciInEvidenceArray);
        JsonNode ciNode = ciInEvidenceArray.get("ci").get(0);
        String actualCI = ciNode.asText();
        Assert.assertEquals(ci, actualCI);
    }

    public void assertCheckDetails(
            String checkMethod, String identityCheckPolicy, String checkDetailsType)
            throws URISyntaxException, IOException, InterruptedException, ParseException {
        String drivingLicenceCRIVC = postRequestToDrivingLicenceVCEndpoint();
        assertCheckDetailsWithinVc(
                checkMethod, identityCheckPolicy, checkDetailsType, drivingLicenceCRIVC);
    }

    private String getClaimsForUser(String baseUrl, String criId, int userDataRowNumber)
            throws URISyntaxException, IOException, InterruptedException {

        var url =
                new URI(
                        baseUrl
                                + "/backend/generateInitialClaimsSet?cri="
                                + criId
                                + "&rowNumber="
                                + userDataRowNumber);

        LOGGER.info("URL =>> " + url);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(url)
                        .GET()
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .build();
        return sendHttpRequest(request).body();
    }

    public void checkDrivingPermitResponseContainsException() {
        RETRY.equals(
                "{\"oauth_error\":{\"error_description\":\"Unexpected server error\",\"error\":\"server_error\"}}");
    }

    private String createRequest(String baseUrl, String criId, String jsonString)
            throws URISyntaxException, IOException, InterruptedException {

        URI uri = new URI(baseUrl + "/backend/createSessionRequest?cri=" + criId);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .build();

        return sendHttpRequest(request).body();
    }

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private String getAccessTokenRequest(String criId) throws IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        coreStubUrl
                                                + "/backend/createTokenRequestPrivateKeyJWT?authorization_code="
                                                + AUTHCODE
                                                + "&cri="
                                                + criId))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .GET()
                        .build();
        return sendHttpRequest(request).body();
    }
}
