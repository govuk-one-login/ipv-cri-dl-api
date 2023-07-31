package uk.gov.di.ipv.cri.drivingpermit.api.util;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFactory {
    private final Sha256MessageDigestFactory messageDigestFactory;

    public HashFactory() {
        this(new Sha256MessageDigestFactory());
    }

    public HashFactory(Sha256MessageDigestFactory messageDigestFactory) {
        this.messageDigestFactory = messageDigestFactory;
    }

    public String getHash(String requestString) {
        MessageDigest sha256;
        try {
            sha256 = messageDigestFactory.getInstance();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] digest = sha256.digest(requestString.getBytes());

        return Hex.encodeHexString(digest);
    }

    public static class Sha256MessageDigestFactory {
        public MessageDigest getInstance() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance("SHA-256");
        }
    }
}
