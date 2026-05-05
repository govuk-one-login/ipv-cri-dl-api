package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.SignedJWT;
import gov.di_ipv_drivingpermit.model.AuthorisationResponse;
import gov.di_ipv_drivingpermit.model.DocumentCheckResponse;
import gov.di_ipv_drivingpermit.model.DrivingPermitForm;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.ObjectMapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.sendHttpRequest;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivingLicenceAPIPage extends DrivingLicencePageObject {

    private static String clientId;
    private static String sessionRequestBody;
    private static String sessionId;
    private static String state;
    private static String authCode;
    private static String accessToken;
    private static String dateTimeOfRotation;
    private static String extractedPostcode;
    private static String vcHeader;
    private static String vcBody;
    private static final String KID_PREFIX = "did:web:review-d.dev.account.gov.uk#";

    private static Boolean retry;
    private static String drivingLicenceCheckResponse;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.MAPPER;

    private final ConfigurationService configurationService =
            new ConfigurationService(System.getenv("ENVIRONMENT"));

    private final SecretsManagerClient secretsManagerClient =
            SecretsManagerClient.builder()
                    .region(Region.EU_WEST_2)
                    .httpClient(AwsCrtHttpClient.builder().build())
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(DrivingLicenceAPIPage.class);

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
        try {
            ObjectNode jsonObject = (ObjectNode) OBJECT_MAPPER.readTree(claimsJson);
            ObjectNode drivingPermitEntry = OBJECT_MAPPER.createObjectNode();
            drivingPermitEntry.put("personalNumber", drivingPermitPersonalNumber);
            drivingPermitEntry.put("expiryDate", drivingPermitExpiryDate);
            drivingPermitEntry.put("issueDate", drivingPermitIssueDate);
            drivingPermitEntry.put("issuedBy", drivingPermitIssuedBy);
            drivingPermitEntry.put("fullAddress", drivingPermitFullAddress);
            ArrayNode drivingPermitArray = OBJECT_MAPPER.createArrayNode();
            drivingPermitArray.add(drivingPermitEntry);
            ObjectNode sharedClaims = (ObjectNode) jsonObject.get("shared_claims");
            sharedClaims.remove("address");
            sharedClaims.set("drivingPermit", drivingPermitArray);
            jsonObject.put("context", "check_details");
            return jsonObject.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build DVA driving permit JSON", e);
        }
    }

    private String insertDrivingPermitDVLA(
            String claimsJson,
            String drivingPermitPersonalNumber,
            String drivingPermitExpiryDate,
            String drivingPermitIssueDate,
            String drivingPermitIssueNumber,
            String drivingPermitIssuedBy,
            String drivingPermitFullAddress) {
        try {
            ObjectNode jsonObject = (ObjectNode) OBJECT_MAPPER.readTree(claimsJson);
            ObjectNode drivingPermitEntry = OBJECT_MAPPER.createObjectNode();
            drivingPermitEntry.put("personalNumber", drivingPermitPersonalNumber);
            drivingPermitEntry.put("expiryDate", drivingPermitExpiryDate);
            drivingPermitEntry.put("issueDate", drivingPermitIssueDate);
            drivingPermitEntry.put("issueNumber", drivingPermitIssueNumber);
            drivingPermitEntry.put("issuedBy", drivingPermitIssuedBy);
            drivingPermitEntry.put("fullAddress", drivingPermitFullAddress);
            ArrayNode drivingPermitArray = OBJECT_MAPPER.createArrayNode();
            drivingPermitArray.add(drivingPermitEntry);
            ObjectNode sharedClaims = (ObjectNode) jsonObject.get("shared_claims");
            sharedClaims.remove("address");
            sharedClaims.set("drivingPermit", drivingPermitArray);
            jsonObject.put("context", "check_details");
            return jsonObject.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build DVLA driving permit JSON", e);
        }
    }

    public void dlUserIdentityAsJwtString(String criId, Integer rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String jsonString = getAuthorisationJwtFromStub(criId, rowNumber);
        LOGGER.info("jsonString = {}", jsonString);
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        sessionRequestBody = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY = {}", sessionRequestBody);

        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                OBJECT_MAPPER.readValue(sessionRequestBody, new TypeReference<>() {});
        clientId = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID = {}", clientId);
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

        sessionRequestBody = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY FOR AUTH SOURCE = {}", sessionRequestBody);

        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                OBJECT_MAPPER.readValue(sessionRequestBody, new TypeReference<>() {});
        clientId = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID FOR AUTH SOURCE = {}", clientId);
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
        sessionRequestBody = createRequest(coreStubUrl, criId, jsonString);
        LOGGER.info("SESSION_REQUEST_BODY FOR AUTH SOURCE = {}", sessionRequestBody);
        Map<String, String> deserialisedSessionResponse =
                OBJECT_MAPPER.readValue(sessionRequestBody, new TypeReference<>() {});
        clientId = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID FOR AUTH SOURCE = {}", clientId);
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
                        .POST(HttpRequest.BodyPublishers.ofString(sessionRequestBody))
                        .build();
        String sessionResponse = sendHttpRequest(request).body();
        LOGGER.info("sessionResponse = {}", sessionResponse);
        Map<String, String> deserialisedResponse =
                OBJECT_MAPPER.readValue(sessionResponse, new TypeReference<>() {});
        sessionId = deserialisedResponse.get("session_id");
        state = deserialisedResponse.get("state");
        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                OBJECT_MAPPER.readValue(sessionRequestBody, new TypeReference<>() {});
        clientId = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID = {}", clientId);
    }

    public void getSessionIdForDL() {
        LOGGER.info("SESSION_ID = {}", sessionId);
        assertTrue(StringUtils.isNotBlank(sessionId));
    }

    public void dlGetRequestToPersonInfoEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        LOGGER.info("getPrivateAPIEndpoint() ==> {}", privateApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/person-info"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", sessionId)
                        .GET()
                        .build();
        String personInfoSessionResponse = sendHttpRequest(request).body();
        LOGGER.info("person-info endpoint and headers() ==> {}, {}", request, request.headers());
        LOGGER.info("personInfoSessionResponse = {}", personInfoSessionResponse);

        JsonNode rootNode = OBJECT_MAPPER.readTree(personInfoSessionResponse);
        JsonNode addressNode = rootNode.path("address").get(0);
        extractedPostcode = addressNode.path("postalCode").asText();

        LOGGER.info("Extracted postcode = {}", extractedPostcode);
    }

    public void postRequestToDrivingLicenceEndpoint(
            String dlJsonRequestBody, String jsonEditsString)
            throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        JsonNode dlJsonNode =
                OBJECT_MAPPER.readTree(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"));
        String dlInputJsonString = dlJsonNode.toString();

        Map<String, String> jsonEdits = new HashMap<>();
        if (!StringUtils.isEmpty(jsonEditsString)) {
            jsonEdits = OBJECT_MAPPER.readValue(jsonEditsString, Map.class);
        }

        DrivingPermitForm drivingPermitFormJson =
                OBJECT_MAPPER.readValue(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"),
                        DrivingPermitForm.class);
        LOGGER.info("sdfsdf = {}", drivingPermitFormJson);

        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);

            field.set(drivingPermitFormJson, entry.getValue());
        }
        String drivingPermitInputJsonString =
                OBJECT_MAPPER.writeValueAsString(drivingPermitFormJson);

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", sessionId)
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));
        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestBodyBody = {}", drivingPermitInputJsonString);
        LOGGER.info("drivingLicenceRequestBody = {}", dlInputJsonString);
        drivingLicenceCheckResponse = sendHttpRequest(request).body();
        LOGGER.info("drivingLicenceCheckResponse = {}", drivingLicenceCheckResponse);
        DocumentCheckResponse documentCheckResponse =
                OBJECT_MAPPER.readValue(drivingLicenceCheckResponse, DocumentCheckResponse.class);
        retry = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", retry);
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
                OBJECT_MAPPER.readTree(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"));

        Map<String, String> jsonEdits = new HashMap<>();
        if (!StringUtils.isEmpty(jsonEditsString)) {
            jsonEdits = OBJECT_MAPPER.readValue(jsonEditsString, Map.class);
        }

        if (extractedPostcode != null) {
            ((ObjectNode) dlJsonNode).put("postcode", extractedPostcode);
            jsonEdits.put("postcode", extractedPostcode);
        }

        DrivingPermitForm drivingPermitFormJson =
                OBJECT_MAPPER.treeToValue(dlJsonNode, DrivingPermitForm.class);
        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(drivingPermitFormJson, entry.getValue());
        }
        LOGGER.info("Driving Permit Form = {}", drivingPermitFormJson);
        LOGGER.info("Driving Permit Edits = {}", jsonEdits);

        String drivingPermitInputJsonString =
                OBJECT_MAPPER.writeValueAsString(drivingPermitFormJson);
        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", sessionId)
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));

        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestBodyForAuthSource = {}", drivingPermitInputJsonString);
        drivingLicenceCheckResponse = sendHttpRequest(request).body();
        LOGGER.info("drivingLicenceCheckResponseForAuthSource = {}", drivingLicenceCheckResponse);
        DocumentCheckResponse documentCheckResponse =
                OBJECT_MAPPER.readValue(drivingLicenceCheckResponse, DocumentCheckResponse.class);
        retry = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", retry);
    }

    public void
            postRequestToDrivingLicenceEndpointWithInvalidSessionIdAndAPIReturnsOAuthAccessDenied(
                    String invalidHeaderValue, String dlJsonRequestBody, String jsonEditsString)
                    throws IOException,
                            InterruptedException,
                            NoSuchFieldException,
                            IllegalAccessException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        JsonNode dlJsonNode =
                OBJECT_MAPPER.readTree(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"));
        String dlInputJsonString = dlJsonNode.toString();

        Map<String, String> jsonEdits = new HashMap<>();
        if (!StringUtils.isEmpty(jsonEditsString)) {
            jsonEdits = OBJECT_MAPPER.readValue(jsonEditsString, Map.class);
        }

        DrivingPermitForm drivingPermitFormJson =
                OBJECT_MAPPER.readValue(
                        new File("src/test/resources/Data/" + dlJsonRequestBody + ".json"),
                        DrivingPermitForm.class);

        for (Map.Entry<String, String> entry : jsonEdits.entrySet()) {
            Field field = drivingPermitFormJson.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);

            field.set(drivingPermitFormJson, entry.getValue());
        }
        String drivingPermitInputJsonString =
                OBJECT_MAPPER.writeValueAsString(drivingPermitFormJson);

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/check-driving-licence"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(drivingPermitInputJsonString));

        switch (invalidHeaderValue) {
            case "mismatchSessionId" ->
                    builder.setHeader("session_id", UUID.randomUUID().toString());
            case "malformedSessionId" -> builder.setHeader("session_id", "&%^$£$%");
            case "missingSessionId" -> builder.setHeader("session_id", "");
            default -> {
                /*Do Nothing - No Header Provided*/
            }
        }

        HttpRequest request = builder.build();
        LOGGER.info("drivingLicenceRequestHeaders = {}", request.headers());
        LOGGER.info("drivingLicenceRequestBody = {}", dlInputJsonString);
        drivingLicenceCheckResponse = sendHttpRequest(request).body();
        LOGGER.info("drivingLicenceCheckResponse = {}", drivingLicenceCheckResponse);
        String expectedResponseForInvalidSessionId =
                "{\"oauth_error\":{\"error_description\":\"Session not found\",\"error\":\"access_denied\"}}";
        assertEquals(expectedResponseForInvalidSessionId, drivingLicenceCheckResponse);
        DocumentCheckResponse documentCheckResponse =
                OBJECT_MAPPER.readValue(drivingLicenceCheckResponse, DocumentCheckResponse.class);
        retry = documentCheckResponse.getRetry();
        LOGGER.info("RETRY = {}", retry);
    }

    public void
            postRequestToDrivingLicenceEndpointWithInvalidSessionIdAndAPIReturnsOAuthAccessDenied(
                    String invalidHeaderValue, String drivingPermitJsonRequestBody)
                    throws IOException,
                            InterruptedException,
                            NoSuchFieldException,
                            IllegalAccessException {
        postRequestToDrivingLicenceEndpointWithInvalidSessionIdAndAPIReturnsOAuthAccessDenied(
                invalidHeaderValue, drivingPermitJsonRequestBody, "");
    }

    public void retryValueInDLCheckResponse(Boolean retry) {
        assertEquals(DrivingLicenceAPIPage.retry, retry);
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
                                                + state
                                                + "&scope=openid&response_type=code&client_id="
                                                + clientId))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session-id", sessionId)
                        .GET()
                        .build();
        String authCallResponse = sendHttpRequest(request).body();
        LOGGER.info("authCallResponse = {}", authCallResponse);
        AuthorisationResponse deserialisedResponse =
                OBJECT_MAPPER.readValue(authCallResponse, AuthorisationResponse.class);
        if (null != deserialisedResponse.getAuthorizationCode()) {
            authCode = deserialisedResponse.getAuthorizationCode().getValue();
            LOGGER.info("authorizationCode = {}", authCode);
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
                OBJECT_MAPPER.readValue(accessTokenPostCallResponse, new TypeReference<>() {});
        accessToken = deserialisedResponse.get("access_token");
    }

    public void postRequestToDrivingLicenceVCEndpoint()
            throws IOException, InterruptedException, ParseException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/credential/issue"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
        String requestDrivingLicenceVCResponse = sendHttpRequest(request).body();
        LOGGER.info("requestDrivingLicenceVCResponse = {}", requestDrivingLicenceVCResponse);
        SignedJWT signedJWT = SignedJWT.parse(requestDrivingLicenceVCResponse);

        vcHeader = signedJWT.getHeader().toString();
        LOGGER.info("VC Header = {}", vcHeader);

        vcBody = signedJWT.getJWTClaimsSet().toString();
        LOGGER.info("VC Body = {}", vcBody);

        JsonNode jsonHeader;
        try {
            jsonHeader = OBJECT_MAPPER.readTree(vcHeader);
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
                jsonHeader.get("typ").asText());
        Assert.assertEquals(
                "The 'alg' field does not have the expected value",
                "ES256",
                jsonHeader.get("alg").asText());
        String kid = jsonHeader.get("kid").asText();
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

    @Override
    public JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(result);
        return jsonNode.get(vc);
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

    public void ciInDrivingLicenceCriVc(String ci) throws IOException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(vcBody);
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

    public void assertJtiIsPresentAndNotNull() throws IOException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(vcBody);
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

        assertEquals(sb.toString(), drivingLicenceCheckResponse);
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
                    dateTimeOfRotation = describeResponse.lastChangedDate().toString();
                    LOGGER.info("Date time of rotation (last changed date) {}", dateTimeOfRotation);
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
                    LocalDateTime.parse(dateTimeOfRotation, DateTimeFormatter.ISO_DATE_TIME)
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
                    LocalDateTime.parse(dateTimeOfRotation, DateTimeFormatter.ISO_DATE_TIME)
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
                                                + authCode
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
