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
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.sendHttpRequest;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicenceAPIPage extends DrivingLicencePageObject {

    private static String CLIENT_ID;
    private static String SESSION_REQUEST_BODY;
    private static String SESSION_ID;
    private static String STATE;
    private static String AUTHCODE;
    private static String ACCESS_TOKEN;
    private static String DATE_TIME_OF_ROTATION;

    private static String vcHeader;
    private static String vcBody;

    private static Boolean RETRY;
    private static String DRIVING_LICENCE_CHECK_RESPONSE;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModules(new JavaTimeModule());

    private final ConfigurationService configurationService =
            new ConfigurationService(System.getenv("ENVIRONMENT"));

    private final SecretsManagerClient secretsManagerClient =
            SecretsManagerClient.builder()
                    .region(Region.EU_WEST_2)
                    .httpClient(UrlConnectionHttpClient.create())
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
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
        LOGGER.info("jsonString = {}", jsonString);
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY = {}", SESSION_REQUEST_BODY);

        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                objectMapper.readValue(SESSION_REQUEST_BODY, new TypeReference<>() {});
        CLIENT_ID = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID = {}", CLIENT_ID);
    }

    public void dlPostRequestToSessionEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        LOGGER.info("getPrivateAPIEndpoint() ==> {}", privateApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/session"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("X-Forwarded-For", "123456789")
                        .POST(HttpRequest.BodyPublishers.ofString(SESSION_REQUEST_BODY))
                        .build();
        String sessionResponse = sendHttpRequest(request).body();
        LOGGER.info("sessionResponse = {}", sessionResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(sessionResponse, new TypeReference<>() {});
        SESSION_ID = deserialisedResponse.get("session_id");
        STATE = deserialisedResponse.get("state");
        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                objectMapper.readValue(SESSION_REQUEST_BODY, new TypeReference<>() {});
        CLIENT_ID = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID = {}", CLIENT_ID);
    }

    public void getSessionIdForDL() {
        LOGGER.info("SESSION_ID = {}", SESSION_ID);
        assertTrue(StringUtils.isNotBlank(SESSION_ID));
    }

    public void postRequestToDrivingLicenceEndpoint(
            String dlJsonRequestBody, String jsonEditsString)
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
        LOGGER.info("drivingLicenceRequestBody = {}", dlInputJsonString);
        DRIVING_LICENCE_CHECK_RESPONSE = sendHttpRequest(request.build()).body();
        LOGGER.info("drivingLicenceCheckResponse = {}", DRIVING_LICENCE_CHECK_RESPONSE);
        DocumentCheckResponse documentCheckResponse =
                objectMapper.readValue(DRIVING_LICENCE_CHECK_RESPONSE, DocumentCheckResponse.class);
        RETRY = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", RETRY);
    }

    public void postRequestToDrivingLicenceEndpoint(String drivingPermitJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpoint(drivingPermitJsonRequestBody, "");
    }

    public void retryValueInDLCheckResponse(Boolean retry) {
        assertEquals(RETRY, retry);
    }

    public void getAuthorisationCodeforDL() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        String coreStubUrl = configurationService.getCoreStubUrl(false);

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
                                                + CLIENT_ID))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session-id", SESSION_ID)
                        .GET()
                        .build();
        String authCallResponse = sendHttpRequest(request).body();
        LOGGER.info("authCallResponse = {}", authCallResponse);
        AuthorisationResponse deserialisedResponse =
                objectMapper.readValue(authCallResponse, AuthorisationResponse.class);
        if (null != deserialisedResponse.getAuthorizationCode()) {
            AUTHCODE = deserialisedResponse.getAuthorizationCode().getValue();
            LOGGER.info("authorizationCode = {}", AUTHCODE);
        }
    }

    public void postRequestToAccessTokenEndpointForDL(String criId)
            throws IOException, InterruptedException {
        String accessTokenRequestBody = getAccessTokenRequest(criId);
        LOGGER.info("Access Token Request Body = {}", accessTokenRequestBody);
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        LOGGER.info("getPublicAPIEndpoint() ==> {}", publicApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/token"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(accessTokenRequestBody))
                        .build();
        String accessTokenPostCallResponse = sendHttpRequest(request).body();
        LOGGER.info("accessTokenPostCallResponse = {}", accessTokenPostCallResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(accessTokenPostCallResponse, new TypeReference<>() {});
        ACCESS_TOKEN = deserialisedResponse.get("access_token");
    }

    public void postRequestToDrivingLicenceVCEndpoint()
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
        LOGGER.info("requestDrivingLicenceVCResponse = {}", requestDrivingLicenceVCResponse);
        SignedJWT signedJWT = SignedJWT.parse(requestDrivingLicenceVCResponse);

        vcHeader = signedJWT.getHeader().toString();
        LOGGER.info("VC Header = {}", vcHeader);

        vcBody = signedJWT.getJWTClaimsSet().toString();
        LOGGER.info("VC Body = {}", vcBody);
    }

    public void validityScoreAndStrengthScoreInVC(String validityScore, String strengthScore)
            throws IOException {
        scoreIs(validityScore, strengthScore, vcBody);
    }

    public void ciInDrivingLicenceCriVc(String ci) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(vcBody);
        JsonNode evidenceArray = jsonNode.get("vc").get("evidence");
        JsonNode ciInEvidenceArray = evidenceArray.get(0);
        LOGGER.info("ciInEvidenceArray = {}", ciInEvidenceArray);
        JsonNode ciNode = ciInEvidenceArray.get("ci").get(0);
        String actualCI = ciNode.asText();
        Assert.assertEquals(ci, actualCI);
    }

    public void assertCheckDetails(
            String checkMethod, String identityCheckPolicy, String checkDetailsType)
            throws IOException {
        assertCheckDetailsWithinVc(checkMethod, identityCheckPolicy, checkDetailsType, vcBody);
    }

    public void assertJtiIsPresentAndNotNull() throws IOException {
        JsonNode jsonNode = objectMapper.readTree(vcBody);
        JsonNode jtiNode = jsonNode.get("jti");
        LOGGER.info("jti = {}", jtiNode.asText());

        assertNotNull(jtiNode.asText());
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

        LOGGER.info("URL =>> {}", url);

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
        checkDebugDrivingPermitResponseContainsException(null, null);
    }

    public void checkDebugDrivingPermitResponseContainsException(
            String cri_internal_error_code, String cri_internal_error_message) {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append(
                "\"oauth_error\":{"
                        + "\"error_description\":\"Unexpected server error\","
                        + "\"error\":\"server_error\""
                        + "}");

        if (cri_internal_error_code != null && cri_internal_error_message != null) {
            // Asserting debug reply
            sb.append(",");
            sb.append("\"cri_internal_error_code\":" + "\"" + cri_internal_error_code + "\"");
            sb.append(",");
            sb.append("\"cri_internal_error_message\":" + "\"" + cri_internal_error_message + "\"");
        }

        sb.append("}");

        assertEquals(sb.toString(), DRIVING_LICENCE_CHECK_RESPONSE);
    }

    public void getLastTestedTime() {
        String PARAMETER_NAME_FORMAT = "/%s/%s";

        String stackParameterPrefix = System.getenv("AWS_STACK_NAME");
        if (stackParameterPrefix != null) {

            String secretId =
                    String.format(PARAMETER_NAME_FORMAT, stackParameterPrefix, "DVLA/password");
            LOGGER.info("{} {}", "getStackSecretValue", secretId);

            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSCURRENT")
                            .build();

            GetSecretValueResponse valueResponse =
                    secretsManagerClient.getSecretValue(valueRequest);

            DATE_TIME_OF_ROTATION = valueResponse.createdDate().toString();
            LOGGER.info("Date time of rotation {}", DATE_TIME_OF_ROTATION);
        } else {
            LOGGER.info("IGNORING TEST AS IT WAS RUN LOCALLY WITHOUT AWS CONTEXT");
        }
    }

    public void passwordHasRotatedSuccessfully() {
        String PARAMETER_NAME_FORMAT = "/%s/%s";

        String stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        if (stackParameterPrefix != null) {
            String secretId =
                    String.format(PARAMETER_NAME_FORMAT, stackParameterPrefix, "DVLA/password");
            LOGGER.info("{} {}", "getStackSecretValue", secretId);

            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSCURRENT")
                            .build();

            GetSecretValueResponse valueResponse =
                    secretsManagerClient.getSecretValue(valueRequest);

            assertTrue(
                    LocalDateTime.parse(DATE_TIME_OF_ROTATION, DateTimeFormatter.ISO_DATE_TIME)
                            .isAfter(LocalDateTime.now().minusHours(4)));
            String password = valueResponse.secretString();
            int specialCharCount = 0;
            int digitalCharCount = 0;
            int upperCaseCharCount = 0;
            int lowerCaseCharCount = 0;
            for (char c : password.toCharArray()) {
                if (c >= 33 && c <= 47) {
                    specialCharCount++;
                }
            }
            assertTrue(specialCharCount >= 2, "Password validation failed in Passay");
            for (char c : password.toCharArray()) {
                if (c >= 48 && c <= 57) {
                    digitalCharCount++;
                }
            }
            assertTrue(digitalCharCount >= 2, "Password Validation failed in Passay");

            for (char c : password.toCharArray()) {
                if (c >= 65 && c <= 90) {
                    upperCaseCharCount++;
                }
            }
            assertTrue(upperCaseCharCount >= 4, "Password Validation failed in Passay");

            for (char c : password.toCharArray()) {
                if (c >= 97 && c <= 122) {
                    lowerCaseCharCount++;
                }
            }
            assertTrue(lowerCaseCharCount >= 6, "Password Validation failed in Passay");
        } else {
            LOGGER.info("IGNORING TEST AS IT WAS RUN LOCALLY WITHOUT AWS CONTEXT");
        }
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
