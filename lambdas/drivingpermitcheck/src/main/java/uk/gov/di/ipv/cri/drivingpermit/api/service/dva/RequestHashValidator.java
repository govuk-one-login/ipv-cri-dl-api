package uk.gov.di.ipv.cri.drivingpermit.api.service.dva;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dva.request.DvaPayload;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class RequestHashValidator {
    private final HashFactory hashFactory;

    public RequestHashValidator() {
        this(new HashFactory());
    }

    public RequestHashValidator(HashFactory hashFactory) {
        this.hashFactory = hashFactory;
    }

    public boolean valid(DvaPayload request, String hash, boolean isImposterStub)
            throws NoSuchAlgorithmException {
        return hashFactory.getHash(request, isImposterStub).equals(hash);
    }

    public static class HashFactory {
        private final Sha256MessageDigestFactory messageDigestFactory;
        private final Logger LOGGER = LogManager.getLogger();

        public HashFactory() {
            this(new Sha256MessageDigestFactory());
        }

        public HashFactory(Sha256MessageDigestFactory messageDigestFactory) {
            this.messageDigestFactory = messageDigestFactory;
        }

        public String getHash(DvaPayload request, boolean isImposterStub)
                throws NoSuchAlgorithmException {
            String message;
            if (isImposterStub) {
                // no requestId used in hash generation if connecting to imposter stub
                message =
                        request.getIssuerId()
                                + Objects.toString(request.getSurname(), "")
                                + request.getForenames().stream()
                                        .filter(Objects::nonNull)
                                        .reduce("", String::concat)
                                + Objects.toString(request.getDateOfBirth(), "")
                                + request.getIssueDate()
                                + request.getExpiryDate()
                                + request.getDriverLicenceNumber()
                                + Objects.toString(request.getPostcode(), "");
            } else {
                message =
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
            }
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
}
