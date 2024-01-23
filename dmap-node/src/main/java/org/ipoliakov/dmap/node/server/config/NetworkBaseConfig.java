package org.ipoliakov.dmap.node.server.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

@Configuration
public class NetworkBaseConfig {

    public static EventLoopGroup threadEventLoopGroup(int threadNumber, String name) {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(name);
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threadNumber, threadFactory) : new NioEventLoopGroup(threadNumber, threadFactory);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService reconnectionExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
