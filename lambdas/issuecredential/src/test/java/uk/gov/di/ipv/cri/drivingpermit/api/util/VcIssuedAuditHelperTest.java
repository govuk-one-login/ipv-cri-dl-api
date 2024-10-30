package uk.gov.di.ipv.cri.drivingpermit.api.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;
import uk.gov.di.ipv.cri.drivingpermit.testdata.DocumentCheckTestDataGenerator;
import uk.gov.di.ipv.cri.drivingpermit.util.PersonIdentityDetailedTestDataGenerator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY;

@ExtendWith(MockitoExtension.class)
class VcIssuedAuditHelperTest {

    @ParameterizedTest
    @CsvSource({"DVA", "DVLA"})
    void ShouldReturnAuditRestrictedFormatFromPersonIdentityDetailedAndDocumentCheckResultItem(
            String issuer) {

        PersonIdentityDetailed testPersonIdentityDetailed =
                PersonIdentityDetailedTestDataGenerator.generate(issuer);

        DocumentCheckResultItem testDocumentCheckResultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), testPersonIdentityDetailed);

        // Called in IssueCredentialHandler where the original form data object is not available
        PersonIdentityDetailed auditPersonIdentityDetailed =
                VcIssuedAuditHelper.mapPersonIdentityDetailedAndDrivingPermitDataToAuditRestricted(
                        testPersonIdentityDetailed, testDocumentCheckResultItem);

        Name namePIDFormData = testPersonIdentityDetailed.getNames().get(0);
        Name namePIDAudit = auditPersonIdentityDetailed.getNames().get(0);

        assertEquals(
                namePIDFormData.getNameParts().get(0).getValue(),
                namePIDAudit.getNameParts().get(0).getValue());
        assertEquals(
                namePIDFormData.getNameParts().get(1).getValue(),
                namePIDAudit.getNameParts().get(1).getValue());
        assertEquals(
                namePIDFormData.getNameParts().get(2).getValue(),
                namePIDAudit.getNameParts().get(2).getValue());

        BirthDate dobPIDFormData = testPersonIdentityDetailed.getBirthDates().get(0);
        BirthDate dobPIDAudit = auditPersonIdentityDetailed.getBirthDates().get(0);
        assertEquals(dobPIDFormData.getValue(), dobPIDAudit.getValue());

        // Driving Permit
        DrivingPermit dpPIDFormData = testPersonIdentityDetailed.getDrivingPermits().get(0);
        DrivingPermit dpPIDAudit = auditPersonIdentityDetailed.getDrivingPermits().get(0);

        assertEquals(dpPIDFormData.getPersonalNumber(), dpPIDAudit.getPersonalNumber());
        assertEquals(dpPIDFormData.getExpiryDate(), dpPIDAudit.getExpiryDate());
        assertEquals(dpPIDFormData.getIssueDate(), dpPIDAudit.getIssueDate());
        assertEquals(dpPIDFormData.getIssuedBy(), dpPIDAudit.getIssuedBy());

        if (IssuingAuthority.valueOf(dpPIDAudit.getIssuedBy()) == IssuingAuthority.DVLA) {
            assertEquals(dpPIDFormData.getIssueNumber(), dpPIDAudit.getIssueNumber());
        }

        // 1 address + postcode
        List<Address> addressPIDFormData = testPersonIdentityDetailed.getAddresses();
        List<Address> addressPIDAudit = auditPersonIdentityDetailed.getAddresses();

        assertNotNull(addressPIDFormData);
        assertNotNull(addressPIDAudit);

        assertEquals(1, addressPIDFormData.size());
        assertEquals(1, addressPIDAudit.size());

        assertEquals(
                addressPIDFormData.get(0).getPostalCode(), addressPIDAudit.get(0).getPostalCode());

        // Both should be hardcoded
        assertEquals(
                UK_DRIVING_PERMIT_ADDRESS_COUNTRY, addressPIDFormData.get(0).getAddressCountry());
        assertEquals(UK_DRIVING_PERMIT_ADDRESS_COUNTRY, addressPIDAudit.get(0).getAddressCountry());
    }
}
