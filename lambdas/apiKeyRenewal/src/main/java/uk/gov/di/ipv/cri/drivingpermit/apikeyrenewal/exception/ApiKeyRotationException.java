package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.exception;

import java.io.Serial;

public class ApiKeyRotationException extends RuntimeException {
    @Serial private static final long serialVersionUID = 4127395021683340917L;

    public ApiKeyRotationException(Throwable cause) {
        super(cause);
    }
}
