package uk.gov.di.ipv.cri.drivingpermit.library.testdata;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.CheckDetails;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermit;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DrivingPermitFormTestDataGenerator {

    public static final String TEST_DRIVING_LICENCE_NUMBER = "A001";

    public static DrivingPermitForm generate() {
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();

        return generate(IssuingAuthority.DVLA);
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
        address.setAddressCountry("GB");

        drivingPermitForm.setAddresses(List.of(address));

        return drivingPermitForm;
    }

    public static DrivingPermitForm generateWithMultipleAddresses(
            int addressChainLength,
            int additionalCurrentAddresses,
            int additionalPreviousAddresses,
            boolean addressShuffle) {
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();

        drivingPermitForm.setForenames(List.of("FirstName", "MiddleName"));
        drivingPermitForm.setSurname("Surname");

        drivingPermitForm.setDateOfBirth(LocalDate.of(1976, 12, 26));

        LocalDate licenceStart = LocalDate.now().minusYears(1);

        drivingPermitForm.setExpiryDate(licenceStart.plusYears(10));
        drivingPermitForm.setDrivingLicenceNumber(TEST_DRIVING_LICENCE_NUMBER);

        List<Address> addresses = new ArrayList<>();
        IntStream.range(0, addressChainLength).forEach(a -> addresses.add(createAddress(a)));

        while (additionalCurrentAddresses > 0) {

            Address additionalCurrentAddress = new Address();
            additionalCurrentAddress.setValidUntil(null);
            additionalCurrentAddress.setValidFrom(
                    LocalDate.now().minusYears(additionalCurrentAddresses));

            addresses.add(additionalCurrentAddress);

            additionalCurrentAddresses--;
        }

        while (additionalPreviousAddresses > 0) {

            Address additionalPreviousAddress = new Address();
            additionalPreviousAddress.setValidUntil(
                    LocalDate.now().minusYears(additionalPreviousAddresses));
            additionalPreviousAddress.setValidFrom(null);

            addresses.add(additionalPreviousAddress);

            additionalPreviousAddresses--;
        }

        // Randomise list order
        if (addressShuffle) {
            Collections.shuffle(addresses);
        }

        drivingPermitForm.setAddresses(addresses);

        return drivingPermitForm;
    }

    private static Address createAddress(int id) {

        Address address = new Address();

        final int yearsBetweenAddresses = 2;

        int startYear = id + (id + yearsBetweenAddresses);
        int endYear = id + id;

        address.setValidFrom(LocalDate.now().minusYears(startYear));

        address.setValidUntil(
                (id == 0 ? null : LocalDate.now().minusYears(endYear).minusMonths(1)));

        address.setBuildingNumber(String.valueOf(ThreadLocalRandom.current().nextInt()));
        address.setPostalCode("Postcode" + id);
        address.setStreetName("Street Name" + id);
        address.setAddressLocality("PostTown" + id);

        return address;
    }

    public static DrivingPermit deriveDrivingPermit(DrivingPermitForm data) {
        DrivingPermit drivingPermit = new DrivingPermit();
        drivingPermit.setDocumentNumber(data.getDrivingLicenceNumber());
        drivingPermit.setExpiryDate(data.getExpiryDate().toString());
        drivingPermit.setIssuedBy(IssuingAuthority.DVLA.toString());

        return drivingPermit;
    }

    public static CheckDetails deriveCheckDetails(DrivingPermitForm data) {
        CheckDetails checkDetails = new CheckDetails();

        checkDetails.setCheckMethod("data");
        checkDetails.setActivityFrom(data.getExpiryDate().minusYears(10).toString());
        checkDetails.setIdentityCheckPolicy("published");

        return checkDetails;
    }
}
