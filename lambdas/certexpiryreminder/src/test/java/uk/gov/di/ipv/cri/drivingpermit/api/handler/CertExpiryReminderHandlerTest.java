package uk.gov.di.ipv.cri.drivingpermit.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.handler.CertExpiryReminderHandler;
import uk.gov.di.ipv.cri.drivingpermit.library.dva.configuration.DvaConfiguration;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CertExpiryReminderHandlerTest {

    @Mock private DvaConfiguration dvaConfiguration;
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

    private CertExpiryReminderHandler certExpiryReminderHandler;

    @BeforeEach()
    void setup() {
        // The following certs are outside the expiryWindow (4 weeks)
        createCertificateMocks();

        when(dvaConfiguration.getDVACertificates())
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
                new CertExpiryReminderHandler(dvaConfiguration, eventProbe);
    }

    @Test
    void HandlerShouldIncrementMetricCountWhenCertIsCloseToExpiry() {
        Date expiry =
                Date.from(
                        LocalDate.now()
                                .atStartOfDay(ZoneId.systemDefault())
                                .plusWeeks(3)
                                .toInstant());
        when(mockDvaSigningCert.getNotAfter()).thenReturn(expiry);
        when(eventProbe.counterMetric(anyString())).thenReturn(eventProbe);

        LocalDate expiryWindow =
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusWeeks(3).toLocalDate();

        certExpiryReminderHandler.handleRequest(null, context);

        Map<String, String> certExpiryMap = new HashMap<String, String>();
        certExpiryMap.put(
                "signingCert",
                expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());

        verify(eventProbe).addDimensions(certExpiryMap);
    }

    @Test
    void HandlerShouldNotIncrementMetricCountWhenCertIsOutsideExpiryWindow() {
        when(mockDvaSigningCert.getNotAfter())
                .thenReturn(
                        Date.from(
                                LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .plusWeeks(5)
                                        .toInstant()));

        certExpiryReminderHandler.handleRequest(null, context);

        verifyNoInteractions(eventProbe);
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
