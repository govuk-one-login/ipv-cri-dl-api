package uk.gov.di.ipv.cri.drivingpermit.api.service;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.util.HashFactory;

import java.security.NoSuchAlgorithmException;

public class RequestHashValidator {
    private final HashFactory hashFactory;

    public RequestHashValidator() {
        this(new HashFactory());
    }

    public RequestHashValidator(HashFactory hashFactory) {
        this.hashFactory = hashFactory;
    }

    public boolean valid(DvaPayload request, String hash, boolean isNonProd) throws NoSuchAlgorithmException {
        return hashFactory.getHash(request, isNonProd).equals(hash);
    }
}
