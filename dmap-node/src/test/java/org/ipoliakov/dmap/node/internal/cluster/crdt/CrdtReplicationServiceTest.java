package org.ipoliakov.dmap.node.internal.cluster.crdt;

import static org.mockito.Mockito.verify;

import java.util.Map;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounter;
import org.ipoliakov.dmap.node.internal.cluster.Cluster;
import org.ipoliakov.dmap.node.internal.cluster.crdt.mapping.PnCounterMapperImpl;
import org.ipoliakov.dmap.protocol.PnCounterReplicationData;
import org.ipoliakov.dmap.protocol.PnCounterStateData;
import org.ipoliakov.dmap.protocol.VectorClockSnapshot;
import org.ipoliakov.dmap.protocol.internal.PnCounterReplicationRes;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrdtReplicationServiceTest {

    @Mock
    private Cluster cluster;
    @Spy
    private PnCounterMapperImpl mapper;
    @InjectMocks
    private CrdtReplicationService replicationService;

    @Test
    void replicate() {
        PnCounter counter = new PnCounter(1);
        counter.addAndGet(100, new VectorClock(1));

        replicationService.replicate(Map.of("counter", counter));

        var expectedRequest = ProtoMessages.pnCounterReplicationReq()
                .addReplicationData(
                        PnCounterReplicationData.newBuilder()
                                .setName("counter")
                                .setTimestamp(VectorClockSnapshot.newBuilder().putTimestampByNodes(1, Long.MIN_VALUE + 1))
                                .addState(PnCounterStateData.newBuilder()
                                        .setNodeId(1)
                                        .setPCounter(100)
                                        .setNodeId(1)
                                )
                );
        verify(cluster).sendToAll(expectedRequest.build(), PnCounterReplicationRes.class);
    }
}
