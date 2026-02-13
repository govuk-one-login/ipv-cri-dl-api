package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.config.CertExpiryReminderConfig;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.handler.CertExpiryReminderHandler;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.CERTIFICATE_EXPIRED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.CERTIFICATE_EXPIRY_REMINDER;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_OK;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class CertExpiryReminderHandlerTest {

    @Mock private CertExpiryReminderConfig certExpiryReminderConfig;
    @Mock private X509Certificate mockDvaSigningCert;
    @Mock private X509Certificate mockDvaEncryptionCert;
    @Mock private X509Certificate mockDvaTlsRootCert;
    @Mock private X509Certificate mockDvaTlsIntermediateCert;
    @Mock private X509Certificate mockDvaTlsCert;
    @Mock private X509Certificate mockDvaSigningThumbCert;
    @Mock private X509Certificate mockDvaHeldTlsRootCert;
    @Mock private X509Certificate mockDvaHeldTlsIntermediateCert;
    @Mock private X509Certificate mockDvaHeldSigningCert;
    @Mock private X509Certificate mockDvaHeldEncryptionCert;
    @Mock private EventProbe eventProbe;

    @Mock private Context context;

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private CertExpiryReminderHandler certExpiryReminderHandler;

    @BeforeEach()
    void setup() {
        environmentVariables.set("POWERTOOLS_METRICS_NAMESPACE", "StackName");
        // The following certs are outside the expiryWindow (4 weeks)
        createCertificateMocks();

        when(certExpiryReminderConfig.getDVACertificates())
                .thenReturn(
                        Map.of(
                                "Encryption", mockDvaEncryptionCert,
                                "tlsRoot", mockDvaTlsRootCert,
                                "signingCert", mockDvaSigningCert,
                                "intermediate", mockDvaTlsIntermediateCert,
                                "thumb", mockDvaSigningThumbCert,
                                "tlsCert", mockDvaTlsCert,
                                "dvaHeldTlsRootCert", mockDvaHeldTlsRootCert,
                                "dvaHeldTlsIntermediateCert", mockDvaHeldTlsIntermediateCert,
                                "dvaHeldSigningCert", mockDvaHeldSigningCert,
                                "dvaHeldEncryptionCert", mockDvaHeldEncryptionCert));

        // Use below certificate as control for tests
        this.certExpiryReminderHandler =
                new CertExpiryReminderHandler(certExpiryReminderConfig, eventProbe);
    }

    @Test
    void HandlerShouldRecordReminderMetricWhenCertIsCloseToExpiry() {
        Date expiry =
                Date.from(
                        LocalDate.now()
                                .atStartOfDay(ZoneId.systemDefault())
                                .plusWeeks(3)
                                .toInstant());
        when(mockDvaSigningCert.getNotAfter()).thenReturn(expiry);
        when(eventProbe.counterMetric(anyString())).thenReturn(eventProbe);

        certExpiryReminderHandler.handleRequest(null, context);

        Map<String, String> certExpiryMap = new HashMap<String, String>();
        certExpiryMap.put(
                "signingCert",
                expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());

        InOrder inOrder = inOrder(eventProbe);
        inOrder.verify(eventProbe).counterMetric(CERTIFICATE_EXPIRY_REMINDER);
        inOrder.verify(eventProbe).counterMetric(CERTIFICATE_EXPIRED);
        inOrder.verify(eventProbe).addDimensions(certExpiryMap);
        inOrder.verify(eventProbe).counterMetric(LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_OK);
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(eventProbe);
    }

    @Test
    void HandlerShouldIncrementOkMetricCountWhenCompletedSuccessfully() {
        when(mockDvaSigningCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));

        when(eventProbe.counterMetric(anyString())).thenReturn(eventProbe);

        certExpiryReminderHandler.handleRequest(null, context);

        InOrder inOrder = inOrder(eventProbe);
        inOrder.verify(eventProbe).counterMetric(LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_OK);
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(eventProbe);
    }

    private void createCertificateMocks() {
        when(mockDvaEncryptionCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaTlsRootCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaSigningCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaTlsIntermediateCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaTlsCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaSigningThumbCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaHeldTlsRootCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaHeldTlsIntermediateCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaHeldSigningCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
        when(mockDvaHeldEncryptionCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));
    }
}
