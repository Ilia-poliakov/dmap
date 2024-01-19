package org.ipoliakov.dmap.node.internal.cluster.crdt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PnCounterService {

    private final Map<String, PnCounter> counters = new ConcurrentHashMap<>();

    @Value("${MEMBER_ID:${member.id:}}")
    private final int memberId;
    private final CrdtReplicationService replicationService;

    public StampedLong get(String name, VectorClock vectorClock) {
        PnCounter counter = getOrCreate(name);
        return counter.get(vectorClock);
    }

    public StampedLong addAndGet(String name, long delta, VectorClock timestamp) {
        PnCounter counter = getOrCreate(name);
        StampedLong stampedLong = counter.addAndGet(delta, timestamp);
        replicationService.replicate(counters);
        return stampedLong;
    }

    public void merge(Map<String, PnCounter> counters) {
        counters.forEach((name, counter) -> {
            PnCounter localCounter = getOrCreate(name);
            localCounter.merge(counter);
        });
    }

    private PnCounter getOrCreate(String name) {
        return counters.computeIfAbsent(name, k -> new PnCounter(memberId));
    }
}
