package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DriverMatchAPIResponse(boolean validDocument) {
    @JsonCreator
    public DriverMatchAPIResponse(
            @JsonProperty(value = "validDocument", required = true) boolean validDocument) {
        this.validDocument = validDocument;
    }

    public static DriverMatchAPIResponseBuilder builder() {
        return new DriverMatchAPIResponseBuilder();
    }

    public static class DriverMatchAPIResponseBuilder {
        private boolean validDocument;

        DriverMatchAPIResponseBuilder() {}

        public DriverMatchAPIResponseBuilder validDocument(boolean validDocument) {
            this.validDocument = validDocument;
            return this;
        }

        public DriverMatchAPIResponse build() {
            return new DriverMatchAPIResponse(this.validDocument);
        }
    }
}
