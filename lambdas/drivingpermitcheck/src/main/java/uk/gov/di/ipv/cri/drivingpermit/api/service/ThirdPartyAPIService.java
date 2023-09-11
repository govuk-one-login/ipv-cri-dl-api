package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.jose.JOSEException;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

import java.text.ParseException;

public interface ThirdPartyAPIService {
    String getServiceName();

    DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws OAuthErrorResponseException, ParseException, JOSEException;
}
