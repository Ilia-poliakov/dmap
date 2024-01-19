package org.ipoliakov.dmap.it.crdt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.it.ClusterIntegrationTest;
import org.ipoliakov.dmap.protocol.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.VectorClockSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
public class PnCounterTest extends ClusterIntegrationTest {

    @Test
    void incrementCounter_withReplication() throws Exception {
        DMapClient<String, String> client1 = clients.getFirst();
        String name = UUID.randomUUID().toString();
        Map<Integer, Long> lastObservedTimestamp = Map.of(1, Long.MIN_VALUE);

        PnCounterSnapshot snapshot = client1.addAndGetCounter(name, 100, lastObservedTimestamp)
                .get(10, TimeUnit.SECONDS);

        PnCounterSnapshot expected = PnCounterSnapshot.newBuilder()
                .setValue(100)
                .setTimestamp(VectorClockSnapshot.newBuilder()
                        .putAllTimestampByNodes(Map.of(1, Long.MIN_VALUE + 1)))
                .build();
        assertEquals(expected, snapshot);

        for (int i = 0; i < clients.size(); i++) {
            DMapClient<String, String> client = clients.get(i);
            PnCounterSnapshot actual = client.getCounterValue(name, lastObservedTimestamp).get(10, TimeUnit.SECONDS);
            assertEquals(100, actual.getValue(), "Wrong result for client " + (i + 1));
        }
    }
}
