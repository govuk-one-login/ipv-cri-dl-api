package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseStatusCodes {
    // Extracted from dev portal + driver-get version 1.13.0
    public static final int SUCCESS = 200;

    public static final int BAD_REQUEST = 400; // Invalid payload data sent
    public static final int UNAUTHORISED = 401; // Username/Password or Token Invalid/Expired
    public static final int FORBIDDEN = 403; // Token Missing or Invalid use of get vs post
    public static final int NOT_FOUND =
            404; // For Match this is a valid response meaning - driving licence record not found

    public static final int TOO_MANY_REQUESTS = 429; // Rate limiting

    public static final int UNSPECIFIED_ERROR_500 = 500; // Remote API Server Error
    public static final int BAD_GATEWAY = 502; // Remote API Network Issue
    public static final int GATEWAY_TIMEOUT = 504; // Remote API Network Issue
}
