package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result;

import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;

public record DriverMatchServiceResult(Validity validity, String requestId) {
    public static DriverMatchServiceResultBuilder builder() {
        return new DriverMatchServiceResultBuilder();
    }

    public static class DriverMatchServiceResultBuilder {
        private Validity validity;
        private String requestId;

        DriverMatchServiceResultBuilder() {}

        public DriverMatchServiceResultBuilder validity(Validity validity) {
            this.validity = validity;
            return this;
        }

        public DriverMatchServiceResultBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public DriverMatchServiceResult build() {
            return new DriverMatchServiceResult(this.validity, this.requestId);
        }
    }
}
