package testdata;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY;

public class DrivingPermitFormTestDataGenerator {

    public static final String TEST_DRIVING_LICENCE_NUMBER = "A001";

    private DrivingPermitFormTestDataGenerator() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static DrivingPermitForm generate() {
        return generate(IssuingAuthority.DVLA);
    }

    public static DrivingPermitForm generateDva() {
        return generate(IssuingAuthority.DVA);
    }

    public static DrivingPermitForm generate(IssuingAuthority issuingAuthority) {
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();

        drivingPermitForm.setLicenceIssuer(issuingAuthority.toString());

        drivingPermitForm.setForenames(List.of("FirstName", "MiddleName"));
        drivingPermitForm.setSurname("Surname");

        drivingPermitForm.setDateOfBirth(LocalDate.of(1976, 12, 26));

        LocalDate licenceStart = drivingPermitForm.getDateOfBirth().plusYears(20);

        drivingPermitForm.setIssueDate(licenceStart);
        drivingPermitForm.setExpiryDate(licenceStart.plusYears(10));
        drivingPermitForm.setDrivingLicenceNumber(TEST_DRIVING_LICENCE_NUMBER);

        Address address = new Address();
        address.setPostalCode("Postcode");
        address.setAddressCountry(UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        drivingPermitForm.setAddresses(List.of(address));

        return drivingPermitForm;
    }
}
