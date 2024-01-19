package org.ipoliakov.dmap.datastructures.crdt;

import org.ipoliakov.dmap.datastructures.VectorClock;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface CRDT<T extends CRDT<T>> {

    void merge(T other);

    VectorClock getCurrentVectorClock();
}
