package uk.gov.di.ipv.cri.drivingpermit.certreminder.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.logging.CorrelationIdPaths;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.FlushMetrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.util.ClientProviderFactory;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.certreminder.config.CertExpiryReminderConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.logging.LoggingSupport;
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

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_OK;

public class CertExpiryReminderHandler implements RequestHandler<Object, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertExpiryReminderHandler.class);

    static {
        LoggingSupport.populateLambdaInitLoggerValues();
    }

    private final CertExpiryReminderConfig certExpiryReminderConfig;
    private final EventProbe eventProbe;

    @ExcludeFromGeneratedCoverageReport
    public CertExpiryReminderHandler()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        ClientProviderFactory clientProviderFactory = new ClientProviderFactory(true, true);

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
    @FlushMetrics(captureColdStart = true)
    @Logging(clearState = true, correlationIdPath = CorrelationIdPaths.EVENT_BRIDGE)
    public Object handleRequest(Object input, Context context) {
        try {
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
                            .counterMetric(Definitions.CERTIFICATE_EXPIRED)
                            .addDimensions(certExpiryMap);
                    // number of dimensions will go out of range if ALL certs expire
                } else {
                    LOGGER.info(
                            "cert path - {}, expires - {}",
                            certificate.getKey(),
                            certificate.getValue());
                }
            }
            LOGGER.info("Returning expired Certificates...");
            return completedOk();
        } catch (Exception e) {
            LOGGER.error(
                    "Cert expiry reminder failed in lambda {}: {}",
                    context.getFunctionName(),
                    e.getMessage(),
                    e);
            eventProbe.counterMetric(LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_ERROR);
            throw new RuntimeException(e);
        }
    }

    private String completedOk() {
        // Lambda Complete No Error
        eventProbe.counterMetric(LAMBDA_CERT_EXPIRY_REMINDER_COMPLETED_OK);
        return "Success";
    }

    public LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
