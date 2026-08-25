package gov.di_ipv_drivingpermit.utilities;

import java.time.Instant;

public class TestRunContext {

    private static Instant suiteStartTime;

    private TestRunContext() {}

    public static void recordSuiteStart() {
        if (suiteStartTime != null) return;
        String epochMs = System.getProperty("suite.start.epoch.ms");
        if (epochMs != null) {
            suiteStartTime = Instant.ofEpochMilli(Long.parseLong(epochMs));
        } else {
            suiteStartTime = Instant.now().minusSeconds(300);
        }
    }

    public static Instant getSuiteStartTime() {
        return suiteStartTime;
    }
}
