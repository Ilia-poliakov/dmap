package org.ipoliakov.dmap.common.network;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.common.IdGenerator;
import org.ipoliakov.dmap.protocol.DMapMessage;

import com.google.protobuf.MessageLite;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class MessageSender {

    private final IdGenerator idGenerator;
    private final ResponseFutures responseFutures;
    private final ProtoMessageRegistry messageRegistry;

    private volatile Channel channel;

    public <R extends MessageLite> CompletableFuture<R> send(MessageLite message, Class<R> responseType) {
        return send(message, channel, responseType);
    }

    public <R extends MessageLite> CompletableFuture<R> send(MessageLite message, Channel channel, Class<R> responseType) {
        CompletableFuture<R> future = new CompletableFuture<>();
        long messageId = idGenerator.next();
        responseFutures.add(messageId, future);

        DMapMessage dMapMessage = DMapMessage.newBuilder()
                .setMessageId(messageId)
                .setPayload(message.toByteString())
                .setPayloadType(messageRegistry.getPayloadType(message.getClass()))
                .build();
        channel.writeAndFlush(dMapMessage, channel.voidPromise());
        return future;
    }
}
