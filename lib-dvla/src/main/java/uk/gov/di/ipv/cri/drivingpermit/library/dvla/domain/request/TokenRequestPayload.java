package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRequestPayload(
        @JsonProperty(value = "userName", required = true) String userName,
        @JsonProperty(value = "password", required = true) String password) {

    public static TokenRequestPayloadBuilder builder() {
        return new TokenRequestPayloadBuilder();
    }

    public static class TokenRequestPayloadBuilder {
        private String userName;
        private String password;

        TokenRequestPayloadBuilder() {
            // Intended
        }

        public TokenRequestPayloadBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public TokenRequestPayloadBuilder password(String password) {
            this.password = password;
            return this;
        }

        public TokenRequestPayload build() {
            return new TokenRequestPayload(userName, password);
        }
    }
}
