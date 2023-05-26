package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.client.internal.MessageSender;
import org.ipoliakov.dmap.client.internal.ResponseFutures;
import org.ipoliakov.dmap.client.internal.exception.RequestException;
import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.ipoliakov.dmap.protocol.GetReq;
import org.ipoliakov.dmap.protocol.GetRes;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.PutRes;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DMapClientImpl<K extends Serializable, V extends Serializable> implements DMapClient<K, V> {

    private final MessageSender messageSender;
    private final Serializer<K, ByteString> keySerializer;
    private final Serializer<V, ByteString> valueSerializer;

    DMapClientImpl(Channel channel,
                   ResponseFutures responseFutures,
                   ProtoMessageFactory protoMessageFactory,
                   Serializer<K, ByteString> keySerializer,
                   Serializer<V, ByteString> valueSerializer) {
        messageSender = new MessageSender(channel, responseFutures, protoMessageFactory);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public CompletableFuture<V> get(K key) {
        GetReq req = GetReq.newBuilder()
                .setPayloadType(PayloadType.GET_REQ)
                .setKey(keySerializer.serialize(key))
                .build();
        return messageSender.send(req, GetRes.class)
                .thenApply(GetRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    @Override
    public CompletableFuture<V> put(K key, V value) {
        PutReq req = PutReq.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setKey(keySerializer.serialize(key))
                .setValue(valueSerializer.serialize(value))
                .build();
        return messageSender.send(req, PutRes.class)
                .thenApply(PutRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    private V handleError(Message message, Throwable t) {
        String errorMessage = "Failed to send PutRequest = " + message;
        log.error(errorMessage, t);
        throw new RequestException(errorMessage, t);
    }
}
