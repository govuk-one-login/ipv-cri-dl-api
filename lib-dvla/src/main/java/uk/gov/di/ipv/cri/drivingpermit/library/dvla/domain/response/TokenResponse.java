package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {

    @JsonProperty("id-token")
    private String idToken;

    @JsonCreator
    public TokenResponse(@JsonProperty(value = "id-token", required = true) String idToken) {
        this.idToken = idToken;
    }

    public static TokenResponseBuilder builder() {
        return new TokenResponseBuilder();
    }

    public String getIdToken() {
        return this.idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public static class TokenResponseBuilder {
        private String idToken;

        TokenResponseBuilder() {}

        public TokenResponseBuilder idToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        public TokenResponse build() {
            return new TokenResponse(this.idToken);
        }
    }
}
