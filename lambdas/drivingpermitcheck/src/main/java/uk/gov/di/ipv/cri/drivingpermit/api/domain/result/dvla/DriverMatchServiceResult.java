package uk.gov.di.ipv.cri.drivingpermit.api.domain.result.dvla;

import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dvla.response.Validity;

@Builder
@Data
public class DriverMatchServiceResult {
    private final Validity validity;
    private final String requestId;
}
