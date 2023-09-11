package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

public class DvlaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvlaThirdPartyDocumentGateway.class.getSimpleName();

    private static final Logger LOGGER = LogManager.getLogger();

    // private DvlaConfiguration dvlaConfiguration;

    private final TokenRequestService tokenRequestService;

    public DvlaThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DvlaEndpointFactory endpointFactory,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {

        // Same on all endpoints
        final RequestConfig defaultRequestConfig =
                new HttpRequestConfig().getDefaultRequestConfig();

        DvlaConfiguration dvlaConfiguration = configurationService.getDvlaConfiguration();

        tokenRequestService =
                new TokenRequestService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws OAuthErrorResponseException {

        String token = tokenRequestService.requestAccessToken(true);

        LOGGER.info("Token {}", token);
        // Fetch Token from Dynamo
        // Got Token?
        // > No? Token Request
        // >> Token Request Ok > Continue
        // >> Token Request Fail > Exception
        // > Yes? Continue

        // Do Check
        // Check Ack? -> Valid True
        // Check Nack? -> Valid False
        // Check Fail -> Exception

        DocumentCheckResult documentCheckResult = new DocumentCheckResult();

        documentCheckResult.setValid(true);

        return documentCheckResult;
    }
}
