package org.ipoliakov.dmap.node.internal.cluster.crdt;

import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.datastructures.crdt.counters.PNCounter;
import org.ipoliakov.dmap.node.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PNCounterService {

    private final ConcurrentHashMap<String, PNCounter> counters = new ConcurrentHashMap<>();

    @Value("${MEMBER_ID:${member.id:}}")
    private final int memberId;

    public StampedLong get(String name) {
        PNCounter counter = counters.get(name);
        if (counter == null) {
            throw new NotFoundException(PNCounter.class, "name = " + name);
        }
        return counter.get(new VectorClock(memberId));
    }
}
