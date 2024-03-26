package uk.gov.di.ipv.cri.drivingpermit.api.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.di.ipv.cri.common.library.domain.SessionRequest;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.CanonicalAddress;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityDateOfBirth;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityItem;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityName;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityNamePart;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityMapper;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.common.library.util.ListUtil;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.drivingpermit.api.handler.IssueCredentialHandler;
import uk.gov.di.ipv.cri.drivingpermit.api.pact.utils.Injector;
import uk.gov.di.ipv.cri.drivingpermit.api.pact.utils.MockHttpServer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.DocumentCheckRetrievalService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// For static tests against potential new contracts
@Tag("Pact")
@Provider("DrivingLicenceVcProvider")
@PactBroker(
        url = "https://${PACT_BROKER_HOST}",
        authentication =
                @PactBrokerAuth(
                        username = "${PACT_BROKER_USERNAME}",
                        password = "${PACT_BROKER_PASSWORD}"))
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IssueCredentialHandlerTest {

    private static final int PORT = 5050;

    @Mock private ConfigurationService configurationService;

    @Mock
    private uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService
            drivingPermitConfigService;

    @Mock private DataStore<SessionItem> dataStore;
    @Mock private DataStore<PersonIdentityItem> personIdentityDataStore;
    @Mock private DataStore<DocumentCheckResultItem> documentCheckResultStore;
    @Mock private EventProbe eventProbe;
    @Mock private AuditService auditService;
    private SessionService sessionService;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModules(new JavaTimeModule());

    @BeforeAll
    static void setupServer() {
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.content_type.override.application/jwt", "text");
    }

    @au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
                .tag("DrivingLicenceVcProvider")
                .branch("main", "IpvCoreBack")
                .deployedOrReleased();
    }

    @BeforeEach
    void pactSetup(PactVerificationContext context)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

        long todayPlusADay =
                LocalDate.now().plusDays(2).toEpochSecond(LocalTime.now(), ZoneOffset.UTC);

        when(configurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyDrivingLicenceComponentId");
        when(configurationService.getSessionExpirationEpoch()).thenReturn(todayPlusADay);
        when(configurationService.getAuthorizationCodeExpirationEpoch()).thenReturn(todayPlusADay);
        when(configurationService.getMaxJwtTtl()).thenReturn(1000L);
        when(configurationService.getParameterValue("JwtTtlUnit")).thenReturn("HOURS");
        when(configurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyDrivingLicenceComponentId");
        when(configurationService.getParameterValueByAbsoluteName(
                        "/release-flags/vc-expiry-removed"))
                .thenReturn("true");
        when(configurationService.getParameterValue("release-flags/vc-contains-unique-id"))
                .thenReturn("true");

        sessionService =
                new SessionService(
                        dataStore, configurationService, Clock.systemUTC(), new ListUtil());

        KeyFactory kf = KeyFactory.getInstance("EC");
        EncodedKeySpec privateKeySpec =
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder()
                                .decode(
                                        "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBYNBSda5ttN9Wu4Do4"
                                                + "gLV1xaks+DB5n6ity2MvBlzDUw=="));
        JWSSigner signer = new ECDSASigner((ECPrivateKey) kf.generatePrivate(privateKeySpec));

        SignedJWTFactory signedJWTFactory = new SignedJWTFactory(signer);

        VerifiableCredentialClaimsSetBuilder verifiableCredentialClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        this.configurationService, Clock.systemUTC());

        Injector tokenHandlerInjector =
                new Injector(
                        new IssueCredentialHandler(
                                new VerifiableCredentialService(
                                        signedJWTFactory,
                                        configurationService,
                                        objectMapper,
                                        verifiableCredentialClaimsSetBuilder),
                                sessionService,
                                eventProbe,
                                auditService,
                                new PersonIdentityService(
                                        new PersonIdentityMapper(),
                                        configurationService,
                                        personIdentityDataStore),
                                new DocumentCheckRetrievalService(
                                        documentCheckResultStore, drivingPermitConfigService)),
                        "/credential/issue",
                        "/");
        MockHttpServer.startServer(new ArrayList<>(List.of(tokenHandlerInjector)), PORT, signer);

        context.setTarget(new HttpTestTarget("localhost", PORT));
    }

    @AfterEach
    public void tearDown() {
        MockHttpServer.stopServer();
    }

    @State("dummyApiKey is a valid api key")
    void dummyAPIKeyIsValid() {}

    @State("dummyAccessToken is a valid access token")
    void accessTokenIsValid() {
        long todayPlusADay =
                LocalDate.now().plusDays(2).toEpochSecond(LocalTime.now(), ZoneOffset.UTC);

        // INITIAL SESSION HANDOFF
        UUID sessionId = performInitialSessionRequest(sessionService, todayPlusADay);
        setSessionIntoMockDB(sessionId);
        // INITIAL SESSION HANDOFF

        // SIMULATED CRI LOGIC
        PersonIdentityNamePart firstNamePart = new PersonIdentityNamePart();
        firstNamePart.setType("GivenName");
        firstNamePart.setValue("PETER");
        PersonIdentityNamePart middleNamePart = new PersonIdentityNamePart();
        middleNamePart.setType("GivenName");
        middleNamePart.setValue("BENJAMIN");
        PersonIdentityNamePart surnamePart = new PersonIdentityNamePart();
        surnamePart.setType("FamilyName");
        surnamePart.setValue("PARKER");
        PersonIdentityName name = new PersonIdentityName();
        name.setNameParts(List.of(firstNamePart, middleNamePart, surnamePart));

        PersonIdentityDateOfBirth birthDate = new PersonIdentityDateOfBirth();
        birthDate.setValue(LocalDate.of(1962, 10, 11));

        CanonicalAddress address = new CanonicalAddress();
        address.setAddressCountry("GB");
        address.setPostalCode("BS981TL");
        address.setValidFrom(LocalDate.now().minusDays(1));

        PersonIdentityItem personIdentityItem = new PersonIdentityItem();
        personIdentityItem.setExpiryDate(
                LocalDate.of(2030, 1, 1).toEpochSecond(LocalTime.now(), ZoneOffset.UTC));
        personIdentityItem.setSessionId(sessionId);
        personIdentityItem.setAddresses(List.of(address));
        personIdentityItem.setNames(List.of(name));
        personIdentityItem.setBirthDates(List.of(birthDate));

        when(personIdentityDataStore.getItem(sessionId.toString())).thenReturn(personIdentityItem);

        // SESSION HANDBACK
        performAuthorizationCodeSet(sessionService, sessionId);
        // SESSION HANDBACK

        // ACCESS TOKEN GENERATION AND SETTING
        SessionItem session = performAccessTokenSet(sessionService, sessionId);
        // ACCESS TOKEN GENERATION AND SETTING

        when(dataStore.getItemByIndex(SessionItem.ACCESS_TOKEN_INDEX, "Bearer dummyAccessToken"))
                .thenReturn(List.of(session));
    }

    @State("dummyInvalidAccessToken is an invalid access token")
    void invalidAccessTokenIsInvalidAccessToken() {}

    @State("test-subject is a valid subject")
    void jwtSubjectIsValid() {}

    @State("dummyDrivingLicenceComponentId is a valid issuer")
    void componentIdIsValidIssue() {}

    @State("VC evidence activityHistoryScore is 1")
    void vcHasDesiredActivityHistoryScore() {}

    @State("VC has a CI of D02")
    void vcHasDesiredCi() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        DocumentCheckResultItem documentCheckResultItem = createBaseDocumentResultItem(sessionUUID);
        documentCheckResultItem.setDocumentNumber("PARKE610112PBFGH");
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");
        documentCheckResultItem.setActivityFrom("1982-05-23");
        documentCheckResultItem.setValidityScore(0);
        documentCheckResultItem.setIssuedBy("DVLA");
        documentCheckResultItem.setIssueNumber("12");
        documentCheckResultItem.setIssueDate("1982-05-23");
        documentCheckResultItem.setContraIndicators(List.of("D02"));
        documentCheckResultItem.setActivityHistoryScore(0);
        when(documentCheckResultStore.getItem(sessionId)).thenReturn(documentCheckResultItem);
    }

    @State("VC evidence validityScore is 2")
    void vcHasDesiredValidityScore() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        DocumentCheckResultItem documentCheckResultItem = createBaseDocumentResultItem(sessionUUID);
        documentCheckResultItem.setDocumentNumber("PARKE610112PBFGH");
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");
        documentCheckResultItem.setActivityFrom("1982-05-23");
        documentCheckResultItem.setValidityScore(2);
        documentCheckResultItem.setIssuedBy("DVLA");
        documentCheckResultItem.setIssueNumber("12");
        documentCheckResultItem.setIssueDate("1982-05-23");
        documentCheckResultItem.setActivityHistoryScore(1);
        when(documentCheckResultStore.getItem(sessionId)).thenReturn(documentCheckResultItem);
    }

    @State("VC evidence txn is dummyTxn")
    void vcHasDesiredTxn() {}

    @State("VC evidence checkDetails activityFrom is 1982-05-23")
    void vcHasDesiredActivityFrom() {}

    @State("VC address is BS981TL, GB")
    void vcHasDesiredAddress() {}

    @State("VC is for Peter Benjamin Parker")
    void vcHasTheDesiredName() {}

    @State("VC driving licence issueNumber is 12")
    void vcHasDesiredIssueNumber() {}

    @State("VC driving licence issuedBy is DVLA")
    void vcHasDesiredIssuer() {}

    @State("VC driving licence issuedBy is DVA")
    void vcHasDesiredDvaIssuer() {}

    @State("VC birthDate is 1962-10-11")
    void vcHasTheDesiredBirthDate() {}

    @State("VC driving licence personalNumber is PARKE610112PBFGH")
    void vcHasTheDesiredDocumentNumber() {}

    @State("VC driving licence personalNumber is 55667788")
    void vcHasDesiredDvaDocumentNumber() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        DocumentCheckResultItem documentCheckResultItem = createBaseDocumentResultItem(sessionUUID);
        documentCheckResultItem.setDocumentNumber("55667788");
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");
        documentCheckResultItem.setActivityFrom("1982-05-23");
        documentCheckResultItem.setValidityScore(2);
        documentCheckResultItem.setIssuedBy("DVA");
        documentCheckResultItem.setIssueNumber("12");
        documentCheckResultItem.setIssueDate("1982-05-23");
        documentCheckResultItem.setActivityHistoryScore(1);
        when(documentCheckResultStore.getItem(sessionId)).thenReturn(documentCheckResultItem);
    }

    @State("VC driving licence personalNumber is 55667780")
    void vcHasIncorrectDvaDocumentNumber() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        DocumentCheckResultItem documentCheckResultItem = createBaseDocumentResultItem(sessionUUID);
        documentCheckResultItem.setDocumentNumber("55667780");
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");
        documentCheckResultItem.setActivityFrom("1982-05-23");
        documentCheckResultItem.setValidityScore(0);
        documentCheckResultItem.setIssuedBy("DVA");
        documentCheckResultItem.setIssueNumber("12");
        documentCheckResultItem.setIssueDate("1982-05-23");
        documentCheckResultItem.setActivityHistoryScore(0);
        documentCheckResultItem.setContraIndicators(List.of("D02"));
        ;
        when(documentCheckResultStore.getItem(sessionId)).thenReturn(documentCheckResultItem);
    }

    @State("VC driving licence issuedDate is 1982-05-23")
    void vcHasTheDesiredIssueDate() {}

    @State("VC driving licence expiryDate is 2062-12-09")
    void vcHasTheDesiredExpiryDate() {}

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testMethod(PactVerificationContext context) {
        context.verifyInteraction();
    }

    private SessionItem performAuthorizationCodeSet(SessionService sessionService, UUID sessionId) {
        SessionItem session = sessionService.getSession(sessionId.toString());
        sessionService.createAuthorizationCode(session);
        session.setAuthorizationCode("dummyAuthCode");
        sessionService.updateSession(session);
        return session;
    }

    private SessionItem performAccessTokenSet(SessionService sessionService, UUID sessionId) {
        SessionItem session = sessionService.getSession(sessionId.toString());
        session.setAccessToken("dummyAccessToken");
        session.setAccessTokenExpiryDate(
                LocalDate.now().plusDays(1).toEpochSecond(LocalTime.now(), ZoneOffset.UTC));
        sessionService.updateSession(session);
        return session;
    }

    private void setSessionIntoMockDB(UUID sessionId) {
        ArgumentCaptor<SessionItem> sessionItemArgumentCaptor =
                ArgumentCaptor.forClass(SessionItem.class);

        verify(dataStore).create(sessionItemArgumentCaptor.capture());

        SessionItem savedSessionitem = sessionItemArgumentCaptor.getValue();

        when(dataStore.getItem(sessionId.toString())).thenReturn(savedSessionitem);
    }

    private UUID performInitialSessionRequest(SessionService sessionService, long todayPlusADay) {
        SessionRequest sessionRequest = new SessionRequest();
        sessionRequest.setNotBeforeTime(new Date(todayPlusADay));
        sessionRequest.setClientId("ipv-core");
        sessionRequest.setAudience("dummyDrivingLicenceComponentId");
        sessionRequest.setRedirectUri(URI.create("http://localhost:5050"));
        sessionRequest.setExpirationTime(new Date(todayPlusADay));
        sessionRequest.setIssuer("ipv-core");
        sessionRequest.setClientId("ipv-core");
        sessionRequest.setSubject("test-subject");

        doNothing().when(dataStore).create(any(SessionItem.class));

        return sessionService.saveSession(sessionRequest);
    }

    private static DocumentCheckResultItem createBaseDocumentResultItem(UUID sessionUUID) {
        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();
        documentCheckResultItem.setStrengthScore(3);
        documentCheckResultItem.setTransactionId("dummyTxn");
        documentCheckResultItem.setExpiry(10000L);
        documentCheckResultItem.setExpiryDate(LocalDate.of(2062, 12, 9).toString());
        documentCheckResultItem.setSessionId(sessionUUID);
        return documentCheckResultItem;
    }
}
