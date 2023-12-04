package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class TokenResponse {

    @JsonProperty("id-token")
    private String idToken;

    @JsonCreator
    public TokenResponse(@JsonProperty(value = "id-token", required = true) String idToken) {
        this.idToken = idToken;
    }
}
