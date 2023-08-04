package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.ParseException;

public class DvlaThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final String SERVICE_NAME = DvlaThirdPartyDocumentGateway.class.getSimpleName();

    public DvlaThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DvlaEndpointFactory endpointFactory,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {}

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException, IOException {
        return null;
    }
}
