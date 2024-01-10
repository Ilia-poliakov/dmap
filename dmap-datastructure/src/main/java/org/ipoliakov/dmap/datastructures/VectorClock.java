package org.ipoliakov.dmap.datastructures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class VectorClock {

    private final Map<Integer, Long> timestampsByNodes = new ConcurrentHashMap<>();

    public VectorClock(int nodeId) {
        this.timestampsByNodes.put(nodeId, Long.MIN_VALUE);
    }

    public VectorClock(VectorClock from) {
        this.timestampsByNodes.putAll(from.timestampsByNodes);
    }

    public void merge(VectorClock other) {
        other.timestampsByNodes.forEach((nodeId, newTimestamp) ->
            this.timestampsByNodes.merge(nodeId, Long.MIN_VALUE, (oldTimestamp, t2) -> Math.max(newTimestamp, oldTimestamp)));
    }

    public void set(int nodeId, long timestamp) {
        this.timestampsByNodes.put(nodeId, timestamp);
    }

    public long getNext(int nodeId) {
        return timestampsByNodes.get(nodeId) + 1;
    }

    public boolean isAfter(VectorClock other) {
        boolean anyIsAfter = false;
        for (Map.Entry<Integer, Long> otherEntry : other.timestampsByNodes.entrySet()) {
            Long localTimestamp = timestampsByNodes.getOrDefault(otherEntry.getKey(), Long.MIN_VALUE);
            if (localTimestamp < otherEntry.getValue()) {
                return false;
            }
            if (localTimestamp > otherEntry.getValue()) {
                anyIsAfter = true;
            }
        }
        return anyIsAfter || other.timestampsByNodes.size() < this.timestampsByNodes.size();
    }
}
