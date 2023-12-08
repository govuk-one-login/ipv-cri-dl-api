package uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception;

public class DVLAMatchUnauthorizedException extends RuntimeException {
    public DVLAMatchUnauthorizedException(String message) {
        super(message);
    }

    // Lowers over head of the exception by suppressing creation of the stack trace
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
