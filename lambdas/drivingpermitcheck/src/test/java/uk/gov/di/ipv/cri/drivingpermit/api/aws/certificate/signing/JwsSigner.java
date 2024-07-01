package uk.gov.di.ipv.cri.drivingpermit.api.aws.certificate.signing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwsSigner {
    private final KmsSigner kmsSigner;

    public JwsSigner(KmsSigner kmsSigner) {
        this.kmsSigner = kmsSigner;
    }

    public JWSObject createSignedJws() {
        try {
            // Create the JWS header
            JWSHeader header = new JWSHeader
                    .Builder(JWSAlgorithm.RS256)
                    .keyID(kmsSigner.getKeyId()) // Optionally, set the key ID
                    .build();

            // Create the JWT claims set
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("1234567890")
                    .issuer("issuer")
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                    .claim("name", "John Doe")
                    .build();

            // Create the JWS object
            JWSObject jwsObject = new JWSObject(header, claimsSet.toPayload());

            // Compute the signature
            byte[] signingInput = jwsObject.getSigningInput();


            byte[] signature = kmsSigner.sign(signingInput);

            ObjectMapper mapper = new ObjectMapper();
            Base64URL base64URLClaimSet = createBase64ClaimSet(claimsSet, mapper);
            Base64URL base64URLHeader = createBase64UrlHeader(header, mapper);

            // Set the signature on the JWS object
            jwsObject = new JWSObject(base64URLHeader, base64URLClaimSet, Base64URL.encode(signature));

            // Serialize the JWS to a compact form
            return jwsObject;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed JWS", e);
        }
    }

    private static Base64URL createBase64ClaimSet(JWTClaimsSet claimsSet, ObjectMapper mapper) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(claimsSet.toJSONObject());

        // Convert JSON string to Base64URL
        Base64URL base64URLClaimSet = Base64URL.encode(jsonString.getBytes(StandardCharsets.UTF_8));
        return base64URLClaimSet;
    }

    private static Base64URL createBase64UrlHeader(JWSHeader header, ObjectMapper mapper) throws JsonProcessingException {
        String headerJsonString = mapper.writeValueAsString(header.toJSONObject());

        // Convert JSON string to Base64URL
        Base64URL base64URLHeader = Base64URL.encode(headerJsonString.getBytes(StandardCharsets.UTF_8));
        return base64URLHeader;
    }

}