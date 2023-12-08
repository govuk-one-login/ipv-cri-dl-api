package uk.gov.di.ipv.cri.drivingpermit.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.util.DrivingPermitFormTestDataGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY;

@ExtendWith(MockitoExtension.class)
class RequestSentAuditHelperTest {

    @ParameterizedTest
    @CsvSource({
        "DVA", // IssuingAuthority
        "DVLA",
    })
    void ShouldReturnAuditRestrictedFormatFromDrivingPermitFormData(
            IssuingAuthority issuingAuthority) {

        DrivingPermitForm drivingPermitForm =
                DrivingPermitFormTestDataGenerator.generate(issuingAuthority);

        PersonIdentityDetailed testPersonIdentityDetailedFromFormData =
                RequestSentAuditHelper.drivingPermitFormDataToAuditRestrictedFormat(
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
        assertEquals(UK_DRIVING_PERMIT_ADDRESS_COUNTRY, pidAddress.getAddressCountry());
    }

    @Test
    void shouldMapNamesToCanonicalName() {

        String foreName = "Forename";
        String middleName = "Middlename";
        String surname = "Surname";

        Name name =
                RequestSentAuditHelper.mapNamesToCanonicalName(
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
        Name name = RequestSentAuditHelper.mapNamesToCanonicalName(null, null);
        assertEquals(0, name.getNameParts().size());
    }

    @Test
    void shouldMapEmptyNameToEmptyCanonicalName() {
        Name name = RequestSentAuditHelper.mapNamesToCanonicalName(new ArrayList<>(), null);
        assertEquals(0, name.getNameParts().size());
    }
}
