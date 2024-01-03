package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.client.internal.ClientMessageSender;
import org.ipoliakov.dmap.client.internal.exception.RequestException;
import org.ipoliakov.dmap.protocol.GetReq;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.ValueRes;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DMapClientImpl<K extends Serializable, V extends Serializable> implements DMapClient<K, V> {

    private final ClientMessageSender messageSender;
    private final Serializer<K, ByteString> keySerializer;
    private final Serializer<V, ByteString> valueSerializer;

    @Override
    public CompletableFuture<V> get(K key) {
        GetReq req = GetReq.newBuilder()
                .setPayloadType(PayloadType.GET_REQ)
                .setKey(keySerializer.serialize(key))
                .build();
        return messageSender.send(req, ValueRes.class)
                .thenApply(ValueRes::getValue)
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
        return messageSender.sendToLeader(req, ValueRes.class)
                .thenApply(ValueRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    @Override
    public CompletableFuture<V> remove(K key, V value) {
        RemoveReq req = RemoveReq.newBuilder()
                .setPayloadType(PayloadType.REMOVE_REQ)
                .setKey(keySerializer.serialize(key))
                .build();
        return messageSender.sendToLeader(req, ValueRes.class)
                .thenApply(ValueRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    private V handleError(Message message, Throwable t) {
        String errorMessage = "Failed to send PutRequest = " + message;
        log.error(errorMessage, t);
        throw new RequestException(errorMessage, t);
    }
}
