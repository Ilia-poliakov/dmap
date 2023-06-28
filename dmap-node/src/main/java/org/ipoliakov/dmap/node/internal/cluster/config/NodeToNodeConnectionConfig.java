package org.ipoliakov.dmap.node.internal.cluster.config;

import static org.ipoliakov.dmap.node.config.NetworkBaseConfig.threadEventLoopGroup;

import org.ipoliakov.dmap.common.network.ProtoMessageFactory;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class NodeToNodeConnectionConfig {

    @Value("${server.bossThreadNumber}")
    private int bossThreadNumber;

    @Bean
    public ChannelGroup clusterChannelGroup() {
        return new DefaultChannelGroup("clusterChannelGroup", nodeToNodeEventLoopGroup().next());
    }

    @Bean
    public EventLoopGroup nodeToNodeEventLoopGroup() {
        return threadEventLoopGroup(bossThreadNumber);
    }

    @Bean
    public NodeToNodeChannelPipelineInitializer nodeToNodeChannelPipelineInitializer(ResponseFutures responseFutures,
                                                                                     ProtoMessageFactory protoMessageFactory) {
        return new NodeToNodeChannelPipelineInitializer(responseFutures, protoMessageFactory);
    }

    @Bean
    public ResponseFutures responseFutures() {
        return new ResponseFutures();
    }

    public static Class<? extends SocketChannel> channel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
