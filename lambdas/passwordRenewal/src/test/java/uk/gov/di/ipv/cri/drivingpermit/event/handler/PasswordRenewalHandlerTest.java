package uk.gov.di.ipv.cri.drivingpermit.event.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangePasswordService;
import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.TokenResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.UnauthorisedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class PasswordRenewalHandlerTest {

    @Mock private SecretsManagerClient mockSecretsManagerClient;

    @Mock private ChangePasswordService mockChangePasswordService;

    @Mock private TokenRequestService mockTokenRequestService;

    @Mock private EventProbe mockEventProbe;

    @Mock private SecretsManagerRotationEvent mockInput;

    @Mock private Context mockContext;
    @Mock private DvlaConfiguration dvlaConfiguration;

    private PasswordRenewalHandler passwordRenewalHandler;

    @BeforeEach
    public void setup() {

        this.passwordRenewalHandler =
                new PasswordRenewalHandler(
                        mockSecretsManagerClient,
                        mockChangePasswordService,
                        mockTokenRequestService,
                        mockEventProbe,
                        dvlaConfiguration);
    }

    @Test
    void whenCreateSecretStepIsCalledThenNewPasswordGeneratedAndSaved() {

        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);
        when(mockSecretsManagerClient.putSecretValue(any(PutSecretValueRequest.class)))
                .thenReturn(null);

        doThrow(new SecretNotFoundException("Secret not set"))
                .when(mockSecretsManagerClient)
                .getSecretValue(any(GetSecretValueRequest.class));
        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        passwordRenewalHandler.handleRequest(mockInput, mockContext);

        ArgumentCaptor<PutSecretValueRequest> putSecretValueCaptor =
                ArgumentCaptor.forClass(PutSecretValueRequest.class);

        verify(mockSecretsManagerClient).putSecretValue(putSecretValueCaptor.capture());
        PutSecretValueRequest putSecretValueRequest = putSecretValueCaptor.getValue();
        assertNotNull(putSecretValueRequest);
        assertNotNull(putSecretValueRequest.secretString());
        assertEquals("1234567", putSecretValueRequest.secretId());
        assertEquals("AWSPENDING", putSecretValueRequest.versionStages().get(0));

        String password = putSecretValueRequest.secretString();
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
    }

    @Test
    void whenCreateSecretStepIsCalledAndAPasswordIsStillPendingThenItIsTestedAndSaved()
            throws OAuthErrorResponseException, UnauthorisedException {

        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);

        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);
        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        passwordRenewalHandler.handleRequest(mockInput, mockContext);

        verify(mockChangePasswordService).sendPasswordChangeRequest("asdfghjkl");
        verify(mockTokenRequestService).performNewTokenRequest("asdfghjkl");
        ArgumentCaptor<UpdateSecretRequest> updateSecretRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdateSecretRequest.class);

        verify(mockSecretsManagerClient).updateSecret(updateSecretRequestArgumentCaptor.capture());
        UpdateSecretRequest updateSecretValueRequest = updateSecretRequestArgumentCaptor.getValue();
        assertNotNull(updateSecretValueRequest);
        assertNotNull(updateSecretValueRequest.secretString());
        assertEquals("1234567", updateSecretValueRequest.secretId());
    }

    @Test
    void whenSetSecretStepCalledThenPasswordIsSentToDVLA()
            throws OAuthErrorResponseException, UnauthorisedException {

        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");
        String newPassword = "asdfghjkl";
        doNothing().when(mockChangePasswordService).sendPasswordChangeRequest((newPassword));

        passwordRenewalHandler.handleRequest(mockInput, mockContext);
        verify(mockChangePasswordService).sendPasswordChangeRequest("asdfghjkl");
    }

    @Test
    void whenTestSecretStepCalledThenPerformTokenRequestIsVerified()
            throws OAuthErrorResponseException {
        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);

        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");
        String newPassword = "asdfghjkl";
        when(mockTokenRequestService.performNewTokenRequest(newPassword))
                .thenReturn(new TokenResponse("qwertyuiop"));

        passwordRenewalHandler.handleRequest(mockInput, mockContext);
        verify(mockTokenRequestService).performNewTokenRequest("asdfghjkl");
    }

    @Test
    void whenFinishSecretStepCalledThenSecretUpdatedSuccessfully() {

        when(mockSecretsManagerClient.updateSecret(any(UpdateSecretRequest.class)))
                .thenReturn(null);

        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("asdfghjkl").build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        passwordRenewalHandler.handleRequest(mockInput, mockContext);

        ArgumentCaptor<UpdateSecretRequest> updateSecretRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdateSecretRequest.class);

        verify(mockSecretsManagerClient).updateSecret(updateSecretRequestArgumentCaptor.capture());
        UpdateSecretRequest updateSecretValueRequest = updateSecretRequestArgumentCaptor.getValue();
        assertNotNull(updateSecretValueRequest);
        assertNotNull(updateSecretValueRequest.secretString());
        assertEquals("1234567", updateSecretValueRequest.secretId());
    }

    @Test
    void whenPasswordRenewalHandlerIsCalledAndNoSecretIdIsPresentThenThrowRuntimeException() {

        when(mockSecretsManagerClient.putSecretValue(any(PutSecretValueRequest.class)))
                .thenThrow(
                        SecretsManagerException.builder()
                                .message("Exception putting in secret")
                                .build());
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString(null).build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);
        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn(null);

        assertThrows(
                RuntimeException.class,
                () -> passwordRenewalHandler.handleRequest(mockInput, mockContext),
                "Exception putting in secret");

        ArgumentCaptor<PutSecretValueRequest> putSecretValueCaptor =
                ArgumentCaptor.forClass(PutSecretValueRequest.class);

        verify(mockSecretsManagerClient).putSecretValue(putSecretValueCaptor.capture());
        PutSecretValueRequest putSecretValueRequest = putSecretValueCaptor.getValue();
        assertNotNull(putSecretValueRequest);
        assertNotNull(putSecretValueRequest.secretString());
        assertNull(null, putSecretValueRequest.secretId());
        assertEquals("AWSPENDING", putSecretValueRequest.versionStages().get(0));
    }

    @Test
    void whenDVLAIsCalledAndPasswordRenewalRequestFailsThenThrowRuntimeException()
            throws OAuthErrorResponseException, UnauthorisedException {
        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);

        doNothing().when(mockChangePasswordService).sendPasswordChangeRequest((anyString()));
        doThrow(
                        new OAuthErrorResponseException(
                                500, ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT))
                .when(mockTokenRequestService)
                .performNewTokenRequest("ASDFGHJ");
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("ASDFGHJ").build();

        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        assertThrows(
                RuntimeException.class,
                () -> passwordRenewalHandler.handleRequest(mockInput, mockContext),
                "Exception performing password renewal request");

        verify(mockTokenRequestService).performNewTokenRequest("ASDFGHJ");

        verifyNoMoreInteractions(mockChangePasswordService, mockSecretsManagerClient);
    }

    @Test
    void whenPasswordRenewalHandlerIsCalledAndDVLAFailsToUpdatePasswordThenThrowRuntimeException()
            throws OAuthErrorResponseException, UnauthorisedException {

        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);
        doThrow(
                        new OAuthErrorResponseException(
                                500, ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT))
                .when(mockChangePasswordService)
                .sendPasswordChangeRequest(anyString());
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("ASDFGHJ").build();

        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        assertThrows(
                RuntimeException.class,
                () -> passwordRenewalHandler.handleRequest(mockInput, mockContext),
                "Exception updating password");

        verify(mockChangePasswordService)
                .sendPasswordChangeRequest(secretValueResponse.secretString());

        verifyNoMoreInteractions(mockChangePasswordService, mockSecretsManagerClient);
    }

    @Test
    void
            whenPasswordRenewalHandlerIsCalledAndDVLAFailsWithUnathorisedErrorThenContinueTheTestingStep()
                    throws OAuthErrorResponseException, UnauthorisedException {

        when(dvlaConfiguration.isPasswordRotationEnabled()).thenReturn(true);
        doThrow(
                        new UnauthorisedException(
                                500, ERROR_INVOKING_THIRD_PARTY_API_CHANGE_PASSWORD_ENDPOINT))
                .when(mockChangePasswordService)
                .sendPasswordChangeRequest(anyString());
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("ASDFGHJ").build();

        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);

        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        passwordRenewalHandler.handleRequest(mockInput, mockContext);

        verify(mockChangePasswordService)
                .sendPasswordChangeRequest(secretValueResponse.secretString());
        verify(mockTokenRequestService).performNewTokenRequest("ASDFGHJ");
        ArgumentCaptor<UpdateSecretRequest> updateSecretRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdateSecretRequest.class);

        verify(mockSecretsManagerClient).updateSecret(updateSecretRequestArgumentCaptor.capture());
        UpdateSecretRequest updateSecretValueRequest = updateSecretRequestArgumentCaptor.getValue();
        assertNotNull(updateSecretValueRequest);
        assertNotNull(updateSecretValueRequest.secretString());
        assertEquals("1234567", updateSecretValueRequest.secretId());
        verifyNoMoreInteractions(mockChangePasswordService, mockSecretsManagerClient);
    }

    @Test
    void whenUpdatePasswordFailsToUpdateSecretThenExceptionIsThrown() {

        when(mockSecretsManagerClient.updateSecret(any(UpdateSecretRequest.class)))
                .thenThrow(
                        SecretsManagerException.builder()
                                .awsErrorDetails(
                                        AwsErrorDetails.builder()
                                                .errorMessage("error message")
                                                .build())
                                .message("Exception creating request for Secrets Manager")
                                .build());
        GetSecretValueResponse secretValueResponse =
                GetSecretValueResponse.builder().secretString("abcdefghijk").build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(secretValueResponse);
        when(mockInput.getStep()).thenReturn(SecretsManagerRotationStep.FINISH_SECRET.toString());
        when(mockInput.getSecretId()).thenReturn("1234567");

        assertThrows(
                RuntimeException.class,
                () -> passwordRenewalHandler.handleRequest(mockInput, mockContext),
                "Exception updating password");

        verifyNoMoreInteractions(mockSecretsManagerClient);
    }

    @Test
    void whenSecretManagerStepIsNullThenReturnNull() {
        when(mockInput.getStep()).thenReturn(null);
        String response = passwordRenewalHandler.handleRequest(mockInput, mockContext);
        assertNotNull(response);

        verifyNoMoreInteractions(mockSecretsManagerClient);
        verifyNoMoreInteractions(mockChangePasswordService);
        verifyNoMoreInteractions(mockTokenRequestService);
    }

    @Test
    void whenSecretManagerStepIsInvalidThenReturnNull() {
        when(mockInput.getStep()).thenReturn("asdfghjk");
        String response = passwordRenewalHandler.handleRequest(mockInput, mockContext);
        assertNotNull(response);

        verifyNoMoreInteractions(mockSecretsManagerClient);
        verifyNoMoreInteractions(mockChangePasswordService);
        verifyNoMoreInteractions(mockTokenRequestService);
    }
}
