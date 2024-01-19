package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.client.internal.exception.RequestException;
import org.ipoliakov.dmap.common.MonotonicallyIdGenerator;
import org.ipoliakov.dmap.common.network.MessageSender;
import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.ipoliakov.dmap.protocol.GetReq;
import org.ipoliakov.dmap.protocol.PnCounterAddAndGetReq;
import org.ipoliakov.dmap.protocol.PnCounterAddAndGetRes;
import org.ipoliakov.dmap.protocol.PnCounterGetReq;
import org.ipoliakov.dmap.protocol.PnCounterGetRes;
import org.ipoliakov.dmap.protocol.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.ValueRes;
import org.ipoliakov.dmap.protocol.VectorClockSnapshot;
import org.ipoliakov.dmap.util.ProtoMessages;

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
                   ProtoMessageRegistry protoMessageRegistry,
                   Serializer<K, ByteString> keySerializer,
                   Serializer<V, ByteString> valueSerializer) {
        this.messageSender = new MessageSender(channel, new MonotonicallyIdGenerator(), responseFutures, protoMessageRegistry);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

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

    @Override
    public CompletableFuture<PnCounterSnapshot> getCounterValue(String name, Map<Integer, Long> lastObservedTimestamp) {
        PnCounterGetReq req = ProtoMessages.pnCounterGetReq(name)
                .setTimestamp(
                        VectorClockSnapshot.newBuilder()
                                .putAllTimestampByNodes(lastObservedTimestamp))
                .build();
        return messageSender.send(req, PnCounterGetRes.class)
                .thenApply(PnCounterGetRes::getValue);
    }

    @Override
    public CompletableFuture<PnCounterSnapshot> addAndGetCounter(String name, long delta, Map<Integer, Long> lastObservedTimestamp) {
        PnCounterAddAndGetReq req = ProtoMessages.pnCounterAddAndGetReq(name, delta)
                .setTimestamp(
                        VectorClockSnapshot.newBuilder()
                                .putAllTimestampByNodes(lastObservedTimestamp))
                .build();
        return messageSender.send(req, PnCounterAddAndGetRes.class)
                .thenApply(PnCounterAddAndGetRes::getValue);
    }

    private V handleError(Message message, Throwable t) {
        String errorMessage = "Failed to send PutRequest = " + message;
        log.error(errorMessage, t);
        throw new RequestException(errorMessage, t);
    }
}
