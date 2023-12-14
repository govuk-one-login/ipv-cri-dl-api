package uk.gov.di.ipv.cri.drivingpermit.event.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiKeyResponse {
    private String newApiKey;
}
