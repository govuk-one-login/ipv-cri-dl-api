package uk.gov.di.ipv.cri.drivingpermit.api.service;

import uk.gov.di.ipv.cri.drivingpermit.api.util.HashFactory;

public class RequestHashValidator {
    private final HashFactory hashFactory;

    public RequestHashValidator() {
        this(new HashFactory());
    }

    public RequestHashValidator(HashFactory hashFactory) {
        this.hashFactory = hashFactory;
    }

    public boolean valid(String request, String hash) {
        return hashFactory.getHash(request).equals(hash);
    }
}
