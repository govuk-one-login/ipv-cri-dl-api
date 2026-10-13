package uk.gov.di.ipv.cri.drivingpermit.apikeyrenewal.model.aws;

public enum SecretRotationStep {
    CREATE_SECRET("createSecret"),
    SET_SECRET("setSecret"),
    TEST_SECRET("testSecret"),
    FINISH_SECRET("finishSecret");

    private final String value;

    SecretRotationStep(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
