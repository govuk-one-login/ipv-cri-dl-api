package uk.gov.di.ipv.cri.drivingpermit.library.domain;

public enum IssuingAuthority {
    DVLA("DVLA"),
    DVA("DVA");

    private final String name;

    private IssuingAuthority(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
