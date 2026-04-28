package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.errorresponse.fields.Errors;

import java.util.List;

public record DriverMatchErrorResponse(List<Errors> errors) {
    @JsonCreator
    public DriverMatchErrorResponse(
            @JsonProperty(value = "errors", required = true) List<Errors> errors) {
        this.errors = errors;
    }

    public static DriverMatchErrorResponseBuilder builder() {
        return new DriverMatchErrorResponseBuilder();
    }

    public static class DriverMatchErrorResponseBuilder {
        private List<Errors> errors;

        DriverMatchErrorResponseBuilder() {}

        public DriverMatchErrorResponseBuilder errors(List<Errors> errors) {
            this.errors = errors;
            return this;
        }

        public DriverMatchErrorResponse build() {
            return new DriverMatchErrorResponse(this.errors);
        }
    }
}
