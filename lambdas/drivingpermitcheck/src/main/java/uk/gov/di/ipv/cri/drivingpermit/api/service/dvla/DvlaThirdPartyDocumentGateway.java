package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.Strategy;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception.DVLAMatchUnauthorizedException;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DVLA;
import static uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse.ERROR_DVLA_EXPIRED_TOKEN_RECOVERY_FAILED;

public class DvlaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvlaThirdPartyDocumentGateway.class.getSimpleName();
    private static final APIResultSource API_RESULT_SOURCE = DVLA;

    private static final Logger LOGGER = LogManager.getLogger();

    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;

    private static final int MAX_UNAUTHORIZED_RECOVERY_ATTEMPTS = 1;

    public DvlaThirdPartyDocumentGateway(DvlaEndpointFactory endpointFactory) {
        tokenRequestService = endpointFactory.getTokenRequestService();
        driverMatchService = endpointFactory.getDriverMatchService();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(
            DrivingPermitForm drivingPermitForm, Strategy strategy)
            throws OAuthErrorResponseException {

        LOGGER.info("DVLA request started");

        DriverMatchServiceResult driverMatchServiceResult = null;

        // Use any existing token by default
        boolean newTokenOverride = false;

        // Only happy path becomes true
        boolean finished = false;

        // > 0 are token expired recovery iterations
        int iteration = 0;

        do {
            String tokenValue = tokenRequestService.requestToken(newTokenOverride, strategy);

            LOGGER.info("Token value {}", tokenValue);

            try {
                driverMatchServiceResult =
                        driverMatchService.performMatch(drivingPermitForm, tokenValue, strategy);

                finished = true;

            } catch (DVLAMatchUnauthorizedException e) {

                LOGGER.warn("{} - iteration {}", e.getClass().getSimpleName(), iteration);

                if (iteration == MAX_UNAUTHORIZED_RECOVERY_ATTEMPTS) {

                    // If Unauthorized comes back again, there could be an issue with the api key
                    LOGGER.error(ERROR_DVLA_EXPIRED_TOKEN_RECOVERY_FAILED);

                    throw new OAuthErrorResponseException(
                            HttpStatusCode.INTERNAL_SERVER_ERROR,
                            ERROR_DVLA_EXPIRED_TOKEN_RECOVERY_FAILED);
                } else {
                    LOGGER.warn(
                            "Assuming Token expired, attempting recovery via a new token request");

                    // flip flag to force update the token
                    newTokenOverride = true;
                    iteration++;
                }
            }

        } while (!finished);

        LOGGER.info("DVLA request complete");

        return mapDriverMatchServiceResultToDocumentCheckResult(driverMatchServiceResult);
    }

    private static DocumentCheckResult mapDriverMatchServiceResultToDocumentCheckResult(
            DriverMatchServiceResult driverMatchServiceResult) {

        LOGGER.info("Mapping DVLA response");

        // Validity.NOT_FOUND and Validity.INVALID map as false here
        boolean validDocument = (driverMatchServiceResult.getValidity() == Validity.VALID);

        String requestId = driverMatchServiceResult.getRequestId();

        DocumentCheckResult documentCheckResult = new DocumentCheckResult();
        documentCheckResult.setValid(validDocument);
        documentCheckResult.setTransactionId(requestId);

        // All errors in DvlaThirdPartyDocumentGateway are exceptions, so this is always true.
        // Can be removed when the remaining ThirdPartyAPIs no longer use this.
        documentCheckResult.setExecutedSuccessfully(true);
        documentCheckResult.setApiResultSource(API_RESULT_SOURCE);
        return documentCheckResult;
    }
}
