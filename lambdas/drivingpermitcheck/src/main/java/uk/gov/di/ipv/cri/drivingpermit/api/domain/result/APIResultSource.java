package uk.gov.di.ipv.cri.drivingpermit.api.domain.result;

public enum APIResultSource {
    DCS("dcs"),
    DVA("dva"),
    DVLA("dvla");

    private final String name;

    APIResultSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
