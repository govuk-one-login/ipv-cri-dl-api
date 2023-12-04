package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.errorresponse.fields.Errors;

import java.util.List;

@Data
@Builder
public class DriverMatchErrorResponse {
    private final List<Errors> errors;

    @JsonCreator
    public DriverMatchErrorResponse(
            @JsonProperty(value = "errors", required = true) List<Errors> errors) {
        this.errors = errors;
    }
}
