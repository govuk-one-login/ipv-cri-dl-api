package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.*;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.verifiablecredential.EvidenceType;
import uk.gov.di.ipv.cri.drivingpermit.api.service.fixtures.TestFixtures;
import uk.gov.di.ipv.cri.drivingpermit.api.util.PersonIdentityDetailedTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.api.util.VcIssuedAuditHelper;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.testdata.DocumentCheckTestDataGenerator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.VerifiableCredentialConstants.*;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class VerifiableCredentialServiceTest implements TestFixtures {

    private static final Logger LOGGER = LogManager.getLogger();

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private final String UNIT_TEST_VC_ISSUER = "UNIT_TEST_VC_ISSUER";
    private final String UNIT_TEST_SUBJECT = "urn:fdc:12345678";

    private final String UNIT_TEST_MAX_JWT_TTL_UNIT = "SECONDS";
    private final long UNIT_TEST_MAX_JWT_TTL = 100L;

    // Returned via the ServiceFactory
    private final ObjectMapper realObjectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ParameterStoreService mockParameterStoreService;
    @Mock private ConfigurationService mockCommonLibConfigurationService;

    private VerifiableCredentialService verifiableCredentialService;

    @BeforeEach
    void setup() throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        mockServiceFactoryBehaviour();

        JWSSigner jwsSigner = new ECDSASigner(getPrivateKey());

        verifiableCredentialService =
                new VerifiableCredentialService(mockServiceFactory, jwsSigner);
    }

    @ParameterizedTest
    @CsvSource({"DVA", "DVLA"})
    void testGenerateSignedVerifiableCredentialJWT(String issuer)
            throws JOSEException, JsonProcessingException, ParseException {

        PersonIdentityDetailed savedPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate(issuer);

        DocumentCheckResultItem savedDocumentCheckResultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), savedPersonIdentityDetailed);

        var auditEventPersonIdentityDetailed =
                VcIssuedAuditHelper.mapPersonIdentityDetailedAndDrivingPermitDataToAuditRestricted(
                        savedPersonIdentityDetailed, savedDocumentCheckResultItem);

        when(mockCommonLibConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn(UNIT_TEST_VC_ISSUER);
        when(mockCommonLibConfigurationService.getMaxJwtTtl()).thenReturn(UNIT_TEST_MAX_JWT_TTL);

        when(mockParameterStoreService.getStackParameterValue(
                        ParameterStoreParameters.MAX_JWT_TTL_UNIT))
                .thenReturn(UNIT_TEST_MAX_JWT_TTL_UNIT);

        SignedJWT signedJWT =
                verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        UNIT_TEST_SUBJECT,
                        savedDocumentCheckResultItem,
                        savedPersonIdentityDetailed);

        JWTClaimsSet generatedClaims = signedJWT.getJWTClaimsSet();
        assertTrue(signedJWT.verify(new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1))));

        String jsonGeneratedClaims =
                realObjectMapper
                        .writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(generatedClaims);
        LOGGER.info(jsonGeneratedClaims);

        JsonNode claimsSet = realObjectMapper.readTree(generatedClaims.toString());
        assertEquals(5, claimsSet.size());

        assertEquals(UNIT_TEST_SUBJECT, claimsSet.get("sub").textValue());
        assertEquals(UNIT_TEST_VC_ISSUER, claimsSet.get("iss").textValue());

        assertEquals(
                EvidenceType.IDENTITY_CHECK.toString(),
                claimsSet
                        .get(VC_CLAIM)
                        .get(VC_EVIDENCE_KEY)
                        .get(0)
                        .get(VC_EVIDENCE_TYPE_KEY)
                        .asText());

        assertEquals(
                savedDocumentCheckResultItem.getContraIndicators().get(0),
                claimsSet.get(VC_CLAIM).get(VC_EVIDENCE_KEY).get(0).get("ci").get(0).asText());
        assertEquals(
                savedDocumentCheckResultItem.getStrengthScore(),
                claimsSet.get(VC_CLAIM).get(VC_EVIDENCE_KEY).get(0).get("strengthScore").asInt());

        assertEquals(
                savedDocumentCheckResultItem.getValidityScore(),
                claimsSet.get(VC_CLAIM).get(VC_EVIDENCE_KEY).get(0).get("validityScore").asInt());

        List<Address> addresses = savedPersonIdentityDetailed.getAddresses();
        assertEquals(1, addresses.size());

        Address address = savedPersonIdentityDetailed.getAddresses().get(0);
        JsonNode claimSetJWTAddress =
                claimsSet.get(VC_CLAIM).get(VC_CREDENTIAL_SUBJECT).get(VC_ADDRESS_KEY).get(0);
        assertEquals(address.getPostalCode(), claimSetJWTAddress.get("postalCode").asText());
        assertEquals(
                UK_DRIVING_PERMIT_ADDRESS_COUNTRY,
                claimSetJWTAddress.get("addressCountry").asText());

        assertEquals(UNIT_TEST_VC_ISSUER, claimsSet.get("iss").textValue());
        assertEquals(UNIT_TEST_SUBJECT, claimsSet.get("sub").textValue());

        long notBeforeTime = claimsSet.get("nbf").asLong();
        final long expirationTime = claimsSet.get("exp").asLong();
        assertEquals(expirationTime, notBeforeTime + UNIT_TEST_MAX_JWT_TTL);

        ECDSAVerifier ecVerifier = new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1));
        assertTrue(signedJWT.verify(ecVerifier));
    }

    private void mockServiceFactoryBehaviour() {
        when(mockServiceFactory.getObjectMapper()).thenReturn(realObjectMapper);
        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getCommonLibConfigurationService())
                .thenReturn(mockCommonLibConfigurationService);
    }
}
