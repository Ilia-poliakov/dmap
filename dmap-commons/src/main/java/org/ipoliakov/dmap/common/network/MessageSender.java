package org.ipoliakov.dmap.common.network;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.common.IdGenerator;
import org.ipoliakov.dmap.protocol.DMapMessage;

import com.google.protobuf.MessageLite;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageSender {

    private final Channel channel;
    private final IdGenerator idGenerator;
    private final ResponseFutures responseFutures;
    private final ProtoMessageRegistry messageFactory;

    public <R extends MessageLite> CompletableFuture<R> send(MessageLite message, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();
        long messageId = idGenerator.next();
        responseFutures.add(messageId, future);

        DMapMessage dMapMessage = DMapMessage.newBuilder()
                .setMessageId(messageId)
                .setPayload(message.toByteString())
                .setPayloadType(messageFactory.getPayloadType(message.getClass()))
                .build();
        channel.writeAndFlush(dMapMessage, channel.voidPromise());
        return future;
    }
}
