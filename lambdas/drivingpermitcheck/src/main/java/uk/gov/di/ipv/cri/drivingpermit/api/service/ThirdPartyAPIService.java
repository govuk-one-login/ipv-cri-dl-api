package uk.gov.di.ipv.cri.drivingpermit.api.service;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;

public interface ThirdPartyAPIService {
    String getServiceName();

    // Error - Do not add additional exceptions to this class
    DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws OAuthErrorResponseException;
}
