package uk.gov.di.ipv.cri.drivingpermit.api.util;

import lombok.experimental.UtilityClass;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.DrivingPermit;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityDetailedFactory;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.persistence.item.DocumentCheckResultItem;

import java.util.List;

import static uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority.DVLA;

@UtilityClass
public class VcIssuedAuditHelper {

    public static PersonIdentityDetailed
            mapPersonIdentityDetailedAndDrivingPermitDataToAuditRestricted(
                    PersonIdentityDetailed personIdentityDetailed,
                    DocumentCheckResultItem documentCheckResultItem) {

        List<Name> names = personIdentityDetailed.getNames();
        List<BirthDate> birthDates = personIdentityDetailed.getBirthDates();
        List<Address> addresses = personIdentityDetailed.getAddresses();

        DrivingPermit drivingPermit = new DrivingPermit();

        IssuingAuthority issuingAuthority =
                IssuingAuthority.valueOf(documentCheckResultItem.getIssuedBy());

        drivingPermit.setPersonalNumber(documentCheckResultItem.getDocumentNumber());
        drivingPermit.setExpiryDate(documentCheckResultItem.getExpiryDate());
        drivingPermit.setIssuedBy(documentCheckResultItem.getIssuedBy());
        drivingPermit.setIssueDate(documentCheckResultItem.getIssueDate());

        // DVLA only field(s)
        if (issuingAuthority == DVLA) {
            drivingPermit.setIssueNumber(documentCheckResultItem.getIssueNumber());
        }

        List<DrivingPermit> drivingPermits = List.of(drivingPermit);

        return PersonIdentityDetailedFactory.createPersonIdentityDetailedWithDrivingPermit(
                names, birthDates, addresses, drivingPermits);
    }
}
