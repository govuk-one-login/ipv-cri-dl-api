package uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class DvlaPayload {
    @JsonProperty private String drivingLicenceNumber;
    @JsonProperty private String lastName;
    @JsonProperty private String issueNumber;
    @JsonProperty private String validFrom;
    @JsonProperty private String validTo;

    public DvlaPayload(
            String drivingLicenceNumber,
            String lastName,
            String issueNumber,
            String validFrom,
            String validTo) {
        this.drivingLicenceNumber = drivingLicenceNumber;
        this.lastName = lastName;
        this.issueNumber = issueNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }
}
