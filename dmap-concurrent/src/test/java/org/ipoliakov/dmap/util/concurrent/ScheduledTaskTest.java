package org.ipoliakov.dmap.util.concurrent;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduledTaskTest {

    @Test
    @DisplayName("Failed task should not break down the executor")
    void run_shouldNotFailExecutor() throws Exception {
        AtomicBoolean success = new AtomicBoolean(false);
        try (ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor()) {
            scheduledExecutor.schedule((ScheduledTask) () -> {
                throw new RuntimeException("Fail");
            }, 1, TimeUnit.MILLISECONDS).get();
            scheduledExecutor.schedule((ScheduledTask) () -> success.set(true), 1, TimeUnit.MILLISECONDS);
            await().untilTrue(success);
        }
    }
}