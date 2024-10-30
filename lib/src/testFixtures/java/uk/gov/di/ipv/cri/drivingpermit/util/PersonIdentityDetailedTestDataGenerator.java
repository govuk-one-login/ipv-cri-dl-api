package uk.gov.di.ipv.cri.drivingpermit.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.NamePart;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityDetailedFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonIdentityDetailedTestDataGenerator {

    private PersonIdentityDetailedTestDataGenerator() {
        throw new IllegalStateException("Test Fixtures");
    }

    public static PersonIdentityDetailed generate(String issuer) {
        List<String> forenames = List.of("FirstName", "MiddleName");
        String surname = "Surname";

        BirthDate dateOfBirth = new BirthDate();
        dateOfBirth.setValue(LocalDate.now().minusYears(30));

        Address address = new Address();
        address.setPostalCode("Postcode");
        address.setAddressCountry(GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        DrivingPermit drivingPermit = new DrivingPermit();
        drivingPermit.setPersonalNumber("12345678");

        LocalDate licenceStart = dateOfBirth.getValue().plusYears(10);
        drivingPermit.setIssueDate(licenceStart.toString());
        drivingPermit.setExpiryDate(licenceStart.plusYears(10).toString());
        drivingPermit.setIssuedBy(issuer);
        drivingPermit.setIssueNumber("1");

        List<Name> names = List.of(mapNamesToCanonicalName(forenames, surname));
        List<BirthDate> birthDates = List.of(dateOfBirth);
        List<Address> addresses = List.of(address);
        List<DrivingPermit> drivingPermits = List.of(drivingPermit);

        return PersonIdentityDetailedFactory.createPersonIdentityDetailedWithDrivingPermit(
                names, birthDates, addresses, drivingPermits);
    }

    public static Name mapNamesToCanonicalName(List<String> forenames, String surname) {
        List<NamePart> nameParts = new ArrayList<>();

        if (Objects.nonNull(forenames) && !forenames.isEmpty()) {
            for (String name : forenames) {
                nameParts.add(setNamePart(name, "GivenName"));
            }
        }

        if (Objects.nonNull(surname)) {
            nameParts.add(setNamePart(surname, "FamilyName"));
        }

        Name name1 = new Name();
        name1.setNameParts(nameParts);
        return name1;
    }

    private static NamePart setNamePart(String value, String type) {
        NamePart namePart = new NamePart();
        namePart.setValue(value);
        namePart.setType(type);
        return namePart;
    }
}
