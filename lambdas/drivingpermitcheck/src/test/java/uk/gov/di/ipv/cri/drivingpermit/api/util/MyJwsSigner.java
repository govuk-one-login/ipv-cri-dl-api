package uk.gov.di.ipv.cri.drivingpermit.api.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;

import java.util.HashSet;
import java.util.Set;

public class MyJwsSigner implements JWSSigner {
    @Override
    public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
        return new Base64URL("base64Url");
    }

    @Override
    public Set<JWSAlgorithm> supportedJWSAlgorithms() {
        HashSet<JWSAlgorithm> hashSet = new HashSet<>();
        hashSet.add(JWSAlgorithm.EdDSA);
        return hashSet;
    }

    @Override
    public JCAContext getJCAContext() {
        return new JCAContext();
    }
}
