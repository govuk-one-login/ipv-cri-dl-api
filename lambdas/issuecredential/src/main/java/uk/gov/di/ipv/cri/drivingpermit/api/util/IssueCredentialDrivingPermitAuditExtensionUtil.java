package uk.gov.di.ipv.cri.drivingpermit.api.util;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.Evidence;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.EvidenceType;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.audit.VCISSDocumentCheckAuditExtension;
import uk.gov.di.ipv.cri.drivingpermit.api.persistence.item.DocumentCheckResultItem;

import java.util.ArrayList;
import java.util.List;

public class IssueCredentialDrivingPermitAuditExtensionUtil {

    private IssueCredentialDrivingPermitAuditExtensionUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static VCISSDocumentCheckAuditExtension generateVCISSDocumentCheckAuditExtension(
            String vcIssuer, List<DocumentCheckResultItem> documentCheckResultItems) {

        List<Evidence> evidenceList = new ArrayList<>();

        for (DocumentCheckResultItem documentCheckResultItem : documentCheckResultItems) {

            Evidence evidence = new Evidence();

            evidence.setType(EvidenceType.IDENTITY_CHECK.toString());
            evidence.setTxn(documentCheckResultItem.getTransactionId());
            evidence.setValidityScore(documentCheckResultItem.getValidityScore());
            evidence.setStrengthScore(documentCheckResultItem.getStrengthScore());

            evidence.setCi(documentCheckResultItem.getContraIndicators());

            evidenceList.add(evidence);
        }

        return new VCISSDocumentCheckAuditExtension(vcIssuer, evidenceList);
    }
}
