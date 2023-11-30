package uk.gov.di.ipv.cri.drivingpermit.testdata;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.List;
import java.util.UUID;

public class DocumentCheckTestDataGenerator {

    private DocumentCheckTestDataGenerator() {
        throw new IllegalStateException("Test Fixtures");
    }

    public static DocumentCheckResultItem generateValidResultItem(
            UUID sessionId, PersonIdentityDetailed personIdentityDetailed) {

        DocumentCheckResultItem documentCheckResultItem = new DocumentCheckResultItem();

        documentCheckResultItem.setSessionId(sessionId);
        documentCheckResultItem.setContraIndicators(List.of("CI1"));

        documentCheckResultItem.setStrengthScore(3);
        documentCheckResultItem.setValidityScore(2);
        documentCheckResultItem.setActivityHistoryScore(1);

        DrivingPermit drivingPermit = personIdentityDetailed.getDrivingPermits().get(0);

        documentCheckResultItem.setActivityFrom(drivingPermit.getIssueDate());
        documentCheckResultItem.setCheckMethod("data");
        documentCheckResultItem.setIdentityCheckPolicy("published");

        documentCheckResultItem.setIssuedBy(drivingPermit.getIssuedBy());
        documentCheckResultItem.setIssueNumber(drivingPermit.getIssueNumber());
        documentCheckResultItem.setDocumentNumber(drivingPermit.getPersonalNumber());
        documentCheckResultItem.setIssueDate(drivingPermit.getIssueDate());
        documentCheckResultItem.setExpiryDate(drivingPermit.getExpiryDate());

        documentCheckResultItem.setTransactionId(UUID.randomUUID().toString());

        return documentCheckResultItem;
    }
}
