package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model;

import java.time.Duration;

public record ApiKeyVerificationConfig(Duration initialDelay, Duration maxDelay, int maxAttempts) {}
