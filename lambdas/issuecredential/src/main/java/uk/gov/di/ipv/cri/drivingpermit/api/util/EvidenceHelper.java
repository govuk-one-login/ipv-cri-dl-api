package uk.gov.di.ipv.cri.drivingpermit.api.util;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.verifiablecredential.Evidence;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.verifiablecredential.EvidenceType;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.List;

public class EvidenceHelper {

    @ExcludeFromGeneratedCoverageReport
    private EvidenceHelper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static Evidence documentCheckResultItemToEvidence(
            DocumentCheckResultItem documentCheckResultItem) {
        Evidence evidence = new Evidence();
        evidence.setType(EvidenceType.IDENTITY_CHECK.toString());
        evidence.setTxn(documentCheckResultItem.getTransactionId());

        evidence.setActivityHistoryScore(documentCheckResultItem.getActivityHistoryScore());
        evidence.setStrengthScore(documentCheckResultItem.getStrengthScore());
        evidence.setValidityScore(documentCheckResultItem.getValidityScore());

        CheckDetails checkDetails = new CheckDetails();
        checkDetails.setCheckMethod(documentCheckResultItem.getCheckMethod());
        checkDetails.setIdentityCheckPolicy(documentCheckResultItem.getIdentityCheckPolicy());
        checkDetails.setActivityFrom(documentCheckResultItem.getActivityFrom());

        evidence.setCi(documentCheckResultItem.getContraIndicators());

        if (null == evidence.getCi()) {
            evidence.setCheckDetails(List.of(checkDetails));
        } else {
            evidence.setFailedCheckDetails(List.of(checkDetails));
        }

        return evidence;
    }
}
