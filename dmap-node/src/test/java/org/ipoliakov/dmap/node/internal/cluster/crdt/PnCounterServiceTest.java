package org.ipoliakov.dmap.node.internal.cluster.crdt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PnCounterServiceTest {

    private static final int NODE_ID_FIRST = 1;

    private PnCounterService service;

    @Mock
    private CrdtReplicationService replicationService;

    @BeforeEach
    void setUp() {
        service = new PnCounterService(NODE_ID_FIRST, replicationService);
    }

    @Test
    void get_shouldCreateCounterIfNotExists() {
        StampedLong c1 = service.get("c1", new VectorClock(NODE_ID_FIRST));
        assertEquals(new StampedLong(0, new VectorClock(NODE_ID_FIRST)), c1);
    }

    @Test
    void addAndGet() {
        VectorClock timestamp = new VectorClock(NODE_ID_FIRST);
        StampedLong actual = service.addAndGet("c1", 10, timestamp);
        VectorClock next = new VectorClock(NODE_ID_FIRST, timestamp.getNext(NODE_ID_FIRST));

        assertEquals(new StampedLong(10, next), actual);
        assertEquals(new StampedLong(10, next), service.get("c1", new VectorClock(1)));
    }
}