package org.ipoliakov.dmap.datastructures.crdt.counters;

import java.util.List;

import org.ipoliakov.dmap.datastructures.VectorClock;

public record PnCounterSnapshot(Integer nodeId, List<PnCounterState> states, VectorClock clock) { }
