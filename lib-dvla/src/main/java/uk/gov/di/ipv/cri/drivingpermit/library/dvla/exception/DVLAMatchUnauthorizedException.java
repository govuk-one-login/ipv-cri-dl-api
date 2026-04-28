package uk.gov.di.ipv.cri.drivingpermit.library.dvla.exception;

import java.io.Serial;

public class DVLAMatchUnauthorizedException extends RuntimeException {
    @Serial private static final long serialVersionUID = -3016468273412000959L;

    public DVLAMatchUnauthorizedException(String message) {
        super(message);
    }

    // Lowers over head of the exception by suppressing creation of the stack trace
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
