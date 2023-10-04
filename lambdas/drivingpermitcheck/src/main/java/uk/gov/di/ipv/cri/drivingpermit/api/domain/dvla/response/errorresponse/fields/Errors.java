package uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.errorresponse.fields;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Errors {
    private final String status;
    private final String code;
    private final String detail;

    @JsonCreator
    public Errors(
            @JsonProperty(value = "status", required = true) String status,
            @JsonProperty(value = "code", required = true) String code,
            @JsonProperty(value = "detail", required = true) String detail) {
        this.status = status;
        this.code = code;
        this.detail = detail;
    }
}
