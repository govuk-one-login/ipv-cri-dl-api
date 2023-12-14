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
// import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
// import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
// import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
// import uk.gov.di.ipv.cri.common.library.util.EventProbe;
// import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.endpoints.ChangeApiKeyService;
// import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.handler.ApiKeyRenewalHandler;
// import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.util.SecretsManagerRotationStep;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.TokenResponse;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
// import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
// import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
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
//     private ApiKeyRenewalHandler apiKeyRenewalHandler;
//
//    @BeforeEach
//    public void setup() {
//
//        String mockApiKey = "DVLA/apiKey";
//        this.apiKeyRenewalHandler =
//                new ApiKeyRenewalHandler(
//                        mockApiKey,
//                        mockSecretsManagerClient,
//                        mockChangeApiKeyService,
//                        mockTokenRequestService,
//                        mockEventProbe,
//                        mockDriverMatchService);
//    }
//
//    @Test
//    void whenCreateSecretStepIsCalledThenNewApiKeyGeneratedAndSaved() {
//
//        when(mockSecretsManagerClient.putSecretValue(any(PutSecretValueRequest.class)))
//                .thenReturn(null);
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.CREATE_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
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
//        assertEquals("1234567", putSecretValueRequest.secretId());
//        assertEquals("AWSPENDING", putSecretValueRequest.versionStages().get(0));
//    }
//
//    @Test
//    void whenTestSecretStepCalledThenPerformTokenRequestIsVerified()
//            throws OAuthErrorResponseException {
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.TEST_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//        when(mockTokenRequestService.performNewTokenRequest())
//                .thenReturn(new TokenResponse("qwertyuiop"));
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//        verify(mockTokenRequestService).performNewTokenRequest();
//    }
//
//    @Test
//    void whenFinishSecretStepCalledThenSecretUpdatedSuccessfully() {
//
//        when(mockSecretsManagerClient.updateSecret(any(UpdateSecretRequest.class)))
//                .thenReturn(null);
//
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
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
//        assertEquals("1234567", updateSecretValueRequest.secretId());
//    }
//
//    @Test
//    void whenPasswordRenewalHandlerIsCalledAndNoSecretIdIsPresentThenThrowRuntimeException() {
//
//        when(mockSecretsManagerClient.putSecretValue(any(PutSecretValueRequest.class)))
//                .thenThrow(
//                        SecretsManagerException.builder()
//                                .message("Exception putting in secret")
//                                .build());
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString(null).build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.CREATE_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn(null);
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
//    void whenDVLAIsCalledAndPasswordRenewalRequestFailsThenThrowRuntimeException()
//            throws OAuthErrorResponseException {
//
//        doThrow(
//                        new OAuthErrorResponseException(
//                                500, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
//                .when(mockTokenRequestService)
//                .performNewTokenRequest();
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("ASDFGHJ").build();
//
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.TEST_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception performing password renewal request");
//
//        verify(mockTokenRequestService).performNewTokenRequest();
//
//        verifyNoMoreInteractions(mockChangeApiKeyService, mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenPasswordRenewalHandlerIsCalledAndDVLAFailsToUpdatePasswordThenThrowRuntimeException()
//            throws OAuthErrorResponseException, JsonProcessingException {
//
//        doThrow(
//                        new OAuthErrorResponseException(
//                                500, ERROR_INVOKING_THIRD_PARTY_API_KEY_ENDPOINT))
//                .when(mockChangeApiKeyService)
//                .sendApiKeyChangeRequest("ASDFGHJ", "aaaaaaa");
//        GetSecretValueResponse secretValueResponse =
//                GetSecretValueResponse.builder().secretString("ASDFGHJ").build();
//
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.SET_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//
//        assertThrows(
//                RuntimeException.class,
//                () -> apiKeyRenewalHandler.handleRequest(mockInput, mockContext),
//                "Exception updating password");
//
//        verify(mockChangeApiKeyService).sendApiKeyChangeRequest("ASDFGHJ", "aaaaaaa");
//
//        verifyNoMoreInteractions(mockChangeApiKeyService, mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenGetValueIsCalledAndFailsToRetrieveSecretThenRotationContinuesWithoutUpdatingSecret()
// {
//
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenThrow(ResourceNotFoundException.class);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        verify(mockSecretsManagerClient).updateSecret(any(UpdateSecretRequest.class));
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenUpdatePasswordFailsToUpdateSecretThenRotationContinuesWithoutUpdatingSecret() {
//
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
//                GetSecretValueResponse.builder().secretString("abcdefghijk").build();
//        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
//                .thenReturn(secretValueResponse);
//        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
//        when(mockInput.getSecretId()).thenReturn("1234567");
//
//        apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//    }
//
//    @Test
//    void whenSecretManagerStepIsNullThenReturnNull() {
//        when(mockInput.getStep()).thenReturn(null);
//        Void response = apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//        assertNull(response);
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//        verifyNoMoreInteractions(mockChangeApiKeyService);
//        verifyNoMoreInteractions(mockTokenRequestService);
//    }
//
//    @Test
//    void whenSecretManagerStepIsInvalidThenReturnNull() {
//        when(mockInput.getStep()).thenReturn("asdfghjk");
//        Void response = apiKeyRenewalHandler.handleRequest(mockInput, mockContext);
//        assertNull(response);
//
//        verifyNoMoreInteractions(mockSecretsManagerClient);
//        verifyNoMoreInteractions(mockChangeApiKeyService);
//        verifyNoMoreInteractions(mockTokenRequestService);
//    }
// }
