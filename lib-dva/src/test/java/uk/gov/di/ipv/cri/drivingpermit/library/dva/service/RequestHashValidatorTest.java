package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestHashValidatorTest {

    @Test
    void shouldCreateRequestHashValidator() {
        assertDoesNotThrow(() -> new RequestHashValidator());
    }

    @Test
    void requestHashValidatorReturnsFalseOnHashValidationFail() throws NoSuchAlgorithmException {
        RequestHashValidator.HashFactory hashFactory = new RequestHashValidator.HashFactory();
        RequestHashValidator requestHashValidator = new RequestHashValidator(hashFactory);

        DvaPayload dvaPayload = new DvaPayload();
        dvaPayload.setSurname("Sur");
        dvaPayload.setForenames(Arrays.asList("Fore"));

        DvaResponse dvaResponse = new DvaResponse();
        dvaResponse.setRequestHash(hashFactory.getHash(dvaPayload, false) + "0");

        boolean isValidHash =
                requestHashValidator.valid(dvaPayload, dvaResponse.getRequestHash(), false);

        // Request Hash  = ad8[...]a5f
        // Response Hash = ad8[...]a5f0
        assertFalse(isValidHash);
    }
}
