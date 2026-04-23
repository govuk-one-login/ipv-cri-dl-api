package uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception;

import uk.gov.account.ipv.cri.lime.limeade.annotation.ExcludeClassFromGeneratedCoverageReport;

import java.io.Serial;

@ExcludeClassFromGeneratedCoverageReport
public class DVLATokenExpiryWindowException extends RuntimeException {
    @Serial private static final long serialVersionUID = 9216009245190896627L;

    public DVLATokenExpiryWindowException(String message) {
        super(message);
    }
}
