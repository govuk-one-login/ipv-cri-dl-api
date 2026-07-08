package gov.di_ipv_drivingpermit.utilities;

import java.time.Instant;

public class TestRunContext {

    private static Instant suiteStartTime;

    private TestRunContext() {}

    public static void recordSuiteStart() {
        if (suiteStartTime == null) {
            suiteStartTime = Instant.now().minusSeconds(30);
        }
    }

    public static Instant getSuiteStartTime() {
        return suiteStartTime;
    }
}
