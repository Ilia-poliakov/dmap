package org.ipoliakov.dmap.node.internal.cluster.raft.heartbeat;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeartbeatService {

    @Value("${raft.heartbeatInterval}")
    private final int heartbeatInterval;
    @Qualifier("heartbeatTask")
    private final ScheduledTask heartbeatTask;
    @Qualifier("heartbeatTaskExecutor")
    private final ScheduledExecutorService scheduledExecutor;
    private ScheduledFuture<?> scheduledHeartbeatTask;

    public void startSendingHeartbeats() {
        stopSendingHeartbeats();
        scheduledHeartbeatTask = scheduledExecutor.scheduleAtFixedRate(heartbeatTask, 0, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    public void stopSendingHeartbeats() {
        if (scheduledHeartbeatTask != null) {
            scheduledHeartbeatTask.cancel(false);
        }
    }

}
