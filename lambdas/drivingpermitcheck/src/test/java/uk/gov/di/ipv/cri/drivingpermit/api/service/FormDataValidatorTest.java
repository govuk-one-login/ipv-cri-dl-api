package uk.gov.di.ipv.cri.drivingpermit.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import testdata.DrivingPermitFormTestDataGenerator;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.util.JsonValidationUtility;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void testFormDataValidatorPostcodeCannotBeNull() {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        final List<Address> TEST_ADDRESSES = drivingPermitForm.getAddresses();
        TEST_ADDRESSES.get(0).setPostalCode(null);

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String TEST_INTEGER_NAME = "Addresses";
        final int TEST_VALUE = TEST_ADDRESSES.size();

        final String EXPECTED_ERROR =
                "Postcode" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorPostcodeCannotBeEmpty() {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        final List<Address> TEST_ADDRESSES = drivingPermitForm.getAddresses();
        TEST_ADDRESSES.get(0).setPostalCode("");

        drivingPermitForm.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String TEST_INTEGER_NAME = "Addresses";
        final int TEST_VALUE = TEST_ADDRESSES.size();

        final String EXPECTED_ERROR =
                "Postcode" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, drivingPermitForm.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testFormDataValidatorIssuerMustBeValid() {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        FormDataValidator formDataValidator = new FormDataValidator();

        String invalidIssuer = "ABCD";

        drivingPermitForm.setLicenceIssuer(invalidIssuer);

        ValidationResult<List<String>> validationResult =
                formDataValidator.validate(drivingPermitForm);

        final String EXPECTED_ERROR = String.format("LicenceIssuer %s is Unknown", invalidIssuer);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }
}
