package org.ipoliakov.dmap.node.internal.cluster.config;

import static org.ipoliakov.dmap.node.server.config.NetworkBaseConfig.threadEventLoopGroup;

import java.time.Clock;

import org.ipoliakov.dmap.common.rpc.ProtoMessageRegistry;
import org.ipoliakov.dmap.common.rpc.ResponseFutures;
import org.ipoliakov.dmap.util.concurrent.LockFreeSnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class NodeToNodeConnectionConfig {

    @Value("${MEMBER_ID:${member.id:}}")
    private int memberId;
    @Value("${cluster.clientThreadNumber}")
    private int clientThreadNumber;

    @Bean
    public EventLoopGroup nodeToNodeEventLoopGroup() {
        return threadEventLoopGroup(clientThreadNumber, "cluster-");
    }

    @Bean
    public NodeToNodeChannelPipelineInitializer nodeToNodeChannelPipelineInitializer(ResponseFutures responseFutures,
                                                                                     ProtoMessageRegistry protoMessageRegistry) {
        return new NodeToNodeChannelPipelineInitializer(responseFutures, protoMessageRegistry);
    }

    @Bean
    public ResponseFutures responseFutures() {
        return new ResponseFutures();
    }

    @Bean
    public LockFreeSnowflakeIdGenerator lockFreeSnowflakeIdGenerator(Clock clock) {
        return new LockFreeSnowflakeIdGenerator(clock, memberId);
    }
}
