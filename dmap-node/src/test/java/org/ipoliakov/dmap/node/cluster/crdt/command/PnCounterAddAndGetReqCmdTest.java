package org.ipoliakov.dmap.node.cluster.crdt.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.protocol.crdt.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.crdt.VectorClockSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class PnCounterAddAndGetReqCmdTest extends IntegrationTest {

    @Test
    void execute() throws Exception {
        String name = UUID.randomUUID().toString();
        PnCounterSnapshot actual = crdtClient.addAndGetCounter(name, 100, Map.of(1, Long.MIN_VALUE)).get();
        PnCounterSnapshot expected = PnCounterSnapshot.newBuilder()
                .setValue(100)
                .setTimestamp(VectorClockSnapshot.newBuilder()
                        .putAllTimestampByNodes(Map.of(1, Long.MIN_VALUE + 1))
                ).build();
        assertEquals(expected, actual);
    }
}