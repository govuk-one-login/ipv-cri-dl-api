package uk.gov.di.ipv.cri.drivingpermit.event.exceptions;

public class SecretNotFoundException extends RuntimeException {
    public SecretNotFoundException(String message) {
        super(message);
    }
}
