package org.ipoliakov.dmap.node.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

@Configuration
public class NetworkBaseConfig {

    public static EventLoopGroup threadEventLoopGroup(int threadNumber) {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadNumber) : new NioEventLoopGroup(threadNumber);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService reconnectionExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
