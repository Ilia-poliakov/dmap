package org.ipoliakov.dmap.node.internal.cluster.config;

import org.ipoliakov.dmap.common.network.ProtoMessageFactory;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.ipoliakov.dmap.common.network.ResponseHandler;
import org.ipoliakov.dmap.protocol.DMapMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NodeToNodeChannelPipelineInitializer extends ChannelInitializer<Channel> {

    private final ResponseFutures responseFutures;
    private final ProtoMessageFactory messageFactory;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(DMapMessage.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new ResponseHandler(responseFutures, messageFactory));
    }
}
