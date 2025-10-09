package uk.gov.di.ipv.cri.drivingpermit.api.testdata;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.util.List;

public class DocumentCheckVerificationResultDataGenerator {
    public static DocumentCheckVerificationResult generate(DrivingPermitForm data) {
        DocumentCheckVerificationResult testDocumentVerificationResult =
                new DocumentCheckVerificationResult();
        testDocumentVerificationResult.setExecutedSuccessfully(true);
        testDocumentVerificationResult.setVerified(true);
        testDocumentVerificationResult.setContraIndicators(List.of("A01"));
        testDocumentVerificationResult.setStrengthScore(1);
        testDocumentVerificationResult.setValidityScore(1);

        CheckDetails checkDetails = new CheckDetails();

        checkDetails.setCheckMethod("data");
        checkDetails.setActivityFrom(data.getExpiryDate().minusYears(10).toString());
        checkDetails.setIdentityCheckPolicy("published");

        testDocumentVerificationResult.setCheckDetails(checkDetails);

        return testDocumentVerificationResult;
    }
}
