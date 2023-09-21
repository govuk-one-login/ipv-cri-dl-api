package uk.gov.di.ipv.cri.drivingpermit.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import testdata.DocumentCheckTestDataGenerator;
import testdata.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.common.library.domain.AuditEvent;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.VCISSDocumentCheckAuditExtension;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IssueCredentialAuditGeneratorTest {
    @Test
    void auditTest1() throws JsonProcessingException {

        DocumentCheckResultItem documentCheckResultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), DrivingPermitFormTestDataGenerator.generate());

        documentCheckResultItem.setStrengthScore(1);
        documentCheckResultItem.setValidityScore(1);
        documentCheckResultItem.setContraIndicators(List.of("u101"));
        documentCheckResultItem.setSessionId(UUID.randomUUID());
        documentCheckResultItem.setTransactionId("01");

        VCISSDocumentCheckAuditExtension ext =
                IssueCredentialDrivingPermitAuditExtensionUtil
                        .generateVCISSDocumentCheckAuditExtension(
                                "TestIssuer", List.of(documentCheckResultItem));

        AuditEventType evt1 = AuditEventType.VC_ISSUED;
        AuditEvent<VCISSDocumentCheckAuditExtension> ev1 =
                new AuditEvent<>(000001L, "PREFIX" + "_" + evt1.toString(), "TEST");

        // -Restricted +ext
        ev1.setExtensions(ext);

        ObjectWriter ow =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .writer()
                        .withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ev1);

        System.out.println(json);
        assertNotNull(json);
    }
}
