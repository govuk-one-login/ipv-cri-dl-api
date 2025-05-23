package uk.gov.di.ipv.cri.drivingpermit.api.exception;

import java.io.Serial;

public class SessionItemNotFoundException extends Exception {
    @Serial private static final long serialVersionUID = -2610722176794913136L;

    public SessionItemNotFoundException(String message) {
        super(message);
    }
}
