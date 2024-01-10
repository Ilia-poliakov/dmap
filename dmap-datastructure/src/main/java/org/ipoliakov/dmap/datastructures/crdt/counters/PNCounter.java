package org.ipoliakov.dmap.datastructures.crdt.counters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.CRDT;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;

public class PNCounter implements CRDT<PNCounter> {

    private static final int ADDITION_INDEX = 0;
    private static final int SUBTRACT_INDEX = 1;

    private final int nodeId;
    private final VectorClock vectorClock;
    private final Map<Integer, long[]> state = new ConcurrentHashMap<>();

    public PNCounter(int nodeId) {
        this.nodeId = nodeId;
        this.vectorClock = new VectorClock(nodeId);
    }

    @Override
    public void merge(PNCounter other) {
        other.state.forEach((nodeId, pnOtherValues) -> {
            long[] newPNValues = trimToZeroValues(this.state.get(nodeId));
            newPNValues[0] = Math.max(newPNValues[0], pnOtherValues[0]);
            newPNValues[1] = Math.max(newPNValues[1], pnOtherValues[1]);
            this.state.put(nodeId, newPNValues);
        });
        this.vectorClock.merge(other.vectorClock);
    }

    @Override
    public VectorClock getCurrentVectorClock() {
        return new VectorClock(this.vectorClock);
    }

    public StampedLong get(VectorClock timestamp) {
        validateConsistency(timestamp);
        long value = 0;
        for (long[] pnValue : state.values()) {
            value += pnValue[0];
            value -= pnValue[1];
        }
        return new StampedLong(value, new VectorClock(vectorClock));
    }

    public StampedLong addAndGet(long delta, VectorClock timestamp) {
        validateConsistency(timestamp);
        if (delta < 0) {
            return subtractAndGet(-delta, timestamp);
        }
        return updateAndGet(delta, timestamp, ADDITION_INDEX);
    }

    public StampedLong subtractAndGet(long delta, VectorClock timestamp) {
        validateConsistency(timestamp);
        if (delta < 0) {
            return addAndGet(-delta, timestamp);
        }
        return updateAndGet(delta, timestamp, SUBTRACT_INDEX);
    }

    private void validateConsistency(VectorClock lastReadTimestamp) {
        if (lastReadTimestamp.isAfter(this.vectorClock)) {
            throw new IllegalStateException("State of this node is stale");
        }
    }

    private StampedLong updateAndGet(long delta, VectorClock timestamp, int index) {
        long nextTimestamp = this.vectorClock.getNext(nodeId);
        this.state.compute(nodeId, (nodeIdKey, pnValues) -> {
            long[] pnValuesForUpdate = trimToZeroValues(pnValues);
            pnValuesForUpdate[index] += delta;
            return pnValuesForUpdate;
        });
        this.vectorClock.set(nodeId, nextTimestamp);
        return get(timestamp);
    }

    private long[] trimToZeroValues(long[] pnValues) {
        return pnValues != null ? pnValues : new long[2];
    }
}
