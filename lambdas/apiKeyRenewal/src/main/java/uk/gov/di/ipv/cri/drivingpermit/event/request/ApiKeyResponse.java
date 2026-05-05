package uk.gov.di.ipv.cri.drivingpermit.event.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiKeyResponse(String newApiKey, String message) {}
