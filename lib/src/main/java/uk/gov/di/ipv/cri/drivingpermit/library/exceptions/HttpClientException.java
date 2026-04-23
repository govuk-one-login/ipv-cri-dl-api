package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import java.io.Serial;

public class HttpClientException extends RuntimeException {
    @Serial private static final long serialVersionUID = -2824404639611212981L;

    public HttpClientException(Throwable cause) {
        super(cause);
    }
}
