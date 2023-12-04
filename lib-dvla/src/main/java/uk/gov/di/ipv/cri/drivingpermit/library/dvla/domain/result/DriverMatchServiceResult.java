package uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.result;

import lombok.Builder;
import lombok.Data;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.domain.response.Validity;

@Builder
@Data
public class DriverMatchServiceResult {
    private final Validity validity;
    private final String requestId;
}
