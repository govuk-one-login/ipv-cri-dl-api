package uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverMatchAPIResponse {
    private final boolean validDocument;

    @JsonCreator
    public DriverMatchAPIResponse(
            @JsonProperty(value = "validDocument", required = true) boolean validDocument) {
        this.validDocument = validDocument;
    }
}
