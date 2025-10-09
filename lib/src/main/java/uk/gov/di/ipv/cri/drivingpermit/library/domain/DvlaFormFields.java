package uk.gov.di.ipv.cri.drivingpermit.library.domain;

import java.time.LocalDate;

public interface DvlaFormFields {
    String getDrivingLicenceNumber();

    String getSurname();

    String getIssueNumber();

    LocalDate getIssueDate();

    LocalDate getExpiryDate();
}
