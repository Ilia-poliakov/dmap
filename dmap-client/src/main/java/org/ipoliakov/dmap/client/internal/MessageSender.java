package org.ipoliakov.dmap.client.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.ipoliakov.dmap.common.network.ProtoMessageFactory;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.ipoliakov.dmap.protocol.DMapMessage;

import com.google.protobuf.MessageLite;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSender {

    private final AtomicLong messageIdSeq = new AtomicLong(1);

    private final Channel channel;
    private final ResponseFutures responseFutures;
    private final ProtoMessageFactory messageFactory;

    public <R extends MessageLite> CompletableFuture<R> send(MessageLite message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();
        long messageId = messageIdSeq.getAndIncrement();
        responseFutures.add(messageId, future);

        DMapMessage dMapMessage = DMapMessage.newBuilder()
                .setMessageId(messageId)
                .setPayload(message.toByteString())
                .setPayloadType(messageFactory.getPayloadType(message.getClass()))
                .build();
        channel.writeAndFlush(dMapMessage);
        return future;
    }
}
