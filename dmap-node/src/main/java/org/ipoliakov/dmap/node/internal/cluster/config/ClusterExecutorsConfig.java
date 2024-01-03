package org.ipoliakov.dmap.node.internal.cluster.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration
public class ClusterExecutorsConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService leaderElectionTaskExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("leader-election-thread"));
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService heartbeatTaskExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("heartbeat-thread"));
    }

    @Bean
    public ExecutorService raftExecutorService() {
        return new ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(16),
                new CustomizableThreadFactory("raft-thread")
        );
    }
}
