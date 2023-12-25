package org.ipoliakov.dmap.util.concurrent;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;

import org.ipoliakov.dmap.common.IdGenerator;

/**
 * <p>Lock free implementation of twitter snowflake id generator.</p>
 * <p>The maximum of nodeId value is 1023</p>
 * <p>The maximum sequence value within 1 millisecond is 4095</p>
 */
public class LockFreeSnowflakeIdGenerator implements IdGenerator {

    private static final int NODE_ID_SHIFT = 10;
    private static final int SEQUENCE_SHIFT = 12;
    private static final int NODE_ID_AND_SEQUENCE_SHIFT = NODE_ID_SHIFT + SEQUENCE_SHIFT;
    private static final long MAX_SEQUENCE = (long) (Math.pow(2, SEQUENCE_SHIFT) - 1);

    private final Clock clock;
    private final long nodeBits;

    private final AtomicLong timestampSequence = new AtomicLong();

    public LockFreeSnowflakeIdGenerator(Clock clock, long nodeId) {
        this.clock = clock;
        this.nodeBits = nodeId << SEQUENCE_SHIFT;
        long maxNodeId = (long) (Math.pow(2, NODE_ID_SHIFT) - 1);
        if (nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException("nodeId(" + nodeId + ") must be between 0 and " + maxNodeId);
        }
    }

    @Override
    public long next() {
        while (true) {
            long oldTimestampSequence = timestampSequence.get();
            long timestampMs = clock.millis();
            long oldTimestampMs = oldTimestampSequence >>> NODE_ID_AND_SEQUENCE_SHIFT;

            if (timestampMs > oldTimestampMs) {
                long newTimestampSequence = timestampMs << NODE_ID_AND_SEQUENCE_SHIFT;
                if (timestampSequence.compareAndSet(oldTimestampSequence, newTimestampSequence)) {
                    return newTimestampSequence | nodeBits;
                }
            } else if (timestampMs == oldTimestampMs) {
                long oldSequence = oldTimestampSequence & MAX_SEQUENCE;
                if (oldSequence < MAX_SEQUENCE) {
                    long newTimestampSequence = oldTimestampSequence + 1;
                    if (timestampSequence.compareAndSet(oldTimestampSequence, newTimestampSequence)) {
                        return newTimestampSequence | nodeBits;
                    }
                }
            } else {
                throw new IllegalStateException(
                    "Clock moved backwards: timestampMs = " + timestampMs + " < oldTimestampMs = " + oldTimestampMs);
            }

            Thread.onSpinWait();
        }
    }
}
