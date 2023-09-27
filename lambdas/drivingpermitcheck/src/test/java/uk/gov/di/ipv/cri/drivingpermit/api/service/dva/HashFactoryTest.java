package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.response.DvaResponse;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashFactoryTest {
    private RequestHashValidator.HashFactory hashFactory;

    @Mock
    private RequestHashValidator.HashFactory.Sha256MessageDigestFactory
            mockSha256MessageDigestFactory;

    @Test
    void hashValidationSuccess() throws NoSuchAlgorithmException {
        DvaPayload dvaPayload = createSuccessDvaPayload();
        DvaResponse dvaResponse = createSuccessDvaResponse();
        hashFactory = new RequestHashValidator.HashFactory();

        assertDoesNotThrow(() -> hashFactory.getHash(createSuccessDvaPayload(), false));
        assertEquals(hashFactory.getHash(dvaPayload, false), dvaResponse.getRequestHash());
    }

    @Test
    void hashValidationAlgorithmException() throws NoSuchAlgorithmException {
        hashFactory = new RequestHashValidator.HashFactory(mockSha256MessageDigestFactory);
        when(mockSha256MessageDigestFactory.getInstance())
                .thenThrow(new NoSuchAlgorithmException());
        assertThrows(
                NoSuchAlgorithmException.class,
                () -> hashFactory.getHash(createSuccessDvaPayload(), false));
    }

    private DvaPayload createSuccessDvaPayload() {
        DvaPayload dvaPayload = new DvaPayload();
        dvaPayload.setRequestId(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")); // dummy UUID
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

    private DvaResponse createSuccessDvaResponse() {
        DvaResponse dvaResponse = new DvaResponse();
        dvaResponse.setRequestHash(
                "98882b9f7f4173eb355f00a7510132b40aec702768a645c195678294bc16768d");
        dvaResponse.setValidDocument(true);
        return dvaResponse;
    }
}
