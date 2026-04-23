package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DvlaPayload(
        @JsonProperty String drivingLicenceNumber,
        @JsonProperty String lastName,
        @JsonProperty String issueNumber,
        @JsonProperty String validFrom,
        @JsonProperty String validTo) {
    public static DvlaPayloadBuilder builder() {
        return new DvlaPayloadBuilder();
    }

    public static class DvlaPayloadBuilder {
        private String drivingLicenceNumber;
        private String lastName;
        private String issueNumber;
        private String validFrom;
        private String validTo;

        DvlaPayloadBuilder() {}

        public DvlaPayloadBuilder drivingLicenceNumber(String drivingLicenceNumber) {
            this.drivingLicenceNumber = drivingLicenceNumber;
            return this;
        }

        public DvlaPayloadBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public DvlaPayloadBuilder issueNumber(String issueNumber) {
            this.issueNumber = issueNumber;
            return this;
        }

        public DvlaPayloadBuilder validFrom(String validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public DvlaPayloadBuilder validTo(String validTo) {
            this.validTo = validTo;
            return this;
        }

        public DvlaPayload build() {
            return new DvlaPayload(
                    this.drivingLicenceNumber,
                    this.lastName,
                    this.issueNumber,
                    this.validFrom,
                    this.validTo);
        }
    }
}
