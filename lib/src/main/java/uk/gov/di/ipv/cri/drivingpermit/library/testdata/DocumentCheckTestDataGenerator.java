package uk.gov.di.ipv.cri.drivingpermit.library.testdata;

import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DocumentCheckTestDataGenerator {

    public static DocumentCheckResultItem generateValidResultItem() {

        LocalDate expiryDate = LocalDate.now().plusYears(5);
        LocalDate issueDate = expiryDate.minusYears(10);

        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();

        documentCheckResultItem.setSessionId(UUID.randomUUID());
        documentCheckResultItem.setContraIndicators(List.of("CI1"));

        documentCheckResultItem.setStrengthScore(3);
        documentCheckResultItem.setValidityScore(2);
        documentCheckResultItem.setActivityHistoryScore(1);

        documentCheckResultItem.setActivityFrom(issueDate.toString());
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");

        documentCheckResultItem.setIssuedBy(IssuingAuthority.DVLA.toString());
        documentCheckResultItem.setDocumentNumber("L101");
        documentCheckResultItem.setIssueDate(issueDate.toString());
        documentCheckResultItem.setExpiryDate(expiryDate.toString());

        documentCheckResultItem.setTransactionId(UUID.randomUUID().toString());

        return documentCheckResultItem;
    }
}
