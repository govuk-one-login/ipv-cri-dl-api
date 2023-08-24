package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.jose.JOSEException;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;

public interface ThirdPartyAPIService {
    String getServiceName();

    DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitForm)
            throws InterruptedException, OAuthHttpResponseExceptionWithErrorBody,
                    CertificateException, ParseException, JOSEException, IOException,
                    NoSuchAlgorithmException;
}
