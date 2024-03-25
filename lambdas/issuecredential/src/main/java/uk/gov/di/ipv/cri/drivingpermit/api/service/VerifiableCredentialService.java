package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ThirdPartyAddress;
import uk.gov.di.ipv.cri.drivingpermit.api.util.EvidenceHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.service.parameterstore.ParameterPrefix;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.VerifiableCredentialConstants.*;
import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVLA;

public class VerifiableCredentialService {

    private final ObjectMapper objectMapper;
    private final ParameterStoreService parameterStoreService;
    private final ConfigurationService commonLibConfigurationService;
    private final SignedJWTFactory signedJwtFactory;
    private final VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder;

    public VerifiableCredentialService(ServiceFactory serviceFactory, JWSSigner jwsSigner) {
        this.objectMapper = serviceFactory.getObjectMapper();
        this.parameterStoreService = serviceFactory.getParameterStoreService();
        this.commonLibConfigurationService = serviceFactory.getCommonLibConfigurationService();

        this.signedJwtFactory = new SignedJWTFactory(jwsSigner);

        this.vcClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        commonLibConfigurationService, Clock.systemUTC());
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(
            String subject,
            DocumentCheckResultItem documentCheckResultItem,
            PersonIdentityDetailed personIdentityDetailed)
            throws JOSEException {
        long jwtTtl = commonLibConfigurationService.getMaxJwtTtl();

        ChronoUnit jwtTtlUnit =
                ChronoUnit.valueOf(
                        parameterStoreService.getParameterValue(
                                ParameterPrefix.STACK, ParameterStoreParameters.MAX_JWT_TTL_UNIT));

        var claimsSet =
                this.vcClaimsSetBuilder
                        .subject(subject)
                        .verifiableCredentialType(DRIVING_PERMIT_CREDENTIAL_TYPE)
                        .timeToLive(jwtTtl, jwtTtlUnit)
                        .verifiableCredentialSubject(
                                Map.of(
                                        VC_ADDRESS_KEY,
                                        convertAddresses(personIdentityDetailed.getAddresses()),
                                        VC_NAME_KEY,
                                        personIdentityDetailed.getNames(),
                                        VC_BIRTHDATE_KEY,
                                        convertBirthDates(personIdentityDetailed.getBirthDates()),
                                        VC_DRIVING_PERMIT_KEY,
                                        convertDrivingPermits(documentCheckResultItem)))
                        .verifiableCredentialEvidence(calculateEvidence(documentCheckResultItem))
                        .build();

        return signedJwtFactory.createSignedJwt(claimsSet);
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

    private Object[] convertDrivingPermits(DocumentCheckResultItem documentCheckResultItem) {
        final DrivingPermit drivingPermit = new DrivingPermit();

        IssuingAuthority issuingAuthority =
                IssuingAuthority.valueOf(documentCheckResultItem.getIssuedBy());

        drivingPermit.setPersonalNumber(documentCheckResultItem.getDocumentNumber());
        drivingPermit.setExpiryDate(documentCheckResultItem.getExpiryDate());
        drivingPermit.setIssuedBy(documentCheckResultItem.getIssuedBy());
        drivingPermit.setIssueDate(documentCheckResultItem.getIssueDate());

        // DVLA only field(s)
        if (issuingAuthority == DVLA) {
            drivingPermit.setIssueNumber(documentCheckResultItem.getIssueNumber());
        }

        return new Map[] {objectMapper.convertValue(drivingPermit, Map.class)};
    }

    private Object[] calculateEvidence(DocumentCheckResultItem documentCheckResultItem) {
        return new Map[] {
            objectMapper.convertValue(
                    EvidenceHelper.documentCheckResultItemToEvidence(documentCheckResultItem),
                    Map.class)
        };
    }
}
