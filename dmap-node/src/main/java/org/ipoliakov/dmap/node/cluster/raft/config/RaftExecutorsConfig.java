package org.ipoliakov.dmap.node.cluster.raft.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration
public class RaftExecutorsConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService leaderElectionTaskExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("leader-election-thread"));
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService heartbeatTaskExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("heartbeat-thread"));
    }
}
