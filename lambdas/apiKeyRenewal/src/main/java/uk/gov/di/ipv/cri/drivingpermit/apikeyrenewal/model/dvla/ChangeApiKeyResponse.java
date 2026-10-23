package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.dvla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangeApiKeyResponse(String newApiKey) {}
