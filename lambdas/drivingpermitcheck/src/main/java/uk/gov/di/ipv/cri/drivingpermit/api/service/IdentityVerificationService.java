package uk.gov.di.ipv.cri.drivingpermit.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.util.List;
import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.DOCUMENT_DATA_VERIFICATION_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.FORM_DATA_VALIDATION_PASS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.ISSUING_AUTHORITY_PREFIX;

public class IdentityVerificationService {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String OPENID_CHECK_METHOD_IDENTIFIER = "data";
    private static final String IDENTITY_CHECK_POLICY = "published";

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
    private final EventProbe eventProbe;

    public IdentityVerificationService(FormDataValidator formDataValidator, EventProbe eventProbe) {
        this.formDataValidator = formDataValidator;
        this.eventProbe = eventProbe;
    }

    public DocumentCheckVerificationResult verifyIdentity(
            DrivingPermitForm drivingPermitData, ThirdPartyAPIService thirdPartyAPIService)
            throws OAuthErrorResponseException {
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
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FORM_DATA_FAILED_VALIDATION);
            }
            LOGGER.info("Form data validated");
            eventProbe.counterMetric(FORM_DATA_VALIDATION_PASS);

            IssuingAuthority issuingAuthority =
                    IssuingAuthority.valueOf(drivingPermitData.getLicenceIssuer());
            LOGGER.info("Document Issuer {}", issuingAuthority);
            eventProbe.counterMetric(
                    ISSUING_AUTHORITY_PREFIX + issuingAuthority.toString().toLowerCase());

            DocumentCheckResult documentCheckResult =
                    thirdPartyAPIService.performDocumentCheck(drivingPermitData);

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

                    // Verification Request Completed
                    eventProbe.counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_SUCCEEDED);

                    LOGGER.info(
                            "Third party transaction id {}",
                            documentCheckResult.getTransactionId());

                    result.setContraIndicators(cis);

                    result.setStrengthScore(documentStrengthScore);
                    result.setValidityScore(documentValidityScore);
                    result.setActivityHistoryScore(activityHistoryScore);

                    // Data capture for VC
                    documentCheckResult.setCheckDetails(
                            getCheckDetails(drivingPermitData, documentCheckResult));

                    result.setCheckDetails(documentCheckResult.getCheckDetails());

                    result.setTransactionId(documentCheckResult.getTransactionId());
                    result.setVerified(documentCheckResult.isValid());
                } else {
                    LOGGER.warn("Driving licence check failed");
                    eventProbe.counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED);

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
            eventProbe.counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED);

            result.setError(ERROR_MSG_CONTEXT);
            result.setExecutedSuccessfully(false);
        } catch (OAuthErrorResponseException e) {
            eventProbe.counterMetric(DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED);
            // Specific exception for all non-recoverable ThirdPartyAPI related errors
            throw e;
        }

        return result;
    }

    private CheckDetails getCheckDetails(
            DrivingPermitForm drivingPermitData, DocumentCheckResult documentCheckResult) {
        CheckDetails checkDetails = new CheckDetails();
        checkDetails.setCheckMethod(OPENID_CHECK_METHOD_IDENTIFIER);
        checkDetails.setIdentityCheckPolicy(IDENTITY_CHECK_POLICY);

        if (documentCheckResult.isValid()) {
            // Map ActivityFrom to documentIssueDate (IssueDate / DateOfIssue)
            // Note: DateOfIssue/issueDate is mapped to the same field - issueDate
            checkDetails.setActivityFrom(drivingPermitData.getIssueDate().toString());
        }
        return checkDetails;
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
