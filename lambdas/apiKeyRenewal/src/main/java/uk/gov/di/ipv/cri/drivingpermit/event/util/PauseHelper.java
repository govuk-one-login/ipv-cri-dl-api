package uk.gov.di.ipv.cri.drivingpermit.event.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PauseHelper.class);
    private final long millis;

    public PauseHelper(long millis) {
        this.millis = millis;
    }

    public void pause() throws InterruptedException {
        LOGGER.info("Pausing thread for {}ms before performing driver match", millis);
        Thread.sleep(millis);
    }
}
