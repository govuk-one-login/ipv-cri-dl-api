package uk.gov.di.ipv.cri.drivingpermit.event.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordPayload {
    private String userName;
    private String password;
    private String newPassword;
}
