package uk.gov.di.ipv.cri.drivingpermit.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class HashFactory {
    private final Sha256MessageDigestFactory messageDigestFactory;
    private final Logger LOGGER = LogManager.getLogger();

    public HashFactory() {
        this(new Sha256MessageDigestFactory());
    }

    public HashFactory(Sha256MessageDigestFactory messageDigestFactory) {
        this.messageDigestFactory = messageDigestFactory;
    }

    public String getHash(DvaPayload request) throws NoSuchAlgorithmException {
        String message =
                request.getIssuerId()
                        + request.getRequestId()
                        + Objects.toString(request.getSurname(), "")
                        + request.getForenames().stream()
                                .filter(Objects::nonNull)
                                .reduce("", String::concat)
                        + Objects.toString(request.getDateOfBirth(), "")
                        + request.getIssueDate()
                        + request.getExpiryDate()
                        + request.getDriverLicenceNumber()
                        + Objects.toString(request.getPostcode(), "");
        MessageDigest sha256;
        try {
            sha256 = messageDigestFactory.getInstance();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Encountered hash validation exception : {}", e.getMessage());
            throw e;
        }
        byte[] array = sha256.digest(message.getBytes());
        return arrayToString(array);
    }

    private String arrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            // performs a bitwise calculation on `anArray` to extract the lower 8 bits
            // and affirm it as a positive value via AND 0xFF
            // before performing a XOR check with 0x100
            sb.append(Integer.toHexString((anArray & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    public static class Sha256MessageDigestFactory {
        public MessageDigest getInstance() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance("SHA-256");
        }
    }
}
