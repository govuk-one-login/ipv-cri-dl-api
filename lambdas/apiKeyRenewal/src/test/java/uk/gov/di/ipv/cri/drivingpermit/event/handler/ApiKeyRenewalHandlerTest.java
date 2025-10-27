// package uk.gov.di.ipv.cri.drivingpermit.event.handler;
//
// import com.amazonaws.services.lambda.runtime.Context;
// import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
// import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
// import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
// import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
// import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
// import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
// import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
// import uk.gov.di.ipv.cri.common.library.util.EventProbe;
// import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangeApiKeyService;
// import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
// import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
// import uk.gov.di.ipv.cri.drivingpermit.library.domain.DvlaFormFields;
// import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
// import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoMoreInteractions;
// import static org.mockito.Mockito.when;
// import static
// uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT;
//
// @ExtendWith(MockitoExtension.class)
// class ApiKeyRenewalHandlerTest {
//
//    @Mock private SecretsManagerClient mockSecretsManagerClient;
//
//    @Mock private ChangeApiKeyService mockChangeApiKeyService;
//
//    @Mock private TokenRequestService mockTokenRequestService;
//
//    @Mock private EventProbe mockEventProbe;
//
//    @Mock private SecretsManagerRotationEvent mockInput;
//
//    @Mock private Context mockContext;
//
//    @Mock private DriverMatchService mockDriverMatchService;
//
//    @Mock private DvlaConfiguration mockDvlaConfiguration;
//
//    private ApiKeyRenewalHandler apiKeyRenewalHandler;
//
//    @BeforeEach
//    public void setup() {
//
//        this.apiKeyRenewalHandler =
//                new ApiKeyRenewalHandler(
//                        mockSecretsManagerClient,
//                        mockChangeApiKeyService,
//                        mockTokenRequestService,
//                        mockDriverMatchService,
//                        mockEventProbe,
//                        mockDvlaConfiguration);
//    }
//
//    @Test
//    void whenCreateSecretHasStartedThenNewApiKeyGeneratedAndSaved()
//            throws OAuthErrorResponseException, JsonProcessingException {
//
//        GetSecretValueRequest initialSecretValueRequest =
//                GetSecretValueRequest.builder()
//                        .secretId("/stackName/DVLA/apiKey")
//                        .versionStage("AWSPENDING")
//                        .build();
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("new_api_key").build();
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.getSecretValue(initialSecretValueRequest))
//                .thenThrow(new SecretNotFoundException("Api Key not found"))
//                .thenReturn(secretValueResponse);
//
//        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
//                .thenReturn("token_value");
//        when(mockDvlaConfiguration.getApiKey()).thenReturn("existing_api_key");
//
//        when(mockChangeApiKeyService.sendApiKeyChangeRequest("existing_api_key", "token_value"))
//                .thenReturn("new_api_key");
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        ArgumentCaptor<PutSecretValueRequest> putSecretValueCaptor =
//                ArgumentCaptor.forClass(PutSecretValueRequest.class);
//
//        verify(mockSecretsManagerClient).putSecretValue(putSecretValueCaptor.capture());
//        PutSecretValueRequest putSecretValueRequest = putSecretValueCaptor.getValue();
//        assertNotNull(putSecretValueRequest);
//        assertNotNull(putSecretValueRequest.secretString());
//        assertEquals("/stackName/DVLA/apiKey", putSecretValueRequest.secretId());
//        assertEquals("AWSPENDING", putSecretValueRequest.versionStages().get(0));
//    }
//
//    @Test
//    void whenTestSecretStepCalledThenPerformMatchIsVerified() throws OAuthErrorResponseException {
//
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("new_api_key").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//        when(mockTokenRequestService.requestToken(true, Strategy.NO_CHANGE))
//                .thenReturn("token_value");
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        verify(mockDriverMatchService)
//                .performMatch(
//                        any(DvlaFormFields.class),
//                        eq("token_value"),
//                        eq("new_api_key"),
//                        eq(Strategy.NO_CHANGE));
//    }
//
//    @Test
//    void whenFinishSecretStepCalledThenSecretUpdatedSuccessfully() {
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.updateSecret(any(UpdateSecretRequest.class)))
//                .thenReturn(null);
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("new_api_key").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        ArgumentCaptor<UpdateSecretRequest> updateSecretRequestArgumentCaptor =
//                ArgumentCaptor.forClass(UpdateSecretRequest.class);
//
//
// verify(mockSecretsManagerClient).updateSecret(updateSecretRequestArgumentCaptor.capture());
//        UpdateSecretRequest updateSecretValueRequest =
// updateSecretRequestArgumentCaptor.getValue();
//        assertNotNull(updateSecretValueRequest);
//        assertNotNull(updateSecretValueRequest.secretString());
//        assertEquals("/stackName/DVLA/apiKey", updateSecretValueRequest.secretId());
//    }
//
//    @Test
//    void whenApiKeyRenewalHandlerIsCalledAndNoSecretIdIsPresentThenThrowRuntimeException()
//            throws OAuthErrorResponseException, JsonProcessingException {
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.putSecretValue(any(PutSecretValueRequest.class)))
//                .thenThrow(
//                        SecretsManagerException.builder()
//                                .message("Exception putting in secret")
//                                .build());
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString(null).build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//        when(mockDvlaConfiguration.getApiKey()).thenReturn("existing_api_key");
//        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
//                .thenReturn("token_value");
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn(null);
//        when(mockChangeApiKeyService.sendApiKeyChangeRequest("existing_api_key", "token_value"))
//                .thenReturn("new_api_key");
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception putting in secret");
//
//        ArgumentCaptor<PutSecretValueRequest> putSecretValueCaptor =
//                ArgumentCaptor.forClass(PutSecretValueRequest.class);
//
//        verify(mockSecretsManagerClient).putSecretValue(putSecretValueCaptor.capture());
//        PutSecretValueRequest putSecretValueRequest = putSecretValueCaptor.getValue();
//        assertNotNull(putSecretValueRequest);
//        assertNotNull(putSecretValueRequest.secretString());
//        assertNull(null, putSecretValueRequest.secretId());
//        assertEquals("AWSPENDING", putSecretValueRequest.versionStages().get(0));
//    }
//
//    @Test
//    void whenDVLAIsCalledAndApiKeyRenewalRequestFailsThenThrowRuntimeException()
//            throws OAuthErrorResponseException, JsonProcessingException {
//
//        GetSecretValueRequest initialSecretValueRequest =
//                GetSecretValueRequest.builder()
//                        .secretId("/stackName/DVLA/apiKey")
//                        .versionStage("AWSPENDING")
//                        .build();
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.getSecretValue(initialSecretValueRequest))
//                .thenThrow(new SecretNotFoundException("Api Key not found"));
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//        when(mockDvlaConfiguration.getApiKey()).thenReturn("existing_api_key");
//        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
//                .thenReturn("authorization_token_value");
//
//        doThrow(new OAuthErrorResponseException(500, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
//                .when(mockChangeApiKeyService)
//                .sendApiKeyChangeRequest("existing_api_key", "authorization_token_value");
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception performing password renewal request");
//
//        verify(mockChangeApiKeyService)
//                .sendApiKeyChangeRequest("existing_api_key", "authorization_token_value");
//        verify(mockSecretsManagerClient).getSecretValue(any(GetSecretValueRequest.class));
//
//        verifyNoMoreInteractions(mockChangeApiKeyService, mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenTestSecretApiKeyIsNullThenDVLAReturns401WhichTriggersException()
//            throws OAuthErrorResponseException, JsonProcessingException {
//
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        doThrow(new OAuthErrorResponseException(401, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
//                .when(mockChangeApiKeyService)
//                .sendApiKeyChangeRequest(eq(null), anyString());
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
//                .thenReturn("token_value");
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString(null).build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse)
//                .thenThrow(SecretNotFoundException.class);
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Error as new password value was null when password retrieved from Secrets
// Manager");
//
//        verify(mockChangeApiKeyService).sendApiKeyChangeRequest(eq(null), anyString());
//    }
//
//    @Test
//    void whenApiKeyRenewalHandlerIsCalledAndDVLAFailsToUpdateApiKeyThenThrowRuntimeException()
//            throws OAuthErrorResponseException, JsonProcessingException {
//
//        GetSecretValueRequest initialSecretValueRequest =
//                GetSecretValueRequest.builder()
//                        .secretId("/stackName/DVLA/apiKey")
//                        .versionStage("AWSPENDING")
//                        .build();
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.getSecretValue(initialSecretValueRequest))
//                .thenThrow(new SecretNotFoundException("Api Key not found"));
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//        when(mockDvlaConfiguration.getApiKey()).thenReturn("existing_api_key");
//        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
//                .thenReturn("authorization_token_value");
//        when(mockChangeApiKeyService.sendApiKeyChangeRequest(
//                        "existing_api_key", "authorization_token_value"))
//                .thenReturn("new_api_key");
//
//        PutSecretValueRequest secretRequest =
//                PutSecretValueRequest.builder()
//                        .secretId(mockInput.getSecretId())
//                        .secretString("new_api_key")
//                        .versionStages("AWSPENDING")
//                        .build();
//
//        when(mockSecretsManagerClient.putSecretValue(secretRequest))
//                .thenThrow(RuntimeException.class);
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception updating password");
//
//        verify(mockChangeApiKeyService)
//                .sendApiKeyChangeRequest("existing_api_key", "authorization_token_value");
//
//        verifyNoMoreInteractions(mockChangeApiKeyService, mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenGetValueIsCalledAndRetrievesExistingSecretThenRotationContinuesToTest() {
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("/stackName/DVLA/apiKey");
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("existing_api_key").build();
//
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        ArgumentCaptor<UpdateSecretRequest> updateSecretRequestArgumentCaptor =
//                ArgumentCaptor.forClass(UpdateSecretRequest.class);
//
//
// verify(mockSecretsManagerClient).updateSecret(updateSecretRequestArgumentCaptor.capture());
//        UpdateSecretRequest updateSecretRequest = updateSecretRequestArgumentCaptor.getValue();
//        assertNotNull(updateSecretRequest);
//        assertEquals("existing_api_key", updateSecretRequest.secretString());
//        assertEquals("/stackName/DVLA/apiKey", updateSecretRequest.secretId());
//    }
//
//    @Test
//    void whenUpdateApiKeyFailsToUpdateSecretThenExceptionIsThrown() {
//        when(mockDvlaConfiguration.isApiKeyRotationEnabled()).thenReturn(true);
//        when(mockSecretsManagerClient.updateSecret(any(UpdateSecretRequest.class)))
//                .thenThrow(
//                        SecretsManagerException.builder()
//                                .awsErrorDetails(
//                                        AwsErrorDetails.builder()
//                                                .errorMessage("error message")
//                                                .build())
//                                .message("Exception creating request for Secrets Manager")
//                                .build());
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("new_api_key").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception updating API Key");
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenSecretManagerStepIsNullThenReturnNull() {
//        when(mockInput.getStep()).thenReturn(null);
//        String response = apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//        assertNotNull(response);
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//        verifyNoMoreInteractions(mockChangeApiKeyService);
//        verifyNoMoreInteractions(mockTokenRequestService);
//    }
//
//    @Test
//    void whenSecretManagerStepIsInvalidThenReturnNull() {
//        when(mockInput.getStep()).thenReturn("abcdefgh");
//        String response = apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//        assertNotNull(response);
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//        verifyNoMoreInteractions(mockChangeApiKeyService);
//        verifyNoMoreInteractions(mockTokenRequestService);
//    }
// }
