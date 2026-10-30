package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.aws;

public enum SecretVersionStage {
    CURRENT("AWSCURRENT"),
    PENDING("AWSPENDING"),
    PREVIOUS("AWSPREVIOUS");

    private final String value;

    SecretVersionStage(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
