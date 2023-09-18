package uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRequestPayload {
    private String userName;
    private String password;
}
