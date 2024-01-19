package org.ipoliakov.dmap.datastructures.crdt.counters;

public record PnCounterState(Integer nodeId, long pCounter, long nCounter) { }
