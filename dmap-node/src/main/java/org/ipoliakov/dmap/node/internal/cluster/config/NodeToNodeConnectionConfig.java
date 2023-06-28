package org.ipoliakov.dmap.node.internal.cluster.config;

import static org.ipoliakov.dmap.node.config.NetworkBaseConfig.threadEventLoopGroup;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.common.network.ProtoMessageFactory;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class NodeToNodeConnectionConfig implements InitializingBean {

    @Value("${server.bossThreadNumber}")
    private int bossThreadNumber;
    @Value("${node.reconnectIntervalMillis}")
    private long networkReconnectIntervalMillis;
    @Value("${node.addresses}")
    private String addresses;

    @Autowired
    private EventLoopGroup nodeToNodeEventLoopGroup;
    @Autowired
    private ScheduledExecutorService reconnectionExecutor;
    @Autowired
    private NodeToNodeChannelPipelineInitializer nodeToNodeChannelPipelineInitializer;

    @Override
    public void afterPropertiesSet() {
        for (String address : addresses.split(",")) {
            String[] idHostPort = address.split("[$:]");
            int id = Integer.parseInt(idHostPort[0]);
            String host = idHostPort[1];
            int port = Integer.parseInt(idHostPort[2]);
            connect(id, host, port);
        }
    }

    private void connect(int id, String host, int port) {
        new Bootstrap().group(nodeToNodeEventLoopGroup)
                .channel(channel())
                .handler(nodeToNodeChannelPipelineInitializer)
                .connect(host, port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        Channel channel = future.channel();
                        log.debug("Successfully connected to channel = {}", channel);
                        channel.closeFuture().addListener((ChannelFutureListener) future1 -> {
                            log.debug("Channel {} closed", future1.channel());
                            scheduleConnect(id, host, port);
                        });
                    } else {
                        log.warn("Failed to connect to {}", future.channel(), future.cause());
                        scheduleConnect(id, host, port);
                    }
                });
    }

    private void scheduleConnect(int id, String host, int port) {
        log.warn("Scheduling reconnect to node on port {} in {} ms", port, networkReconnectIntervalMillis);
        reconnectionExecutor.schedule(() -> connect(id, host, port), networkReconnectIntervalMillis, TimeUnit.MILLISECONDS);
    }

    @Bean
    public EventLoopGroup nodeToNodeEventLoopGroup() {
        return threadEventLoopGroup(bossThreadNumber);
    }

    @Bean
    public NodeToNodeChannelPipelineInitializer nodeToNodeChannelPipelineInitializer(ResponseFutures responseFutures, ProtoMessageFactory protoMessageFactory) {
        return new NodeToNodeChannelPipelineInitializer(responseFutures, protoMessageFactory);
    }

    @Bean
    public ResponseFutures responseFutures() {
        return new ResponseFutures();
    }

    private static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
