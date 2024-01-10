package org.ipoliakov.dmap.datastructures.crdt;

import org.ipoliakov.dmap.datastructures.VectorClock;

public interface CRDT<T extends CRDT<T>> {

    void merge(T other);

    VectorClock getCurrentVectorClock();
}
