package org.ipoliakov.dmap.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ScheduledTask extends Runnable {

    Logger log = LoggerFactory.getLogger(ScheduledTask.class);

    default void run() {
        try {
            execute();
        } catch (Exception e) {
            log.error("Error while running scheduled task", e);
        }
    }

    void execute();
}
