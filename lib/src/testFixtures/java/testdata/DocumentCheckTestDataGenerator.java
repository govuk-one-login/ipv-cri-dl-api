package testdata;

import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.List;
import java.util.UUID;

public class DocumentCheckTestDataGenerator {

    private DocumentCheckTestDataGenerator() {}

    public static DocumentCheckResultItem generateValidResultItem(
            UUID sessionId, DrivingPermitForm drivingPermitForm) {

        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();

        documentCheckResultItem.setSessionId(sessionId);
        documentCheckResultItem.setContraIndicators(List.of("CI1"));

        documentCheckResultItem.setStrengthScore(3);
        documentCheckResultItem.setValidityScore(2);
        documentCheckResultItem.setActivityHistoryScore(1);

        documentCheckResultItem.setActivityFrom(drivingPermitForm.getIssueDate().toString());
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");

        documentCheckResultItem.setIssuedBy(drivingPermitForm.getLicenceIssuer());
        documentCheckResultItem.setDocumentNumber(drivingPermitForm.getDrivingLicenceNumber());
        documentCheckResultItem.setIssueDate(drivingPermitForm.getIssueDate().toString());
        documentCheckResultItem.setExpiryDate(drivingPermitForm.getExpiryDate().toString());

        documentCheckResultItem.setTransactionId(UUID.randomUUID().toString());

        return documentCheckResultItem;
    }
}
