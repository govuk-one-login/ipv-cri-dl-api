package gov.di_ipv_drivingpermit.utilities;

import java.util.HashMap;
import java.util.Map;

public class TestDataCreator {

    public static Map<String, TestInput> dvaTestUsers = new HashMap<>();
    public static Map<String, TestInput> dvlaTestUsers = new HashMap<>();

    public static DVADrivingLicenceSubject billyBatsonHappyPath;
    public static DVADrivingLicenceSubject kennethDecerqueiraHappyPath;
    public static DVADrivingLicenceSubject selinaUnhappyPath;
    public static DrivingLicenceSubject peterHappyPath;
    public static DrivingLicenceSubject kennethDecerqueiraDvlaHappyPath;

    public static void createDefaultResponses() {
        billyBatsonHappyPath =
                new DVADrivingLicenceSubject(
                        "55667788",
                        "BATSON",
                        "BILLY",
                        "",
                        "26",
                        "07",
                        "1981",
                        "01",
                        "10",
                        "2042",
                        "19",
                        "04",
                        "2018",
                        "NW3 5RG");
        selinaUnhappyPath =
                new DVADrivingLicenceSubject(
                        "88776655",
                        "KYLE",
                        "SELINA",
                        "",
                        "12",
                        "08",
                        "1985",
                        "04",
                        "08",
                        "2032",
                        "14",
                        "09",
                        "2018",
                        "E20 2AQ");

        kennethDecerqueiraHappyPath =
                new DVADrivingLicenceSubject(
                        "12345678",
                        "DECERQUEIRA",
                        "KENNETH",
                        "",
                        "08",
                        "07",
                        "1965",
                        "01",
                        "10",
                        "2042",
                        "19",
                        "04",
                        "2018",
                        "BA2 5AA");

        kennethDecerqueiraDvlaHappyPath =
                new DrivingLicenceSubject(
                        "DECER607085K99AE",
                        "DECERQUEIRA",
                        "KENNETH",
                        "",
                        "08",
                        "07",
                        "1965",
                        "01",
                        "05",
                        "2035",
                        "02",
                        "05",
                        "2025",
                        "17",
                        "BA2 5AA");

        peterHappyPath =
                new DrivingLicenceSubject(
                        "PARKE610112PBFGH",
                        "PARKER",
                        "PETER",
                        "BENJAMIN",
                        "11",
                        "10",
                        "1962",
                        "09",
                        "12",
                        "2062",
                        "23",
                        "05",
                        "2018",
                        "12",
                        "BS98 1TL");

        dvaTestUsers.put("DVADrivingLicenceSubjectHappyBilly", billyBatsonHappyPath);
        dvaTestUsers.put("DVADrivingLicenceSubjectHappyKenneth", kennethDecerqueiraHappyPath);
        dvaTestUsers.put("DVADrivingLicenceSubjectUnhappySelina", selinaUnhappyPath);

        dvlaTestUsers.put("DrivingLicenceSubjectHappyPeter", peterHappyPath);
        dvlaTestUsers.put("DrivingLicenceSubjectHappyKenneth", kennethDecerqueiraDvlaHappyPath);
    }

    public static TestInput getTestUserFromMap(String issuer, String scenario) {
        if (issuer.equals("DVLA")) {
            return dvlaTestUsers.get(scenario);
        } else {
            return dvaTestUsers.get(scenario);
        }
    }
}
