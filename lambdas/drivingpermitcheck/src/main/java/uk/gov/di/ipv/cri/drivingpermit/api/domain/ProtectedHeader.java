package uk.gov.di.ipv.cri.drivingpermit.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ProtectedHeader {

    @JsonProperty("alg")
    private final String algorithm;

    @JsonProperty("x5t")
    private final String sha1Thumbprint;

    @JsonProperty("x5t#S256")
    private final String sha256Thumbprint;

    @ExcludeFromGeneratedCoverageReport
    public ProtectedHeader(String algorithm, String sha1Thumbprint, String sha256Thumbprint) {
        this.algorithm = algorithm;
        this.sha1Thumbprint = sha1Thumbprint;
        this.sha256Thumbprint = sha256Thumbprint;
    }
}
