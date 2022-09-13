package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.KMSSigner;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ThirdPartyAddress;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.VerifiableCredentialConstants;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.Evidence;
import uk.gov.di.ipv.cri.drivingpermit.api.persistence.item.DocumentCheckResultItem;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.nimbusds.jwt.JWTClaimNames.EXPIRATION_TIME;
import static com.nimbusds.jwt.JWTClaimNames.ISSUER;
import static com.nimbusds.jwt.JWTClaimNames.NOT_BEFORE;
import static com.nimbusds.jwt.JWTClaimNames.SUBJECT;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.VerifiableCredentialConstants.*;

public class VerifiableCredentialService {

    private final SignedJWTFactory signedJwtFactory;
    private final ConfigurationService configurationService;
    private final ObjectMapper objectMapper;

    public VerifiableCredentialService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.signedJwtFactory =
                new SignedJWTFactory(
                        new KMSSigner(
                                configurationService.getVerifiableCredentialKmsSigningKeyId()));
        this.objectMapper =
                new ObjectMapper()
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());
    }

    public VerifiableCredentialService(
            SignedJWTFactory signedClaimSetJwt,
            ConfigurationService configurationService,
            ObjectMapper objectMapper) {
        this.signedJwtFactory = signedClaimSetJwt;
        this.configurationService = configurationService;
        this.objectMapper = objectMapper;
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(
            String subject,
            DocumentCheckResultItem documentCheckResultItem,
            PersonIdentityDetailed personIdentityDetailed)
            throws JOSEException {
        var now = Instant.now();

        var claimsSet =
                new JWTClaimsSet.Builder()
                        .claim(SUBJECT, subject)
                        .claim(ISSUER, configurationService.getVerifiableCredentialIssuer())
                        .claim(NOT_BEFORE, now.getEpochSecond())
                        .claim(
                                EXPIRATION_TIME,
                                now.plusSeconds(configurationService.getMaxJwtTtl())
                                        .getEpochSecond())
                        .claim(
                                VerifiableCredentialConstants.VC_CLAIM,
                                Map.of(
                                        VC_TYPE,
                                        new String[] {
                                            VERIFIABLE_CREDENTIAL_TYPE,
                                            DRIVING_PERMIT_CREDENTIAL_TYPE
                                        },
                                        VC_CREDENTIAL_SUBJECT,
                                        Map.of(
                                                VC_ADDRESS_KEY,
                                                convertAddresses(
                                                        personIdentityDetailed.getAddresses()),
                                                VC_NAME_KEY,
                                                personIdentityDetailed.getNames(),
                                                VC_BIRTHDATE_KEY,
                                                convertBirthDates(
                                                        personIdentityDetailed.getBirthDates())),
                                        VC_EVIDENCE_KEY,
                                        calculateEvidence(documentCheckResultItem)))
                        .build();

        return signedJwtFactory.createSignedJwt(claimsSet);
    }

    public String getVerifiableCredentialIssuer() {
        return configurationService.getVerifiableCredentialIssuer();
    }

    private Object[] convertAddresses(List<Address> addresses) {
        return addresses.stream()
                .map(address -> objectMapper.convertValue(address, ThirdPartyAddress.class))
                .toArray();
    }

    private Object[] convertBirthDates(List<BirthDate> birthDates) {
        return birthDates.stream()
                .map(
                        birthDate ->
                                Map.of(
                                        "value",
                                        birthDate
                                                .getValue()
                                                .format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .toArray();
    }

    private Object[] calculateEvidence(DocumentCheckResultItem documentCheckResultItem) {

        Evidence evidence = new Evidence();
        evidence.setType("IdentityCheck");
        evidence.setTxn(documentCheckResultItem.getTransactionId());

        evidence.setStrengthScore(documentCheckResultItem.getStrengthScore());
        evidence.setValidityScore(documentCheckResultItem.getValidityScore());

        evidence.setCi(documentCheckResultItem.getContraIndicators());

        return new Map[] {objectMapper.convertValue(evidence, Map.class)};
    }
}
