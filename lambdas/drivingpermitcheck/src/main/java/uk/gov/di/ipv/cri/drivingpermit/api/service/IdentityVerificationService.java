package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;

import java.util.List;
import java.util.Objects;

public class IdentityVerificationService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERROR_MSG_CONTEXT =
            "Error occurred when attempting to invoke the third party api";
    private static final String ERROR_DRIVING_PERMIT_CHECK_RESULT_RETURN_NULL =
            "Null DrivingPermitCheckResult returned when invoking third party API.";
    private static final String ERROR_DRIVING_PERMIT_CHECK_RESULT_NO_ERR_MSG =
            "DrivingPermitCheckResult had no error message.";
    private static final int MAX_DRIVING_PERMIT_GPG45_STRENGTH_VALUE = 4;
    private static final int MAX_DRIVING_PERMIT_GPG45_VALIDITY_VALUE = 2;
    private static final int MIN_DRIVING_PERMIT_GPG45_VALUE = 0;

    private final PersonIdentityValidator personIdentityValidator;
    private final ThirdPartyDocumentGateway thirdPartyGateway;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    IdentityVerificationService(
            ThirdPartyDocumentGateway thirdPartyGateway,
            PersonIdentityValidator personIdentityValidator,
            ContraindicationMapper contraindicationMapper,
            AuditService auditService,
            ConfigurationService configurationService,
            ObjectMapper objectMapper) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.personIdentityValidator = personIdentityValidator;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public DocumentCheckVerificationResult verifyIdentity(DrivingPermitForm drivingPermitData)
            throws OAuthHttpResponseExceptionWithErrorBody {
        DocumentCheckVerificationResult result = new DocumentCheckVerificationResult();

        try {
            LOGGER.info("Validating identity...");
            ValidationResult<List<String>> validationResult =
                    this.personIdentityValidator.validate(drivingPermitData);
            if (!validationResult.isValid()) {
                result.setSuccess(false);
                result.setValidationErrors(validationResult.getError());
                result.setError("IdentityValidationError");
                return result;
            }
            LOGGER.info("Identity info validated");
            DocumentCheckResult documentCheckResult =
                    thirdPartyGateway.performDocumentCheck(drivingPermitData);

            LOGGER.info("Third party response mapped");
            LOGGER.info(
                    "Third party response {}",
                    new ObjectMapper().writeValueAsString(documentCheckResult));
            if (Objects.nonNull(documentCheckResult)) {
                result.setSuccess(documentCheckResult.isExecutedSuccessfully());
                if (result.isSuccess()) {
                    LOGGER.info("Mapping contra indicators from document check response");

                    int documentStrengthScore = MAX_DRIVING_PERMIT_GPG45_STRENGTH_VALUE;
                    int documentValidityScore = calculateValidity(documentCheckResult);
                    List<String> cis = calculateContraIndicators(documentCheckResult);

                    LOGGER.info(
                            "Document check passed successfully. Indicators {}, Strength Score {}, Validity Score {}",
                            String.join(", ", cis),
                            documentStrengthScore,
                            documentValidityScore);

                    LOGGER.info(
                            "Third party transaction id {}",
                            documentCheckResult.getTransactionId());

                    result.setContraIndicators(cis.toArray(new String[0]));
                    result.setStrengthScore(documentStrengthScore);
                    result.setTransactionId(documentCheckResult.getTransactionId());
                    result.setSuccess(documentCheckResult.isExecutedSuccessfully());

                } else {
                    LOGGER.warn("Fraud check failed");
                    if (Objects.nonNull(documentCheckResult.getErrorMessage())) {
                        result.setError(documentCheckResult.getErrorMessage());
                    } else {
                        result.setError(ERROR_DRIVING_PERMIT_CHECK_RESULT_NO_ERR_MSG);
                        LOGGER.warn(ERROR_DRIVING_PERMIT_CHECK_RESULT_NO_ERR_MSG);
                    }
                }
                return result;
            }
            LOGGER.error(ERROR_DRIVING_PERMIT_CHECK_RESULT_RETURN_NULL);
            result.setError(ERROR_MSG_CONTEXT);
            result.setSuccess(false);
        } catch (InterruptedException ie) {
            LOGGER.error(ERROR_MSG_CONTEXT, ie);
            Thread.currentThread().interrupt();
            result.setError(ERROR_MSG_CONTEXT + ": " + ie.getMessage());
            result.setSuccess(false);
        } catch (Exception e) {
            LOGGER.error(ERROR_MSG_CONTEXT, e);
            result.setError(ERROR_MSG_CONTEXT + ": " + e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }

    private int calculateValidity(DocumentCheckResult documentCheckResult) {
        return documentCheckResult.isValid()
                ? MAX_DRIVING_PERMIT_GPG45_VALIDITY_VALUE
                : MIN_DRIVING_PERMIT_GPG45_VALUE;
    }

    private List<String> calculateContraIndicators(DocumentCheckResult documentCheckResult) {
        return documentCheckResult.isValid() ? null : List.of("DO2");
    }
}
