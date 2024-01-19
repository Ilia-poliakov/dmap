package org.ipoliakov.dmap.node.internal.cluster.crdt.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.internal.cluster.crdt.PnCounterService;
import org.ipoliakov.dmap.protocol.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.VectorClockSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

@Timeout(10)
class PnCounterGetReqCmdTest extends IntegrationTest {

    @Autowired
    private PnCounterService pnCounterService;

    @Test
    void execute() throws Exception {
        String name = UUID.randomUUID().toString();
        pnCounterService.addAndGet(name, 100, new VectorClock(1));
        PnCounterSnapshot actual = crdtClient.getCounterValue(name, Map.of(1, Long.MIN_VALUE)).get(10, TimeUnit.SECONDS);

        PnCounterSnapshot expected = PnCounterSnapshot.newBuilder()
                .setValue(100)
                .setTimestamp(VectorClockSnapshot.newBuilder()
                        .putAllTimestampByNodes(Map.of(1, Long.MIN_VALUE + 1))
                ).build();
        assertEquals(expected, actual);
    }
}