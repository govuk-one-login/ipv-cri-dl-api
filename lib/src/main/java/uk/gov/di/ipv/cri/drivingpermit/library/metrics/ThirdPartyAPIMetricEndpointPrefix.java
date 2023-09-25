package uk.gov.di.ipv.cri.drivingpermit.library.metrics;

/** Not for direct use - see {@link ThirdPartyAPIEndpointMetric} */
public enum ThirdPartyAPIMetricEndpointPrefix {
    // Only one endpoint on DCS API
    DCS_THIRD_PARTY_API_DCS_ENDPOINT,
    // DVA Api Endpoints
    DVA_THIRD_PARTY_API_DVA_ENDPOINT,
    // DLVA Api Endpoints
    DVLA_THIRD_PARTY_API_TOKEN_ENDPOINT,
    DVLA_THIRD_PARTY_API_MATCH_ENDPOINT;
}
