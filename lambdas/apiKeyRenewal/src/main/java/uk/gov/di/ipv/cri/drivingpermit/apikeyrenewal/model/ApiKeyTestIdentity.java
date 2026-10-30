package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.time.LocalDate;
import java.util.List;

public final class ApiKeyTestIdentity {

    private ApiKeyTestIdentity() {}

    public static DrivingPermitForm drivingPermitForm() {
        DrivingPermitForm drivingPermit = new DrivingPermitForm();
        drivingPermit.setLicenceIssuer("DVLA");

        drivingPermit.setForenames(List.of("KENNETH"));
        drivingPermit.setSurname("DECERQUEIRA");

        drivingPermit.setDateOfBirth(LocalDate.of(1965, 7, 8));

        drivingPermit.setIssueDate(LocalDate.of(2018, 4, 19));
        drivingPermit.setExpiryDate(LocalDate.of(2042, 10, 1));
        drivingPermit.setDrivingLicenceNumber("DECER607085KE9LN");
        drivingPermit.setIssueNumber("16");

        Address address = new Address();
        address.setPostalCode("BA2 5AA");
        address.setAddressCountry(GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        drivingPermit.setAddresses(List.of(address));

        return drivingPermit;
    }
}
