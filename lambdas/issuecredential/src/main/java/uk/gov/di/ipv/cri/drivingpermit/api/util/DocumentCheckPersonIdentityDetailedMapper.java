package uk.gov.di.ipv.cri.drivingpermit.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.*;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentCheckPersonIdentityDetailedMapper {

    private DocumentCheckPersonIdentityDetailedMapper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static PersonIdentityDetailed generatePersonIdentityDetailed(
            DrivingPermitForm drivingPermitData) {

        Name name1 =
                mapNamesToCanonicalName(
                        drivingPermitData.getForenames(), drivingPermitData.getSurname());

        BirthDate birthDate = new BirthDate();
        birthDate.setValue(drivingPermitData.getDateOfBirth());

        return new PersonIdentityDetailed(
                List.of(name1),
                List.of(birthDate),
                drivingPermitData.getAddresses(),
                List.of(mapDrivingPermit(drivingPermitData)));
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

    private static DrivingPermit mapDrivingPermit(DrivingPermitForm drivingPermitData) {
        DrivingPermit drivingPermit = new DrivingPermit();

        drivingPermit.setPersonalNumber(drivingPermitData.getDrivingLicenceNumber());
        drivingPermit.setIssueNumber(drivingPermitData.getIssueNumber());
        drivingPermit.setIssueDate(drivingPermitData.getIssueDate());
        drivingPermit.setExpiryDate(drivingPermitData.getExpiryDate());
        drivingPermit.setIssuedBy(drivingPermitData.getLicenceIssuer());

        return drivingPermit;
    }
}
