package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.client.internal.exception.RequestException;
import org.ipoliakov.dmap.common.network.MessageSender;
import org.ipoliakov.dmap.protocol.storage.GetReq;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.protocol.storage.RemoveReq;
import org.ipoliakov.dmap.protocol.storage.ValueRes;
import org.ipoliakov.dmap.util.ProtoMessages;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class KvStorageClientImpl<K extends Serializable, V extends Serializable> implements KvStorageClient<K, V> {

    private final MessageSender messageSender;
    private final Serializer<K, ByteString> keySerializer;
    private final Serializer<V, ByteString> valueSerializer;

    @Override
    public CompletableFuture<V> get(K key) {
        GetReq req = ProtoMessages.getReq(keySerializer.serialize(key));
        return messageSender.send(req, ValueRes.class)
                .thenApply(ValueRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    @Override
    public CompletableFuture<V> put(K key, V value) {
        PutReq req = ProtoMessages.putReq()
                .setKey(keySerializer.serialize(key))
                .setValue(valueSerializer.serialize(value))
                .build();
        return messageSender.send(req, ValueRes.class)
                .thenApply(ValueRes::getValue)
                .thenApply(valueSerializer::dserialize)
                .exceptionally(t -> handleError(req, t));
    }

    @Override
    public CompletableFuture<V> remove(K key, V value) {
        RemoveReq req = ProtoMessages.removeReq(keySerializer.serialize(key));
        return messageSender.send(req, ValueRes.class)
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
