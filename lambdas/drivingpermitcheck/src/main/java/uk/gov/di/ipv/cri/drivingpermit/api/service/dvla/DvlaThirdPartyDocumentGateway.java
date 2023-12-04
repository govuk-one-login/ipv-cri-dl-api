package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result.DriverMatchServiceResult;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DVLA;

public class DvlaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvlaThirdPartyDocumentGateway.class.getSimpleName();
    private static final APIResultSource API_RESULT_SOURCE = DVLA;

    private static final Logger LOGGER = LogManager.getLogger();

    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;

    public DvlaThirdPartyDocumentGateway(DvlaEndpointFactory endpointFactory) {
        tokenRequestService = endpointFactory.getTokenRequestService();
        driverMatchService = endpointFactory.getDriverMatchService();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws OAuthErrorResponseException {

        String tokenValue = tokenRequestService.requestToken(false);

        LOGGER.info("Token value {}", tokenValue);

        DriverMatchServiceResult driverMatchServiceResult =
                driverMatchService.performMatch(drivingPermitForm, tokenValue);

        return mapDriverMatchServiceResultToDocumentCheckResult(driverMatchServiceResult);
    }

    private static DocumentCheckResult mapDriverMatchServiceResultToDocumentCheckResult(
            DriverMatchServiceResult driverMatchServiceResult) {

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
