package uk.gov.di.ipv.cri.drivingpermit.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.NamePart;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityDetailedFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVLA;

public class RequestSentAuditHelper {

    public static PersonIdentityDetailed drivingPermitFormDataToAuditRestrictedFormat(
            DrivingPermitForm drivingPermitData) {

        Name name =
                mapNamesToCanonicalName(
                        drivingPermitData.getForenames(), drivingPermitData.getSurname());
        List<Name> names = List.of(name);

        BirthDate birthDate = new BirthDate();
        birthDate.setValue(drivingPermitData.getDateOfBirth());
        List<BirthDate> birthDates = List.of(birthDate);

        List<Address> addresses = drivingPermitData.getAddresses();

        DrivingPermit drivingPermit = new DrivingPermit();

        IssuingAuthority issuingAuthority =
                IssuingAuthority.valueOf(drivingPermitData.getLicenceIssuer());

        drivingPermit.setIssuedBy(drivingPermitData.getLicenceIssuer());
        drivingPermit.setPersonalNumber(drivingPermitData.getDrivingLicenceNumber());
        drivingPermit.setExpiryDate(drivingPermitData.getExpiryDate().toString());
        drivingPermit.setIssueDate(drivingPermitData.getIssueDate().toString());

        // DVLA only field(s)
        if (issuingAuthority == DVLA) {
            drivingPermit.setIssueNumber(drivingPermitData.getIssueNumber());
        }

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
