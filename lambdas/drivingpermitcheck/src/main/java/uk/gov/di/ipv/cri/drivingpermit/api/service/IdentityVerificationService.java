package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.gateway.ThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.util.List;
import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DCS_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DCS_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_PASS;

public class IdentityVerificationService {
    private final Logger LOGGER = LogManager.getLogger();

    private static final String DOCUMENT_VALIDITY_CI = "D02";

    private static final int MAX_DRIVING_PERMIT_GPG45_STRENGTH_VALUE = 3;
    private static final int MAX_DRIVING_PERMIT_GPG45_VALIDITY_VALUE = 2;
    private static final int MIN_DRIVING_PERMIT_GPG45_VALUE = 0;
    private static final int MAX_DRIVING_ACTIVITY_HISTORY_SCORE = 1;
    private static final int MIN_DRIVING_ACTIVITY_HISTORY_SCORE = 0;

    private static final String ERROR_MSG_CONTEXT =
            "Error occurred when attempting to invoke the third party api";
    private static final String ERROR_DRIVING_PERMIT_CHECK_RESULT_RETURN_NULL =
            "Null DrivingPermitCheckResult returned when invoking third party API.";
    private static final String ERROR_DRIVING_PERMIT_CHECK_RESULT_NO_ERR_MSG =
            "DrivingPermitCheckResult had no error message.";

    private final FormDataValidator formDataValidator;
    private final ThirdPartyDocumentGateway thirdPartyGateway;

    private final EventProbe eventProbe;

    private final boolean useDcs;

    IdentityVerificationService(
            ThirdPartyDocumentGateway thirdPartyGateway,
            FormDataValidator formDataValidator,
            ContraindicationMapper contraindicationMapper,
            AuditService auditService,
            ConfigurationService configurationService,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.formDataValidator = formDataValidator;
        this.eventProbe = eventProbe;
        this.useDcs = configurationService.getUseLegacy();
    }

    public DocumentCheckVerificationResult verifyIdentity(DrivingPermitForm drivingPermitData)
            throws OAuthHttpResponseExceptionWithErrorBody {
        DocumentCheckVerificationResult result = new DocumentCheckVerificationResult();

        try {
            LOGGER.info("Validating form data...");
            ValidationResult<List<String>> validationResult =
                    this.formDataValidator.validate(drivingPermitData);
            if (!validationResult.isValid()) {
                String errorMessages = String.join(",", validationResult.getError());
                LOGGER.error(
                        "{} - {} ",
                        ErrorResponse.FORM_DATA_FAILED_VALIDATION.getMessage(),
                        errorMessages);
                eventProbe.counterMetric(FORM_DATA_VALIDATION_FAIL);
                throw new OAuthHttpResponseExceptionWithErrorBody(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FORM_DATA_FAILED_VALIDATION);
            }
            LOGGER.info("Form data validated");
            eventProbe.counterMetric(FORM_DATA_VALIDATION_PASS);

            DocumentCheckResult documentCheckResult = new DocumentCheckResult();

            if (useDcs) {
                LOGGER.info("Performing document check (DCS)");
                documentCheckResult = thirdPartyGateway.performDocumentCheck(drivingPermitData);
            } else {
                LOGGER.info("Performing document check (DVA direct)");
                // replace with new method in LIME-685
                documentCheckResult = thirdPartyGateway.performDocumentCheck(drivingPermitData);
            }

            LOGGER.info("Third party response mapped");
            if (Objects.nonNull(documentCheckResult)) {
                result.setExecutedSuccessfully(documentCheckResult.isExecutedSuccessfully());
                if (result.isExecutedSuccessfully()) {
                    LOGGER.info("Mapping contra indicators from Driving licence check response");

                    int documentStrengthScore = MAX_DRIVING_PERMIT_GPG45_STRENGTH_VALUE;
                    int documentValidityScore = calculateValidity(documentCheckResult);
                    int activityHistoryScore = calculateActivityHistory(documentCheckResult);
                    List<String> cis = calculateContraIndicators(documentCheckResult);

                    LOGGER.info(
                            "Driving licence check passed successfully. Indicators {}, Strength Score {}, Validity Score {}, Activity HistoryScore {}",
                            (cis != null) ? String.join(", ", cis) : "[]",
                            documentStrengthScore,
                            documentValidityScore,
                            activityHistoryScore);
                    eventProbe.counterMetric(DCS_CHECK_REQUEST_SUCCEEDED);

                    LOGGER.info(
                            "Third party transaction id {}",
                            documentCheckResult.getTransactionId());

                    result.setContraIndicators(cis);

                    result.setStrengthScore(documentStrengthScore);
                    result.setValidityScore(documentValidityScore);
                    result.setActivityHistoryScore(activityHistoryScore);

                    result.setCheckDetails(documentCheckResult.getCheckDetails());

                    result.setTransactionId(documentCheckResult.getTransactionId());
                    result.setVerified(documentCheckResult.isValid());
                } else {
                    LOGGER.warn("Driving licence check failed");
                    eventProbe.counterMetric(DCS_CHECK_REQUEST_FAILED);

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
            eventProbe.counterMetric(DCS_CHECK_REQUEST_FAILED);

            result.setError(ERROR_MSG_CONTEXT);
            result.setExecutedSuccessfully(false);
        } catch (OAuthHttpResponseExceptionWithErrorBody e) {
            eventProbe.counterMetric(DCS_CHECK_REQUEST_FAILED);
            // Specific exception for non-recoverable DCS related errors
            throw e;
        } catch (InterruptedException ie) {
            LOGGER.error(ERROR_MSG_CONTEXT, ie);
            Thread.currentThread().interrupt();
            result.setError(ERROR_MSG_CONTEXT + ": " + ie.getMessage());
            result.setExecutedSuccessfully(false);
        } catch (Exception e) {
            LOGGER.error(ERROR_MSG_CONTEXT, e);
            result.setError(ERROR_MSG_CONTEXT + ": " + e.getMessage());
            result.setExecutedSuccessfully(false);
        }
        return result;
    }

    private int calculateValidity(DocumentCheckResult documentCheckResult) {
        return documentCheckResult.isValid()
                ? MAX_DRIVING_PERMIT_GPG45_VALIDITY_VALUE
                : MIN_DRIVING_PERMIT_GPG45_VALUE;
    }

    private int calculateActivityHistory(DocumentCheckResult documentCheckResult) {
        return documentCheckResult.isValid()
                ? MAX_DRIVING_ACTIVITY_HISTORY_SCORE
                : MIN_DRIVING_ACTIVITY_HISTORY_SCORE;
    }

    private List<String> calculateContraIndicators(DocumentCheckResult documentCheckResult) {
        return documentCheckResult.isValid() ? null : List.of(DOCUMENT_VALIDITY_CI);
    }
}
