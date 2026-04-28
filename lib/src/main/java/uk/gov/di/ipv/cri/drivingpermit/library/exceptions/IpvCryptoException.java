package uk.gov.di.ipv.cri.drivingpermit.library.exceptions;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.io.Serial;

public class IpvCryptoException extends RuntimeException {

    @Serial private static final long serialVersionUID = 727295448492386661L;

    @ExcludeFromGeneratedCoverageReport
    public IpvCryptoException(String message) {
        super(message);
    }
}
