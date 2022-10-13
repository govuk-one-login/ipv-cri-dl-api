package uk.gov.di.ipv.cri.drivingpermit.api.testdata;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckVerificationResult;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

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

        testDocumentVerificationResult.setCheckDetails(
                DrivingPermitFormTestDataGenerator.deriveCheckDetails(data));
        testDocumentVerificationResult.setDrivingPermit(
                DrivingPermitFormTestDataGenerator.deriveDrivingPermit(data));

        return testDocumentVerificationResult;
    }
}
