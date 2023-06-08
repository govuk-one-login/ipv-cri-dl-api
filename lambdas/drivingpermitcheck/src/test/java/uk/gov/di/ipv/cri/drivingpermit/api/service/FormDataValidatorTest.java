package uk.gov.di.ipv.cri.drivingpermit.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.util.JsonValidationUtility;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormDataValidatorTest {

    private static final Logger LOGGER = LogManager.getLogger();

    @Test
    void testFormDataValidatorNamesCannotBeNull() {

        final String TEST_STRING = null;

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setForenames(null);
        drivingPermitForm.setSurname(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR_0 =
                "Forenames" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;
        final String EXPECTED_ERROR_1 =
                "Surname" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertNull(drivingPermitForm.getForenames());
        assertNull(drivingPermitForm.getSurname());
        assertEquals(2, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR_0, validationResult.getError().get(0));
        assertEquals(EXPECTED_ERROR_1, validationResult.getError().get(1));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorDOBCannotBeNull() {

        final LocalDate TEST_LOCAL_DATE = null;

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setDateOfBirth(TEST_LOCAL_DATE);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR =
                "DateOfBirth" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_LOCAL_DATE, drivingPermitForm.getDateOfBirth());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesCannotBeNull() {

        final List<Address> TEST_ADDRESSES = null;

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR =
                "Addresses" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesCannotBeEmpty() {

        final List<Address> TEST_ADDRESSES = new ArrayList<>();

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String TEST_INTEGER_NAME = "Addresses";
        final int TEST_VALUE = TEST_ADDRESSES.size();

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        FormDataValidator.MIN_SUPPORTED_ADDRESSES,
                        FormDataValidator.MAX_SUPPORTED_ADDRESSES,
                        TEST_INTEGER_NAME);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorTooManyAddressesFail() {

        final int addressChainLength = FormDataValidator.MAX_SUPPORTED_ADDRESSES;
        final int additionalCurrentAddresses = 1;
        final int additionalPreviousAddresses = 0;
        final int TOTAL_ADDRESSES = addressChainLength + additionalCurrentAddresses;
        final boolean shuffleAddresses = true;

        DrivingPermitForm drivingPermitForm =
                DrivingPermitFormTestDataGenerator.generateWithMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        FormDataValidator formDataValidator = new FormDataValidator();

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TOTAL_ADDRESSES,
                        FormDataValidator.MIN_SUPPORTED_ADDRESSES,
                        FormDataValidator.MAX_SUPPORTED_ADDRESSES,
                        "Addresses");

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TOTAL_ADDRESSES, drivingPermitForm.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesValidCurrentAddressIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesCurrentAddressNullDatesIsOk() {
        // Edge case scenario : A current address where user does not know when exactly they moved
        // in (ValidFrom null).

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(null);
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesCurrentAddressWithFutureDatesIsFail() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now().plusYears(1));
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR = FormDataValidator.createAddressCheckErrorMessage(1, 0, 0, 1);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES.size(), drivingPermitForm.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesValidCurrentAndPreviousAddressIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.of(1999, 12, 31));
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidFrom(null);
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS, TEST_PREVIOUS_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesAValidCurrentAndPreviousAddressAreInReverseOrderIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now());

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom().minusDays(1));

        final List<Address> TEST_ADDRESSES = List.of(TEST_PREVIOUS_ADDRESS, TEST_CURRENT_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesValidCurrentAndPreviousAddressOverlapIsOk() {
        // Edge case scenario : A current address where user has moved out of the previous on the
        // current date
        // (CURRENT ValidFrom == PREVIOUS ValidUntil).

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now().atTime(0, 0).toLocalDate());

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS, TEST_PREVIOUS_ADDRESS);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesMultipleCurrentAddressesIsOk() {

        final Address TEST_CURRENT_ADDRESS_1 = new Address();
        TEST_CURRENT_ADDRESS_1.setValidFrom(LocalDate.now());
        TEST_CURRENT_ADDRESS_1.setValidUntil(null);

        final Address TEST_CURRENT_ADDRESS_2 = new Address();
        TEST_CURRENT_ADDRESS_2.setValidFrom(LocalDate.now());
        TEST_CURRENT_ADDRESS_2.setValidUntil(null);

        final List<Address> TEST_ADDRESSES =
                List.of(TEST_CURRENT_ADDRESS_1, TEST_CURRENT_ADDRESS_2);

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesManyPreviousAddressesFail() {

        final int addressChainLength = 0;
        final int additionalCurrentAddresses = 0;
        final int additionalPreviousAddresses = 2;
        final int TOTAL_ADDRESSES =
                addressChainLength + additionalCurrentAddresses + additionalPreviousAddresses;
        final boolean shuffleAddresses = true;

        DrivingPermitForm form =
                DrivingPermitFormTestDataGenerator.generateWithMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        FormDataValidator formDataValidator = new FormDataValidator();

        ValidationResult<List<String>> validationResult = formDataValidator.validate(form);

        final String EXPECTED_ERROR =
                FormDataValidator.createAddressCheckErrorMessage(
                        TOTAL_ADDRESSES,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        0);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TOTAL_ADDRESSES, form.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorAddressesManyValidCurrentAndValidPreviousAddressesOutOfOrderIsOK() {

        final int addressChainLength = 10;
        final int additionalCurrentAddresses = 5;
        final int additionalPreviousAddresses = 5;
        final int TOTAL_ADDRESSES =
                addressChainLength + additionalCurrentAddresses + additionalPreviousAddresses;
        final boolean shuffleAddresses = true;

        DrivingPermitForm form =
                DrivingPermitFormTestDataGenerator.generateWithMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        FormDataValidator formDataValidator = new FormDataValidator();

        ValidationResult<List<String>> validationResult = formDataValidator.validate(form);

        assertEquals(TOTAL_ADDRESSES, form.getAddresses().size());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }
}
