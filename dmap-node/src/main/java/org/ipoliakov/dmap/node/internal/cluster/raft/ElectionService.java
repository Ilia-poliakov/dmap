package org.ipoliakov.dmap.node.internal.cluster.raft;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.internal.cluster.raft.task.LeaderElectionTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ElectionService implements InitializingBean {

    private static final Random RANDOM = new Random();

    private static final int ELECTION_TIMEOUT_LOWER = 3000;
    private static final int ELECTION_TIMEOUT_UPPER = 10000;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private final LeaderElectionTask leaderElectionTask;

    private ScheduledFuture<?> scheduledElectionTask;

    @Override
    public void afterPropertiesSet() {
        scheduleNextElection();
    }

//    public void restartElectionTask() {
//        if (scheduledElectionTask != null) {
//            scheduledElectionTask.cancel(false);
//        }
//        scheduleNextElection();
//    }

    public void scheduleNextElection() {
        this.scheduledElectionTask = scheduledExecutor.schedule(leaderElectionTask, nextElectionTime(), TimeUnit.MILLISECONDS);
    }

    private int nextElectionTime() {
        return RANDOM.nextInt(ELECTION_TIMEOUT_UPPER - ELECTION_TIMEOUT_LOWER) + ELECTION_TIMEOUT_LOWER;
    }

}
