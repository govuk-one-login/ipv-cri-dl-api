package uk.gov.di.ipv.cri.drivingpermit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import testdata.DocumentCheckTestDataGenerator;
import testdata.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.helpers.PersonIdentityDetailedHelperMapper;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PersonIdentityDetailedHelperMapperTest {

    @Test
    void ShouldReturnAuditRestrictedFormatFromPassportFormData() {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        PersonIdentityDetailed testPersonIdentityDetailedFromFormData =
                PersonIdentityDetailedHelperMapper.drivingPermitFormDataToAuditRestrictedFormat(
                        drivingPermitForm);

        Name pidName = testPersonIdentityDetailedFromFormData.getNames().get(0);
        assertEquals(
                drivingPermitForm.getForenames().get(0), pidName.getNameParts().get(0).getValue());
        assertEquals(
                drivingPermitForm.getForenames().get(1), pidName.getNameParts().get(1).getValue());
        assertEquals(drivingPermitForm.getSurname(), pidName.getNameParts().get(2).getValue());
        assertEquals(
                drivingPermitForm.getDateOfBirth(),
                testPersonIdentityDetailedFromFormData.getBirthDates().get(0).getValue());

        // Driving Permit
        DrivingPermit drivingPermit =
                testPersonIdentityDetailedFromFormData.getDrivingPermits().get(0);
        assertEquals(
                drivingPermitForm.getDrivingLicenceNumber(), drivingPermit.getPersonalNumber());
        assertEquals(drivingPermitForm.getExpiryDate().toString(), drivingPermit.getExpiryDate());
        assertEquals(drivingPermitForm.getIssueDate().toString(), drivingPermit.getIssueDate());
        assertEquals(drivingPermitForm.getLicenceIssuer(), drivingPermit.getIssuedBy());
        assertEquals(drivingPermitForm.getIssueNumber(), drivingPermit.getIssueNumber());

        // 1 address
        Address pidAddress = testPersonIdentityDetailedFromFormData.getAddresses().get(0);

        assertEquals(pidAddress.getPostalCode(), pidAddress.getPostalCode());
        assertEquals("GB", pidAddress.getAddressCountry());
    }

    @Test
    void ShouldReturnAuditRestrictedFormatFromPersonIdentityDetailedAndDocumentCheckResultItem() {

        DrivingPermitForm onlyUsedToGenerateData = DrivingPermitFormTestDataGenerator.generate();

        PersonIdentityDetailed testPersonIdentityDetailedFromFormData =
                PersonIdentityDetailedHelperMapper.drivingPermitFormDataToAuditRestrictedFormat(
                        onlyUsedToGenerateData);

        DocumentCheckResultItem testDocumentCheckResultItem =
                DocumentCheckTestDataGenerator.generateValidResultItem(
                        UUID.randomUUID(), onlyUsedToGenerateData);

        // Called in IssueCredentialHandler where the original form data object is not available
        PersonIdentityDetailed auditPersonIdentityDetailed =
                PersonIdentityDetailedHelperMapper
                        .mapPersonIdentityDetailedAndDrivingPermitDataToAuditRestricted(
                                testPersonIdentityDetailedFromFormData,
                                testDocumentCheckResultItem);

        Name namePIDFormData = testPersonIdentityDetailedFromFormData.getNames().get(0);
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

        BirthDate dobPIDFormData = testPersonIdentityDetailedFromFormData.getBirthDates().get(0);
        BirthDate dobPIDAudit = auditPersonIdentityDetailed.getBirthDates().get(0);
        assertEquals(dobPIDFormData.getValue(), dobPIDAudit.getValue());

        // Driving Permit
        DrivingPermit dpPIDFormData =
                testPersonIdentityDetailedFromFormData.getDrivingPermits().get(0);
        DrivingPermit dpPIDAudit = auditPersonIdentityDetailed.getDrivingPermits().get(0);

        assertEquals(dpPIDFormData.getPersonalNumber(), dpPIDAudit.getPersonalNumber());
        assertEquals(dpPIDFormData.getExpiryDate(), dpPIDAudit.getExpiryDate());
        assertEquals(dpPIDFormData.getIssueDate(), dpPIDAudit.getIssueDate());
        assertEquals(dpPIDFormData.getIssuedBy(), dpPIDAudit.getIssuedBy());
        assertEquals(dpPIDFormData.getIssueNumber(), dpPIDAudit.getIssueNumber());

        // 1 address + postcode
        List<Address> addressPIDFormData = testPersonIdentityDetailedFromFormData.getAddresses();
        List<Address> addressPIDAudit = auditPersonIdentityDetailed.getAddresses();

        assertNotNull(addressPIDFormData);
        assertNotNull(addressPIDAudit);

        assertEquals(1, addressPIDFormData.size());
        assertEquals(1, addressPIDAudit.size());

        assertEquals(
                addressPIDFormData.get(0).getPostalCode(), addressPIDAudit.get(0).getPostalCode());

        // Both should be hardcoded
        assertEquals(
                GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY,
                addressPIDFormData.get(0).getAddressCountry());
        assertEquals(
                GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY,
                addressPIDAudit.get(0).getAddressCountry());
    }

    @Test
    void shouldMapNamesToCanonicalName() {

        String foreName = "Forename";
        String middleName = "Middlename";
        String surname = "Surname";

        Name name =
                PersonIdentityDetailedHelperMapper.mapNamesToCanonicalName(
                        List.of(foreName, middleName), surname);

        assertEquals(3, name.getNameParts().size());

        assertEquals(foreName, name.getNameParts().get(0).getValue());
        assertEquals("GivenName", name.getNameParts().get(0).getType());

        assertEquals(middleName, name.getNameParts().get(1).getValue());
        assertEquals("GivenName", name.getNameParts().get(1).getType());

        assertEquals(surname, name.getNameParts().get(2).getValue());
        assertEquals("FamilyName", name.getNameParts().get(2).getType());
    }

    @Test
    void shouldMapNullNameToEmptyCanonicalName() {
        Name name = PersonIdentityDetailedHelperMapper.mapNamesToCanonicalName(null, null);
        assertEquals(0, name.getNameParts().size());
    }

    @Test
    void shouldMapEmptyNameToEmptyCanonicalName() {
        Name name =
                PersonIdentityDetailedHelperMapper.mapNamesToCanonicalName(new ArrayList<>(), null);
        assertEquals(0, name.getNameParts().size());
    }
}
