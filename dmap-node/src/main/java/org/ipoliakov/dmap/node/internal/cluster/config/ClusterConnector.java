package org.ipoliakov.dmap.node.internal.cluster.config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.common.IdGenerator;
import org.ipoliakov.dmap.common.network.MessageSender;
import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftCluster;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
public class ClusterConnector implements InitializingBean {

    @Value("${node.reconnectIntervalMillis}")
    private long networkReconnectIntervalMillis;
    @Value("${MEMBERS:${node.members:}}")
    private String members;

    @Autowired
    private EventLoopGroup nodeToNodeEventLoopGroup;
    @Autowired
    private ScheduledExecutorService reconnectionExecutor;
    @Autowired
    private NodeToNodeChannelPipelineInitializer nodeToNodeChannelPipelineInitializer;
    @Autowired
    private RaftCluster raftCluster;
    @Autowired
    private ResponseFutures responseFutures;
    @Autowired
    private ProtoMessageRegistry protoMessageRegistry;
    @Autowired
    @Qualifier("lockFreeSnowflakeIdGenerator")
    private IdGenerator idGenerator;

    @Override
    public void afterPropertiesSet() {
        if (members.isBlank()) {
            return;
        }
        for (String address : members.split(",")) {
            String[] idHostPort = address.split("[#:]");
            int id = Integer.parseInt(idHostPort[0]);
            String host = idHostPort[1];
            int port = Integer.parseInt(idHostPort[2]);
            connect(id, host, port);
        }
    }

    private void connect(int nodeId, String host, int port) {
        new Bootstrap().group(nodeToNodeEventLoopGroup)
                .channel(channel())
                .handler(nodeToNodeChannelPipelineInitializer)
                .connect(host, port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        Channel channel = future.channel();
                        raftCluster.addMessageSender(nodeId, new MessageSender(idGenerator, responseFutures, protoMessageRegistry, channel));
                        log.debug("Successfully connected to channel = {}", channel);
                        channel.closeFuture().addListener((ChannelFutureListener) future1 -> {
                            Channel c = future1.channel();
                            raftCluster.remove(nodeId);
                            log.debug("Channel {} closed", c);
                            scheduleConnect(nodeId, host, port);
                        });
                    } else {
                        Channel c = future.channel();
                        raftCluster.remove(nodeId);
                        log.warn("Failed to connect to {}", c, future.cause());
                        scheduleConnect(nodeId, host, port);
                    }
                });
    }

    private void scheduleConnect(int id, String host, int port) {
        log.warn("Scheduling reconnect to node on port {} in {} ms", port, networkReconnectIntervalMillis);
        reconnectionExecutor.schedule(() -> connect(id, host, port), networkReconnectIntervalMillis, TimeUnit.MILLISECONDS);
    }

    private static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
