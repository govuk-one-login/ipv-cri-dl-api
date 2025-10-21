package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
import gov.di_ipv_drivingpermit.model.AuthorisationResponse;
import gov.di_ipv_drivingpermit.model.DocumentCheckResponse;
import gov.di_ipv_drivingpermit.model.DrivingPermitForm;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretResponse;
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
import java.util.*;

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
    private static String extractedPostcode;
    private static String vcHeader;
    private static String vcBody;
    private static final String KID_PREFIX = "did:web:review-d.dev.account.gov.uk#";

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

    public static class DynamoDBService {

        public static Map<String, AttributeValue> getItemByPrimaryKey(
                String tableName, String primaryKey, String primaryKeyValue) {
            DynamoDbClient dynamoDbClient =
                    DynamoDbClient.builder()
                            //
                            // .credentialsProvider(ProfileCredentialsProvider.create())
                            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                            .region(Region.US_WEST_2)
                            .build();
            Map<String, AttributeValue> keyToGet = new HashMap<>();
            keyToGet.put(primaryKey, AttributeValue.builder().s(primaryKeyValue).build());
            GetItemRequest request =
                    GetItemRequest.builder().tableName(tableName).key(keyToGet).build();

            try {
                GetItemResponse response = dynamoDbClient.getItem(request);
                if (response.hasItem()) {
                    return response.item();
                } else {
                    System.out.println("No item found with the given primary key.");
                    return null;
                }
            } catch (DynamoDbException e) {
                System.err.println("Unable to get item: " + e.getMessage());
                return null;
            } finally {
                dynamoDbClient.close();
            }
        }
    }

    public String getAuthorisationJwtFromStub(String criId, Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        if (coreStubUrl == null) {
            throw new IllegalArgumentException("Environment variable IPV_CORE_STUB_URL is not set");
        }
        return getClaimsForUser(coreStubUrl, criId, rowNumber);
    }

    public String getAuthorisationJwtFromStubWithDVADrivingPermit(
            String context,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress,
            String criId,
            Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        if (coreStubUrl == null) {
            throw new IllegalArgumentException("Environment variable IPV_CORE_STUB_URL is not set");
        }
        String claimsJson = getClaimsForUser(coreStubUrl, criId, rowNumber, context);
        return insertDrivingPermitDVA(
                claimsJson,
                drivingPermitPersonalNumber,
                drivingPermitExpiryDate,
                drivingPermitIssueDate,
                drivingPermitIssuedBy,
                drivingPermitFullAddress);
    }

    public String getAuthorisationJwtFromStubWithDVLADrivingPermit(
            String context,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssueNumber,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress,
            String criId,
            Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        if (coreStubUrl == null) {
            throw new IllegalArgumentException("Environment variable IPV_CORE_STUB_URL is not set");
        }
        String claimsJson = getClaimsForUser(coreStubUrl, criId, rowNumber, context);
        return insertDrivingPermitDVLA(
                claimsJson,
                drivingPermitPersonalNumber,
                drivingPermitExpiryDate,
                drivingPermitIssueDate,
                drivingPermitIssueNumber,
                drivingPermitIssuedBy,
                drivingPermitFullAddress);
    }

    private String insertDrivingPermitDVA(
            String claimsJson,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress) {
        JSONObject jsonObject = new JSONObject(claimsJson);
        JSONObject drivingPermitEntry = new JSONObject();

        drivingPermitEntry.put("personalNumber", drivingPermitPersonalNumber);
        drivingPermitEntry.put("expiryDate", drivingPermitExpiryDate);
        drivingPermitEntry.put("issueDate", drivingPermitIssueDate);
        drivingPermitEntry.put("issuedBy", drivingPermitIssuedBy);
        drivingPermitEntry.put("fullAddress", drivingPermitFullAddress);

        JSONArray drivingPermitArray = new JSONArray();
        drivingPermitArray.put(drivingPermitEntry);

        JSONObject sharedClaims = jsonObject.getJSONObject("shared_claims");

        sharedClaims.remove("address");

        sharedClaims.put("drivingPermit", drivingPermitArray);
        jsonObject.put("context", "check_details");

        return jsonObject.toString();
    }

    private String insertDrivingPermitDVLA(
            String claimsJson,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssueNumber,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress) {
        JSONObject jsonObject = new JSONObject(claimsJson);
        JSONObject drivingPermitEntry = new JSONObject();

        drivingPermitEntry.put("personalNumber", drivingPermitPersonalNumber);
        drivingPermitEntry.put("expiryDate", drivingPermitExpiryDate);
        drivingPermitEntry.put("issueDate", drivingPermitIssueDate);
        drivingPermitEntry.put("issueNumber", drivingPermitIssueNumber);
        drivingPermitEntry.put("issuedBy", drivingPermitIssuedBy);
        drivingPermitEntry.put("fullAddress", drivingPermitFullAddress);

        JSONArray drivingPermitArray = new JSONArray();
        drivingPermitArray.put(drivingPermitEntry);
        JSONObject sharedClaims = jsonObject.getJSONObject("shared_claims");
        sharedClaims.remove("address");
        sharedClaims.put("drivingPermit", drivingPermitArray);
        jsonObject.put("context", "check_details");

        return jsonObject.toString();
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

    public void dlUserIdentityDVAWithDrivingPermitAsJwtString(
            String context,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress,
            String criId,
            Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String jsonString =
                getAuthorisationJwtFromStubWithDVADrivingPermit(
                        context,
                        drivingPermitPersonalNumber,
                        drivingPermitExpiryDate,
                        drivingPermitIssueDate,
                        drivingPermitIssuedBy,
                        drivingPermitFullAddress,
                        criId,
                        rowNumber);
        LOGGER.info("jsonStringDrivingPermitAuthSource = {}", jsonString);

        String coreStubUrl = configurationService.getCoreStubUrl(false);

        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY FOR AUTH SOURCE = {}", SESSION_REQUEST_BODY);

        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                objectMapper.readValue(SESSION_REQUEST_BODY, new TypeReference<>() {});
        CLIENT_ID = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID FOR AUTH SOURCE = {}", CLIENT_ID);
    }

    public void dlUserIdentityDVLAWithDrivingPermitAsJwtString(
            String context,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssueNumber,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress,
            String criId,
            Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String jsonString =
                getAuthorisationJwtFromStubWithDVLADrivingPermit(
                        context,
                        drivingPermitPersonalNumber,
                        drivingPermitExpiryDate,
                        drivingPermitIssueDate,
                        drivingPermitIssueNumber,
                        drivingPermitIssuedBy,
                        drivingPermitFullAddress,
                        criId,
                        rowNumber);
        LOGGER.info("jsonStringDrivingPermitAuthSource = {}", jsonString);
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY FOR AUTH SOURCE = {}", SESSION_REQUEST_BODY);
        Map<String, String> deserialisedSessionResponse =
                objectMapper.readValue(SESSION_REQUEST_BODY, new TypeReference<>() {});
        CLIENT_ID = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID FOR AUTH SOURCE = {}", CLIENT_ID);
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

    public void dlGetRequestToPersonInfoEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        LOGGER.info("getPrivateAPIEndpoint() ==> {}", privateApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/person-info"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", SESSION_ID)
                        .GET()
                        .build();
        String personInfoSessionResponse = sendHttpRequest(request).body();
        LOGGER.info("person-info endpoint and headers() ==> {}, {}", request, request.headers());
        LOGGER.info("personInfoSessionResponse = {}", personInfoSessionResponse);

        JsonNode rootNode = objectMapper.readTree(personInfoSessionResponse);
        JsonNode addressNode = rootNode.path("address").get(0);
        extractedPostcode = addressNode.path("postalCode").asText();

        LOGGER.info("Extracted postcode = {}", extractedPostcode);
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
        LOGGER.info("sdfsdf = {}", drivingPermitFormJson);

        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);

            field.set(drivingPermitFormJson, entry.getValue());
        }
        String drivingPermitInputJsonString =
                objectMapper.writeValueAsString(drivingPermitFormJson);

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", SESSION_ID)
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));
        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestBodyBody = {}", drivingPermitInputJsonString);
        LOGGER.info("drivingLicenceRequestBody = {}", dlInputJsonString);
        DRIVING_LICENCE_CHECK_RESPONSE = sendHttpRequest(request).body();
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

    public void postRequestToDrivingLicenceEndpointWithPersonInfoDetails(
            String dlJsonRequestBody, String jsonEditsString)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {

        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();

        JsonNode dlJsonNode =
                objectMapper.readTree(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"));

        Map<String, String> jsonEdits = new HashMap<>();
        if (!StringUtils.isEmpty(jsonEditsString)) {
            jsonEdits = objectMapper.readValue(jsonEditsString, Map.class);
        }

        if (extractedPostcode != null) {
            ((ObjectNode) dlJsonNode).put("postcode", extractedPostcode);
            jsonEdits.put("postcode", extractedPostcode);
        }

        DrivingPermitForm drivingPermitFormJson =
                objectMapper.treeToValue(dlJsonNode, DrivingPermitForm.class);
        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(drivingPermitFormJson, entry.getValue());
        }
        LOGGER.info("Driving Permit Form = {}", drivingPermitFormJson);
        LOGGER.info("Driving Permit Edits = {}", jsonEdits);

        String drivingPermitInputJsonString =
                objectMapper.writeValueAsString(drivingPermitFormJson);
        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", SESSION_ID)
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));

        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestBodyForAuthSource = {}", drivingPermitInputJsonString);
        DRIVING_LICENCE_CHECK_RESPONSE = sendHttpRequest(request).body();
        LOGGER.info(
                "drivingLicenceCheckResponseForAuthSource = {}", DRIVING_LICENCE_CHECK_RESPONSE);
        DocumentCheckResponse documentCheckResponse =
                objectMapper.readValue(DRIVING_LICENCE_CHECK_RESPONSE, DocumentCheckResponse.class);
        RETRY = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", RETRY);
    }

    public void postRequestToDrivingLicenceEndpointWithInvalidSessionId(
            String invalidHeaderValue, String dlJsonRequestBody, String jsonEditsString)
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

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));

        switch (invalidHeaderValue) {
            case "mismatchSessionId" -> builder.setHeader(
                    "session_id", UUID.randomUUID().toString());
            case "malformedSessionId" -> builder.setHeader("session_id", "&%^$£$%");
            case "missingSessionId" -> builder.setHeader("session_id", "");
            default -> {
                /*Do Nothing - No Header Provided*/
            }
        }

        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestHeaders = {}", request.headers());
        LOGGER.info("drivingLicenceRequestBody = {}", dlInputJsonString);
        DRIVING_LICENCE_CHECK_RESPONSE = sendHttpRequest(request).body();
        LOGGER.info("drivingLicenceCheckResponse = {}", DRIVING_LICENCE_CHECK_RESPONSE);
        String expectedResponseForInvalidSessionId =
                "{\"oauth_error\":{\"error_description\":\"Session not found\",\"error\":\"access_denied\"}}";
        assertEquals(expectedResponseForInvalidSessionId, DRIVING_LICENCE_CHECK_RESPONSE);
        DocumentCheckResponse documentCheckResponse =
                objectMapper.readValue(DRIVING_LICENCE_CHECK_RESPONSE, DocumentCheckResponse.class);
        RETRY = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", RETRY);
    }

    public void postRequestToDrivingLicenceEndpointWithInvalidSessionId(
            String invalidHeaderValue, String drivingPermitJsonRequestBody)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        postRequestToDrivingLicenceEndpointWithInvalidSessionId(
                invalidHeaderValue, drivingPermitJsonRequestBody, "");
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

        JSONObject jsonHeader;
        try {
            jsonHeader = new JSONObject(vcHeader);
        } catch (Exception e) {
            LOGGER.error("Failed to parse VC Header as JSON", e);
            throw new AssertionError("Failed to parse VC Header as JSON", e);
        }
        String[] expectedFields = {"kid", "typ", "alg"};
        for (String field : expectedFields) {
            Assert.assertTrue(
                    "Field '" + field + "' is missing in the VC Header", jsonHeader.has(field));
        }
        Assert.assertEquals(
                "The 'typ' field does not have the expected value",
                "JWT",
                jsonHeader.getString("typ"));
        Assert.assertEquals(
                "The 'alg' field does not have the expected value",
                "ES256",
                jsonHeader.getString("alg"));
        String kid = jsonHeader.getString("kid");
        Assert.assertTrue(
                "The 'kid' field does not start with the expected prefix",
                kid.startsWith(KID_PREFIX));
        String kidSuffix = kid.substring(KID_PREFIX.length());
        Assert.assertFalse("The 'kid' field suffix should not be empty", kidSuffix.isEmpty());
    }

    public void postRequestToDrivingLicenceVCEndpointWithInvalidAuthCode()
            throws IOException, InterruptedException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        String randomAccessToken = UUID.randomUUID().toString();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/credential/issue"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + randomAccessToken)
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
        String requestDrivingLicenceVCResponse = sendHttpRequest(request).body();
        LOGGER.info("requestDrivingLicenceVCResponse = {}", requestDrivingLicenceVCResponse);
        String expectedResponseForInvalidAuthCode =
                "{\"oauth_error\":{\"error_description\":\"Access denied by resource owner or authorization server\",\"error\":\"access_denied\"}}";
        assertEquals(expectedResponseForInvalidAuthCode, requestDrivingLicenceVCResponse);
    }

    public void validityScoreAndStrengthScoreInVC(String validityScore, String strengthScore)
            throws IOException {
        scoreIs(validityScore, strengthScore, vcBody);
    }

    public JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        return jsonNode.get(vc);
    }

    public void scoreIs(
            String expectedValidityScore, String expectedStrengthScore, String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = " + result);
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
        LOGGER.info("result = " + result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String validityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(expectedValidityScore, validityScore);

        String strengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(expectedStrengthScore, strengthScore);

        String type = evidence.get(0).get("type").asText();
        assertEquals(expectedType, type);
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

    public void assertCheckDetailsWithinVc(
            String checkMethod,
            String identityCheckPolicy,
            String checkDetailsType,
            String drivingLicenceCRIVC)
            throws IOException {
        JsonNode vcNode = getJsonNode(drivingLicenceCRIVC, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");
        JsonNode firstItemInEvidenceArray = evidence.get(0);
        LOGGER.info("firstItemInEvidenceArray = " + firstItemInEvidenceArray);
        if (checkDetailsType.equals("success")) {
            JsonNode checkDetailsNode = firstItemInEvidenceArray.get("checkDetails");
            JsonNode checkMethodNode = checkDetailsNode.get(0).get("checkMethod");
            String actualCheckMethod = checkMethodNode.asText();
            LOGGER.info("actualCheckMethod = " + actualCheckMethod);
            JsonNode identityCheckPolicyNode = checkDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = " + actualidentityCheckPolicy);
            JsonNode activityFromNode = checkDetailsNode.get(0).get("activityFrom");
            String actualactivityFrom = activityFromNode.asText();
            LOGGER.info("actualactivityFrom = " + actualactivityFrom);
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
            LOGGER.info("actualCheckMethod = " + actualCheckMethod);
            JsonNode identityCheckPolicyNode =
                    failedCheckDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = " + actualidentityCheckPolicy);
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

    public void assertJtiIsPresentAndNotNull() throws IOException {
        JsonNode jsonNode = objectMapper.readTree(vcBody);
        JsonNode jtiNode = jsonNode.get("jti");
        LOGGER.info("jti = {}", jtiNode.asText());

        assertNotNull(jtiNode.asText());
    }

    private String getClaimsForUser(String baseUrl, String criId, int userDataRowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        return getClaimsForUser(baseUrl, criId, userDataRowNumber, null);
    }

    private String getClaimsForUser(
            String baseUrl, String criId, Integer userDataRowNumber, String context)
            throws URISyntaxException, IOException, InterruptedException {

        String uriInput = baseUrl + "/backend/generateInitialClaimsSet?cri=" + criId;

        if (userDataRowNumber != null) {
            uriInput += "&rowNumber=" + userDataRowNumber;
        }

        if (context != null) {
            uriInput += "&context=" + context;
        }
        var url = new URI(uriInput);

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
            String criInternalErrorCode, String criInternalErrorMessage) {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append(
                "\"oauth_error\":{"
                        + "\"error_description\":\"Unexpected server error\","
                        + "\"error\":\"server_error\""
                        + "}");

        if (criInternalErrorCode != null && criInternalErrorMessage != null) {
            // Asserting debug reply
            sb.append(",");
            sb.append("\"cri_internal_error_code\":" + "\"" + criInternalErrorCode + "\"");
            sb.append(",");
            sb.append("\"cri_internal_error_message\":" + "\"" + criInternalErrorMessage + "\"");
        }

        sb.append("}");

        assertEquals(sb.toString(), DRIVING_LICENCE_CHECK_RESPONSE);
    }

    public void getLastTestedTime(String secretName) {
        String parameterNameFormat = "/%s/%s";
        String stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        if (stackParameterPrefix != null) {
            String secretId = String.format(parameterNameFormat, stackParameterPrefix, secretName);
            LOGGER.info("Attempting to describe secret: {}", secretId);

            try {
                DescribeSecretRequest describeRequest =
                        DescribeSecretRequest.builder().secretId(secretId).build();

                DescribeSecretResponse describeResponse =
                        secretsManagerClient.describeSecret(describeRequest);

                if (describeResponse.lastChangedDate() != null) {
                    DATE_TIME_OF_ROTATION = describeResponse.lastChangedDate().toString();
                    LOGGER.info(
                            "Date time of rotation (last changed date) {}", DATE_TIME_OF_ROTATION);
                } else {
                    LOGGER.warn("Last changed date not available for secret {}", secretId);
                }
                LOGGER.info("Rotation Enabled: {}", describeResponse.rotationEnabled());
                LOGGER.info("Next Rotation Date: {}", describeResponse.nextRotationDate());
                if (describeResponse.lastRotatedDate() != null) {
                    LOGGER.info("Last Rotated Date: {}", describeResponse.lastRotatedDate());
                }
                if (describeResponse.tags() != null && !describeResponse.tags().isEmpty()) {
                    LOGGER.info("Tags: {}", describeResponse.tags());
                }

            } catch (Exception e) {
                LOGGER.error(
                        "Error retrieving secret details for {}: {}", secretId, e.getMessage());
            }

        } else {
            LOGGER.info("IGNORING TEST AS IT WAS RUN LOCALLY WITHOUT AWS CONTEXT");
        }
    }

    public void passwordHasRotatedSuccessfully() {
        String parameterNameFormat = "/%s/%s";

        String stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        if (stackParameterPrefix != null) {
            String secretId =
                    String.format(parameterNameFormat, stackParameterPrefix, "DVLA/password");
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

    public void apiKeyHasRotatedSuccessfully() {
        String parameterNameFormat = "/%s/%s";
        String stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        if (stackParameterPrefix != null) {
            String secretId =
                    String.format(parameterNameFormat, stackParameterPrefix, "DVLA/apiKey");
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

            assertTrue(
                    valueResponse.versionStages().contains("AWSCURRENT"),
                    "Retrieved secret version is not associated with AWSCURRENT stage.");
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
