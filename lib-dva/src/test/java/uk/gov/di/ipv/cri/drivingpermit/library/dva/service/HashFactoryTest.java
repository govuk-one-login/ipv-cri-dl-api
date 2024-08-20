package uk.gov.di.ipv.cri.drivingpermit.library.dva.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.domain.response.DvaResponse;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashFactoryTest {
    private RequestHashValidator.HashFactory hashFactory;

    @Mock
    private RequestHashValidator.HashFactory.Sha256MessageDigestFactory
            mockSha256MessageDigestFactory;

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void hashValidationSuccess(boolean imposterStub) throws NoSuchAlgorithmException {
        DvaPayload dvaPayload = createSuccessDvaPayload(imposterStub);
        DvaResponse dvaResponse = createSuccessDvaResponse(imposterStub);
        hashFactory = new RequestHashValidator.HashFactory();

        Assertions.assertDoesNotThrow(
                () -> hashFactory.getHash(createSuccessDvaPayload(imposterStub), imposterStub));
        assertEquals(hashFactory.getHash(dvaPayload, imposterStub), dvaResponse.getRequestHash());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void hashValidationAlgorithmException(boolean imposterStub) throws NoSuchAlgorithmException {
        hashFactory = new RequestHashValidator.HashFactory(mockSha256MessageDigestFactory);
        when(mockSha256MessageDigestFactory.getInstance())
                .thenThrow(new NoSuchAlgorithmException());
        assertThrows(
                NoSuchAlgorithmException.class,
                () -> hashFactory.getHash(createSuccessDvaPayload(imposterStub), imposterStub));
    }

    private DvaPayload createSuccessDvaPayload(boolean imposterStub) {
        DvaPayload dvaPayload = new DvaPayload();
        if (!imposterStub) {
            dvaPayload.setRequestId(
                    UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")); // dummy UUID
        }
        dvaPayload.setForenames(Arrays.asList("KENNETH"));
        dvaPayload.setSurname("DECERQUEIRA");
        dvaPayload.setDriverLicenceNumber("12345678");
        dvaPayload.setDateOfBirth(LocalDate.of(1965, 7, 8));
        dvaPayload.setPostcode("BA2 5AA");
        dvaPayload.setDateOfIssue(LocalDate.of(2018, 4, 19));
        dvaPayload.setExpiryDate(LocalDate.of(2042, 10, 1));
        dvaPayload.setIssuerId("DVA");
        return dvaPayload;
    }

    private DvaResponse createSuccessDvaResponse(boolean imposterStub) {
        DvaResponse dvaResponse = new DvaResponse();

        String requestHash =
                imposterStub
                        ? "a06d209caf647292dd8a3b7ef174485633899a470b7164707ec4aa0235072758"
                        : "98882b9f7f4173eb355f00a7510132b40aec702768a645c195678294bc16768d";

        dvaResponse.setRequestHash(requestHash);
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }
}
