package org.ipoliakov.dmap.node.internal.cluster.raft;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.ipoliakov.dmap.node.internal.cluster.raft.heartbeat.HeartbeatService;
import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class HeartbeatServiceTest {

    private static final int EXECUTION_TIMES = 3;

    private TestHeartbeatTask heartbeatTask;
    private HeartbeatService heartbeatService;

    @BeforeEach
    void setUp() {
        heartbeatTask = new TestHeartbeatTask();
        heartbeatService = new HeartbeatService(1000, heartbeatTask, Executors.newSingleThreadScheduledExecutor());
    }

    @AfterEach
    void tearDown() {
        heartbeatService.stopSendingHeartbeats();
    }

    @Test
    void startSendingHeartbeats() {
        heartbeatService.startSendingHeartbeats();
        await().untilAtomic(heartbeatTask.counter, is(EXECUTION_TIMES));
    }

    @Test
    void stopSendingHeartbeats() {
        heartbeatService.startSendingHeartbeats();
        await().untilAtomic(heartbeatTask.counter, is(EXECUTION_TIMES));
        heartbeatService.stopSendingHeartbeats();
        assertEquals(EXECUTION_TIMES, heartbeatTask.counter.get());
    }

    @Test
    @DisplayName("stopSendingHeartbeats - do nothing when not started")
    void stopSendingHeartbeats_withoutStarting() {
        heartbeatService.stopSendingHeartbeats();
        assertEquals(0, heartbeatTask.counter.get());
    }

    @Test
    void stopSendingHeartbeats_stopTwice() {
        heartbeatService.startSendingHeartbeats();
        await().untilAtomic(heartbeatTask.counter, is(EXECUTION_TIMES));
        heartbeatService.stopSendingHeartbeats();
        heartbeatService.stopSendingHeartbeats();
        assertEquals(EXECUTION_TIMES, heartbeatTask.counter.get());
    }

    private static class TestHeartbeatTask implements ScheduledTask {

        final AtomicInteger counter = new AtomicInteger();

        @Override
        public void runInternal() {
            counter.incrementAndGet();
        }
    }

}