package uk.gov.di.ipv.cri.drivingpermit.certreminder.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.config.CertExpiryReminderConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions;
import uk.gov.di.ipv.cri.drivingpermit.library.service.ParameterStoreService;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CertExpiryReminderHandler implements RequestHandler<Object, Object> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final CertExpiryReminderConfig certExpiryReminderConfig;
    private final EventProbe eventProbe;

    @ExcludeFromGeneratedCoverageReport
    public CertExpiryReminderHandler()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        ClientProviderFactory clientProviderFactory = new ClientProviderFactory();

        ParameterStoreService parameterStoreService =
                new ParameterStoreService(clientProviderFactory.getSSMProvider());

        certExpiryReminderConfig = new CertExpiryReminderConfig(parameterStoreService);

        this.eventProbe = new EventProbe();
    }

    public CertExpiryReminderHandler(
            CertExpiryReminderConfig certExpiryReminderConfig, EventProbe eventProbe) {
        LOGGER.info("CONSTRUCTING...");
        this.certExpiryReminderConfig = certExpiryReminderConfig;
        this.eventProbe = eventProbe;
    }

    @Override
    @Metrics(captureColdStart = true)
    @Logging(correlationIdPath = CorrelationIdPathConstants.EVENT_BRIDGE)
    public Object handleRequest(Object input, Context context) {
        LOGGER.info("Handling requests");

        Map<String, LocalDate> certificates = new HashMap<>();

        LOGGER.info("Loading Certificates...");
        for (Map.Entry<String, X509Certificate> certificate :
                certExpiryReminderConfig.getDVACertificates().entrySet()) {
            Date date = certificate.getValue().getNotAfter();
            certificates.put(certificate.getKey(), convertToLocalDate(date));
        }

        LOGGER.info("Setting expiry window");
        LocalDate expiryWindow = LocalDate.now().plusWeeks(4);
        Map<String, String> certExpiryMap = new HashMap<>();

        LOGGER.info("Checking Certificates...");
        for (Map.Entry<String, LocalDate> certificate : certificates.entrySet()) {
            if (certificate.getValue().isAfter(LocalDate.now())
                    && certificate.getValue().isBefore(expiryWindow)) {
                certExpiryMap.put(certificate.getKey(), certificate.getValue().toString());
                LOGGER.warn(
                        "cert path - {}, expires - {}",
                        certificate.getKey(),
                        certificate.getValue());

                eventProbe.counterMetric(Definitions.CERTIFICATE_EXPIRY_REMINDER);
                eventProbe
                        .counterMetric(Definitions.CERTIFICATE_EXPIRYS)
                        .addDimensions(certExpiryMap);

            } else {
                LOGGER.info(
                        "cert path - {}, expires - {}",
                        certificate.getKey(),
                        certificate.getValue());
            }
        }
        LOGGER.info("Returning expired Certificates...");
        return "";
    }

    public LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
