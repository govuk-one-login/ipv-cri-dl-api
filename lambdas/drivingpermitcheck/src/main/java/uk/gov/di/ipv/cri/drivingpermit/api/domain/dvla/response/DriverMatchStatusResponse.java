package uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverMatchStatusResponse {
    private final String message;

    @JsonCreator
    public DriverMatchStatusResponse(
            @JsonProperty(value = "message", required = true) String message) {
        this.message = message;
    }
}
