package uk.gov.di.ipv.cri.drivingpermit.event.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiKeyResponse {
    private String newApiKey;
    private String message;
}
