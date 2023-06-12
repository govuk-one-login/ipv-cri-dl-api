package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
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
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.*;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.drivingpermit.api.service.fixtures.TestFixtures;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.PersonIdentityDetailedHelperMapper;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DocumentCheckTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.Clock;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.drivingpermit.api.domain.VerifiableCredentialConstants.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class VerifiableCredentialServiceTest implements TestFixtures {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ADDRESSES_TO_GENERATE_IN_TEST = 5;

    private final String UNIT_TEST_VC_ISSUER = "UNIT_TEST_VC_ISSUER";
    private final long UNIT_TEST_MAX_JWT_TTL = 100L;
    private final String UNIT_TEST_SUBJECT = "UNIT_TEST_SUBJECT";

    @SystemStub private EnvironmentVariables environmentVariables;

    @Mock private ConfigurationService mockConfigurationService;

    private ObjectMapper objectMapper;

    private VerifiableCredentialService verifiableCredentialService;

    @BeforeEach
    void setup() throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {

        environmentVariables.set("AWS_REGION", "eu-west-2");

        SignedJWTFactory signedJwtFactory = new SignedJWTFactory(new ECDSASigner(getPrivateKey()));

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        when(mockConfigurationService.getParameterValue("JwtTtlUnit")).thenReturn("SECONDS");
        VerifiableCredentialClaimsSetBuilder verifiableCredentialClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        mockConfigurationService, Clock.systemUTC());
        verifiableCredentialService =
                new VerifiableCredentialService(
                        signedJwtFactory,
                        mockConfigurationService,
                        objectMapper,
                        verifiableCredentialClaimsSetBuilder);
    }

    @ParameterizedTest
    @MethodSource("getAddressCount")
    void testGenerateSignedVerifiableCredentialJWTWithAddressCount(int addressCount)
            throws JOSEException, JsonProcessingException, ParseException {
        DocumentCheckResultItem documentCheckResultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem();

        PersonIdentityDetailed personIdentityDetailed =
                PersonIdentityDetailedHelperMapper.drivingPermitFormDataToAuditRestrictedFormat(
                        DrivingPermitFormTestDataGenerator.generateWithMultipleAddresses(
                                addressCount, 0, 0, false));

        when(mockConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn(UNIT_TEST_VC_ISSUER);
        when(mockConfigurationService.getMaxJwtTtl()).thenReturn(UNIT_TEST_MAX_JWT_TTL);

        SignedJWT signedJWT =
                verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        UNIT_TEST_SUBJECT, documentCheckResultItem, personIdentityDetailed);

        JWTClaimsSet generatedClaims = signedJWT.getJWTClaimsSet();
        assertTrue(signedJWT.verify(new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1))));

        String jsonGeneratedClaims =
                objectMapper
                        .writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(generatedClaims);
        LOGGER.info(jsonGeneratedClaims);

        String issuer = verifiableCredentialService.getVerifiableCredentialIssuer();
        assertEquals(UNIT_TEST_VC_ISSUER, issuer);

        JsonNode claimsSet = objectMapper.readTree(generatedClaims.toString());
        assertEquals(5, claimsSet.size());

        assertAll(
                () -> {
                    assertEquals(
                            documentCheckResultItem.getContraIndicators().get(0),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_EVIDENCE_KEY)
                                    .get(0)
                                    .get("ci")
                                    .get(0)
                                    .asText());
                    assertEquals(
                            documentCheckResultItem.getStrengthScore(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_EVIDENCE_KEY)
                                    .get(0)
                                    .get("strengthScore")
                                    .asInt());

                    assertEquals(
                            documentCheckResultItem.getValidityScore(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_EVIDENCE_KEY)
                                    .get(0)
                                    .get("validityScore")
                                    .asInt());

                    assertNotEquals(0, addressCount);

                    IntStream.range(0, addressCount)
                            .forEach(
                                    a -> {
                                        Address address =
                                                personIdentityDetailed.getAddresses().get(a);
                                        JsonNode claimSetJWTAddress =
                                                claimsSet
                                                        .get(VC_CLAIM)
                                                        .get(VC_CREDENTIAL_SUBJECT)
                                                        .get(VC_ADDRESS_KEY)
                                                        .get(a);
                                        assertEquals(
                                                address.getBuildingNumber(),
                                                claimSetJWTAddress.get("buildingNumber").asText());

                                        assertEquals(
                                                address.getStreetName(),
                                                claimSetJWTAddress.get("streetName").asText());
                                        assertEquals(
                                                address.getAddressLocality(),
                                                claimSetJWTAddress.get("addressLocality").asText());
                                        assertEquals(
                                                address.getPostalCode(),
                                                claimSetJWTAddress.get("postalCode").asText());
                                        assertEquals(
                                                "GB",
                                                claimSetJWTAddress.get("addressCountry").asText());
                                    });
                });
        assertEquals(UNIT_TEST_VC_ISSUER, claimsSet.get("iss").textValue());
        assertEquals(UNIT_TEST_SUBJECT, claimsSet.get("sub").textValue());

        long notBeforeTime = claimsSet.get("nbf").asLong();
        final long expirationTime = claimsSet.get("exp").asLong();
        assertEquals(expirationTime, notBeforeTime + UNIT_TEST_MAX_JWT_TTL);

        ECDSAVerifier ecVerifier = new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1));
        assertTrue(signedJWT.verify(ecVerifier));
    }

    private static int[] getAddressCount() {
        return IntStream.range(1, ADDRESSES_TO_GENERATE_IN_TEST).toArray();
    }
}
