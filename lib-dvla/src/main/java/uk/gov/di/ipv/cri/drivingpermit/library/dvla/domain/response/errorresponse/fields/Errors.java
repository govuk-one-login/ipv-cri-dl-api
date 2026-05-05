package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.errorresponse.fields;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Errors(String status, String code, String detail) {
    @JsonCreator
    public Errors(
            @JsonProperty(value = "status", required = true) String status,
            @JsonProperty(value = "code", required = true) String code,
            @JsonProperty(value = "detail", required = true) String detail) {
        this.status = status;
        this.code = code;
        this.detail = detail;
    }

    public static ErrorsBuilder builder() {
        return new ErrorsBuilder();
    }

    public static class ErrorsBuilder {
        private String status;
        private String code;
        private String detail;

        ErrorsBuilder() {}

        public ErrorsBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ErrorsBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ErrorsBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Errors build() {
            return new Errors(this.status, this.code, this.detail);
        }
    }
}
