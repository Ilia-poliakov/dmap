package org.ipoliakov.dmap.node.cluster.raft.election;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ElectionService implements InitializingBean {

    private static final Random RANDOM = new Random();

    @Value("${raft.electionTimeoutLower}")
    private final int electionTimeoutLower;
    @Value("${raft.electionTimeoutUpper}")
    private final int electionTimeoutUpper;

    @Qualifier("leaderElectionTask")
    private final ScheduledTask leaderElectionTask;
    @Qualifier("leaderElectionTaskExecutor")
    private final ScheduledExecutorService scheduledExecutor;

    private ScheduledFuture<?> scheduledElectionTask;

    @Override
    public void afterPropertiesSet() {
        scheduleNextElection();
    }

    public void restartElectionTask() {
        stopElectionTask();
        scheduleNextElection();
    }

    public void stopElectionTask() {
        if (scheduledElectionTask != null) {
            scheduledElectionTask.cancel(false);
        }
    }

    private void scheduleNextElection() {
        this.scheduledElectionTask = scheduledExecutor.schedule(leaderElectionTask, nextElectionTime(), TimeUnit.MILLISECONDS);
    }

    private int nextElectionTime() {
        return RANDOM.nextInt(electionTimeoutUpper - electionTimeoutLower) + electionTimeoutLower;
    }

}
