package uk.gov.di.ipv.cri.drivingpermit.api.exception;

import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;

import java.io.Serial;

public class CredentialRequestException extends Exception {
    @Serial private static final long serialVersionUID = 42346730807325427L;

    public CredentialRequestException(ErrorResponse invalidRequestParam) {
        super(invalidRequestParam.getMessage());
    }
}
