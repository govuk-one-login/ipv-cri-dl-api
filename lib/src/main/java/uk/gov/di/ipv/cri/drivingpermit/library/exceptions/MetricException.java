package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import java.io.Serial;

public class MetricException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1947203662730263951L;

    public MetricException(String message) {
        super(message);
    }
}
