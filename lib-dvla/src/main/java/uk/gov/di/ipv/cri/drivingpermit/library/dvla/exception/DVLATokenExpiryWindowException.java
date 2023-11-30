package uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception;

public class DVLATokenExpiryWindowException extends RuntimeException {
    public DVLATokenExpiryWindowException(String message) {
        super(message);
    }
}
