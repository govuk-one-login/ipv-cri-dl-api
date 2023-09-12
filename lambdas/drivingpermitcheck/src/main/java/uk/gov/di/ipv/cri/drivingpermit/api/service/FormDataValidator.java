package uk.gov.di.ipv.cri.drivingpermit.api.service;

import uk.gov.di.ipv.cri.drivingpermit.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.drivingpermit.api.util.JsonValidationUtility;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FormDataValidator {
    public static final int MIN_SUPPORTED_ADDRESSES = 1;
    public static final int MAX_SUPPORTED_ADDRESSES = 1; // One address with just postcode
    private static final int POSTCODE_STRING_MAX_LEN = 8;

    private static final int NAME_STRING_MAX_LEN = 30;

    ValidationResult<List<String>> validate(DrivingPermitForm drivingPermitForm) {
        List<String> validationErrors = new ArrayList<>();

        List<String> foreNames = drivingPermitForm.getForenames();
        if (JsonValidationUtility.validateListDataEmptyIsFail(
                foreNames, "Forenames", validationErrors)) {
            for (String name : drivingPermitForm.getForenames()) {
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        name, NAME_STRING_MAX_LEN, "Forename", validationErrors);
            }
        }

        JsonValidationUtility.validateStringDataEmptyIsFail(
                drivingPermitForm.getSurname(), NAME_STRING_MAX_LEN, "Surname", validationErrors);

        if (Objects.isNull(drivingPermitForm.getDateOfBirth())) {
            validationErrors.add(
                    "DateOfBirth" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX);
        }

        if (Objects.isNull(drivingPermitForm.getAddresses())) {
            validationErrors.add("Addresses" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX);
        } else if (JsonValidationUtility.validateIntegerRangeData(
                drivingPermitForm.getAddresses().size(),
                MIN_SUPPORTED_ADDRESSES,
                MAX_SUPPORTED_ADDRESSES,
                "Addresses",
                validationErrors)) {

            JsonValidationUtility.validateStringDataEmptyIsFail(
                    drivingPermitForm.getAddresses().get(0).getPostalCode(),
                    POSTCODE_STRING_MAX_LEN,
                    "Postcode",
                    validationErrors);
        }

        // this implementation needs completing to validate all necessary fields
        return new ValidationResult<>(validationErrors.isEmpty(), validationErrors);
    }
}
