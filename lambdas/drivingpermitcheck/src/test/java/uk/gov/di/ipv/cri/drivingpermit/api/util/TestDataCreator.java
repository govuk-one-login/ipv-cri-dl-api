package uk.gov.di.ipv.cri.drivingpermit.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;

public class TestDataCreator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        Address address = new Address();
        address.setValidFrom(LocalDate.now().minusYears(3));
        if (addressType.equals(AddressType.PREVIOUS)) {
            address.setValidUntil(LocalDate.now().minusMonths(1));
        }

        address.setBuildingNumber("101");
        address.setStreetName("Street Name");
        address.setAddressLocality("PostTown");
        address.setPostalCode("Postcode");
        address.setAddressCountry("GB");

        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static DrivingPermitForm createTestDrivingPermitForm(AddressType addressType) {
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();

        drivingPermitForm.setForenames(List.of("FirstName", "MiddleName"));
        drivingPermitForm.setSurname("Surname");

        drivingPermitForm.setDateOfBirth(LocalDate.of(1976, 12, 26));
        Address address = new Address();
        address.setValidFrom(LocalDate.now().minusYears(3));
        if (addressType.equals(AddressType.PREVIOUS)) {
            address.setValidUntil(LocalDate.now().minusMonths(1));
        }

        address.setBuildingNumber("101");
        address.setStreetName("Street Name");
        address.setAddressLocality("PostTown");
        address.setPostalCode("Postcode");
        address.setAddressCountry("GB");

        drivingPermitForm.setAddresses(List.of(address));
        return drivingPermitForm;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(CURRENT);
    }

    public static DrivingPermitForm createTestDrivingPermitForm() {
        return createTestDrivingPermitForm(CURRENT);
    }

    public static PersonIdentity createTestPersonIdentityMultipleAddresses(
            int addressChainLength,
            int additionalCurrentAddresses,
            int additionalPreviousAddresses,
            boolean addressShuffle) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));

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

        personIdentity.setAddresses(addresses);

        return personIdentity;
    }

    public static DrivingPermitForm createTestDrivingPermitMultipleAddresses(
            int addressChainLength,
            int additionalCurrentAddresses,
            int additionalPreviousAddresses,
            boolean addressShuffle) {
        DrivingPermitForm drivingPermitForm = new DrivingPermitForm();

        drivingPermitForm.setForenames(List.of("FirstName", "MiddleName"));
        drivingPermitForm.setSurname("Surname");

        drivingPermitForm.setDateOfBirth(LocalDate.of(1976, 12, 26));

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

    public static DcsResponse createSuccessDcsResponse() {
        DcsResponse dcsResponse = new DcsResponse();
        dcsResponse.setCorrelationId("1234");
        dcsResponse.setRequestId("4321");
        dcsResponse.setValid(true);
        return dcsResponse;
    }

    private static Address createAddress(int id) {

        Address address = new Address();

        final int yearsBetweenAddresses = 2;

        int startYear = id + (id + yearsBetweenAddresses);
        int endYear = id + id;

        address.setValidFrom(LocalDate.now().minusYears(startYear));

        address.setValidUntil(
                (id == 0 ? null : LocalDate.now().minusYears(endYear).minusMonths(1)));

        address.setPostalCode("Postcode" + id);
        address.setStreetName("Street Name" + id);
        address.setAddressLocality("PostTown" + id);

        LOGGER.info(
                "createAddress "
                        + id
                        + " "
                        + address.getAddressType()
                        + " from "
                        + address.getValidFrom()
                        + " until "
                        + address.getValidUntil());

        return address;
    }
}
