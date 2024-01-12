package org.ipoliakov.dmap.node.internal.cluster.crdt.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.internal.cluster.crdt.PnCounterService;
import org.ipoliakov.dmap.protocol.PnCounterReplicationData;
import org.ipoliakov.dmap.protocol.PnCounterStateData;
import org.ipoliakov.dmap.protocol.VectorClockSnapshot;
import org.ipoliakov.dmap.protocol.internal.PnCounterReplicationRes;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

@Timeout(10)
class PnCounterReplicationReqCmdTest extends IntegrationTest {

    @Autowired
    private PnCounterService pnCounterService;

    @Test
    void execute() throws Exception {
        var replicationData = PnCounterReplicationData.newBuilder()
                .setName("counter")
                .setTimestamp(VectorClockSnapshot.newBuilder().putTimestampByNodes(1, 100))
                .addState(
                        PnCounterStateData.newBuilder()
                                .setPCounter(6)
                                .setNCounter(1)
                                .setNodeId(1)
                );

        var replicationReq = ProtoMessages.pnCounterReplicationReq()
                .addReplicationData(replicationData)
                .build();
        CompletableFuture.allOf(
                cluster.sendToAll(replicationReq, PnCounterReplicationRes.class)
                        .toArray(new CompletableFuture[0])
        ).get(10, TimeUnit.SECONDS);

        StampedLong actual = pnCounterService.get(replicationData.getName(), new VectorClock(1));
        StampedLong expected = new StampedLong(5, new VectorClock(1, 100));
        assertEquals(expected, actual);
    }
}