package org.ipoliakov.dmap.rpc.server;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerPipelineInitializer extends ChannelInitializer<Channel> {

    public final ProtobufEncoder protobufEncoder = new ProtobufEncoder();
    public final ProtobufDecoder protobufDecoder = new ProtobufDecoder(DMapMessage.getDefaultInstance());

    private final DispatcherCommandHandler dispatcherCommandHandler;

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast("protobufDecoder", protobufDecoder);
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobufEncoder", protobufEncoder);

        pipeline.addLast("dispatchingCommand", dispatcherCommandHandler);
    }
}