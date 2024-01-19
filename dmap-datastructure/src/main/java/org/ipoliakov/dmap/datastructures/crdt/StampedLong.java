package org.ipoliakov.dmap.datastructures.crdt;

import org.ipoliakov.dmap.datastructures.VectorClock;

public record StampedLong(long value, VectorClock timestamp) { }
