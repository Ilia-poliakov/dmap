package org.ipoliakov.dmap.client.internal;

import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.ipoliakov.dmap.protocol.DMapMessage;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientPipelineInitializer extends ChannelInitializer<SocketChannel> {

    private final ResponseFutures responseFutures;
    private final ProtoMessageFactory messageFactory;

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline p = channel.pipeline();

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(DMapMessage.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new ResponseHandler(responseFutures, messageFactory));
    }
}
