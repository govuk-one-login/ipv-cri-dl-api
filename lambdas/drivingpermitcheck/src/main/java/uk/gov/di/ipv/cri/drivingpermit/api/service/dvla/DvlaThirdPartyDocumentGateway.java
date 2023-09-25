package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

public class DvlaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvlaThirdPartyDocumentGateway.class.getSimpleName();

    private static final Logger LOGGER = LogManager.getLogger();

    private final TokenRequestService tokenRequestService;

    public DvlaThirdPartyDocumentGateway(DvlaEndpointFactory endpointFactory) {

        tokenRequestService = endpointFactory.getTokenRequestService();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws OAuthErrorResponseException {

        String token = tokenRequestService.requestAccessToken(false);

        LOGGER.info("Token value {}", token);

        // TODO Perform Check
        // Check Ack? -> Valid True
        // Check Nack? -> Valid False
        // Check Fail -> Exception

        DocumentCheckResult documentCheckResult = new DocumentCheckResult();

        documentCheckResult.setValid(true);
        documentCheckResult.setExecutedSuccessfully(documentCheckResult.isValid());

        return documentCheckResult;
    }
}
