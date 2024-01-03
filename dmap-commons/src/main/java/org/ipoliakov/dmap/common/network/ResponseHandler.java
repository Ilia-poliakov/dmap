package org.ipoliakov.dmap.common.network;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.protocol.DMapMessage;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResponseHandler extends SimpleChannelInboundHandler<DMapMessage> {

    private final ResponseFutures responseFutures;
    private final ProtoMessageRegistry messageRegistry;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DMapMessage message) {
        long messageId = message.getMessageId();
        CompletableFuture future = responseFutures.get(messageId);
        MessageLite payload = messageRegistry.parsePayload(message);
        future.complete(payload);
    }
}
