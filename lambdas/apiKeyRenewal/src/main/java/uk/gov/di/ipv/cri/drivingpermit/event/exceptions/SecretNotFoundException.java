package uk.gov.di.ipv.cri.drivingpermit.event.exceptions;

import java.io.Serial;

public class SecretNotFoundException extends RuntimeException {
    @Serial private static final long serialVersionUID = -2931978436360910214L;

    public SecretNotFoundException(String message) {
        super(message);
    }
}
