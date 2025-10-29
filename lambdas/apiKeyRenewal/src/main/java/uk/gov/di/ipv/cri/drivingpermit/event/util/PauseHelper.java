package uk.gov.di.ipv.cri.drivingpermit.event.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PauseHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private final long millis;

    public PauseHelper(long millis) {
        this.millis = millis;
    }

    public void pause() throws InterruptedException {
        LOGGER.info("Pausing thread for {}ms before performing driver match", millis);
        Thread.sleep(millis);
    }
}
