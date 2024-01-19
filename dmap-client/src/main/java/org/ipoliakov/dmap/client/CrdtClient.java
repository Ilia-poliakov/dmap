package org.ipoliakov.dmap.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.common.network.MessageSender;
import org.ipoliakov.dmap.protocol.crdt.PnCounterAddAndGetReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterAddAndGetRes;
import org.ipoliakov.dmap.protocol.crdt.PnCounterGetReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterGetRes;
import org.ipoliakov.dmap.protocol.crdt.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.crdt.VectorClockSnapshot;
import org.ipoliakov.dmap.util.ProtoMessages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrdtClient {

    private final MessageSender messageSender;

    public CompletableFuture<PnCounterSnapshot> getCounterValue(String name, Map<Integer, Long> lastObservedTimestamp) {
        PnCounterGetReq req = ProtoMessages.pnCounterGetReq(name)
                .setTimestamp(vectorClockSnapshot(lastObservedTimestamp))
                .build();
        return messageSender.send(req, PnCounterGetRes.class)
                .thenApply(PnCounterGetRes::getValue);
    }

    public CompletableFuture<PnCounterSnapshot> addAndGetCounter(String name, long delta, Map<Integer, Long> lastObservedTimestamp) {
        PnCounterAddAndGetReq req = ProtoMessages.pnCounterAddAndGetReq(name, delta)
                .setTimestamp(vectorClockSnapshot(lastObservedTimestamp))
                .build();
        return messageSender.send(req, PnCounterAddAndGetRes.class)
                .thenApply(PnCounterAddAndGetRes::getValue);
    }

    private static VectorClockSnapshot vectorClockSnapshot(Map<Integer, Long> lastObservedTimestamp) {
        return VectorClockSnapshot.newBuilder()
                .putAllTimestampByNodes(lastObservedTimestamp)
                .build();
    }
}
