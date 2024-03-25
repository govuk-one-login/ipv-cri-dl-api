package uk.gov.di.ipv.cri.drivingpermit.event.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiKeyResponse {
    private String newApiKey;
}
