package org.ipoliakov.dmap.common;

import java.util.concurrent.atomic.AtomicLong;

public class MonotonicallyIdGenerator implements IdGenerator {

    private final AtomicLong sequence;

    public MonotonicallyIdGenerator() {
        this(0);
    }

    public MonotonicallyIdGenerator(long startValue) {
        sequence = new AtomicLong(startValue);
    }

    @Override
    public long next() {
        return sequence.getAndIncrement();
    }
}
