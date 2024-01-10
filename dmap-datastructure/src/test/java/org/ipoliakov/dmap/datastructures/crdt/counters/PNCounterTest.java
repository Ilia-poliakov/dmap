package org.ipoliakov.dmap.datastructures.crdt.counters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PNCounterTest {

    @Test
    void merge() {
        PNCounter counter1 = new PNCounter(1);
        counter1.addAndGet(100, new VectorClock(1));

        PNCounter counter2 = new PNCounter(1);
        counter2.addAndGet(200, new VectorClock(1));

        counter1.merge(counter2);

        assertEquals(new StampedLong(200, counter1.getCurrentVectorClock()), counter1.get(new VectorClock(1)));
    }

    @Test
    void merge_equalClocks() {
        PNCounter counter1 = new PNCounter(1);
        counter1.addAndGet(100, new VectorClock(1));

        PNCounter counter2 = new PNCounter(1);
        counter2.addAndGet(100, new VectorClock(1));

        counter1.merge(counter2);

        assertEquals(new StampedLong(100, counter1.getCurrentVectorClock()), counter1.get(new VectorClock(1)));
    }

    @Test
    void merge_greaterWithLess() {
        PNCounter counter1 = new PNCounter(1);
        counter1.addAndGet(200, new VectorClock(1));

        PNCounter counter2 = new PNCounter(1);
        counter2.addAndGet(100, new VectorClock(1));

        counter1.merge(counter2);
        assertEquals(new StampedLong(200, counter1.getCurrentVectorClock()), counter1.get(new VectorClock(1)));
    }

    @Test
    void addAndGet() {
        PNCounter counter = new PNCounter(1);
        StampedLong actual = counter.addAndGet(100, new VectorClock(1));
        assertEquals(new StampedLong(100, counter.getCurrentVectorClock()), actual);

        actual = counter.addAndGet(100, new VectorClock(1));
        assertEquals(new StampedLong(200, counter.getCurrentVectorClock()), actual);

        actual = counter.addAndGet(-50, new VectorClock(1));
        assertEquals(new StampedLong(150, counter.getCurrentVectorClock()), actual);

        VectorClock clockInFuture = new VectorClock(1);
        clockInFuture.set(1, counter.getCurrentVectorClock().getNext(1));
        assertThrows(IllegalStateException.class, () -> counter.addAndGet(1, clockInFuture));
    }

    @Test
    void subtractAndGet() {
        PNCounter counter = new PNCounter(1);
        StampedLong actual = counter.subtractAndGet(100, new VectorClock(1));
        assertEquals(new StampedLong(-100, counter.getCurrentVectorClock()), actual);

        actual = counter.subtractAndGet(100, new VectorClock(1));
        assertEquals(new StampedLong(-200, counter.getCurrentVectorClock()), actual);

        actual = counter.subtractAndGet(-50, new VectorClock(1));
        assertEquals(new StampedLong(-150, counter.getCurrentVectorClock()), actual);

        VectorClock clockInFuture = new VectorClock(1);
        clockInFuture.set(1, counter.getCurrentVectorClock().getNext(1));
        assertThrows(IllegalStateException.class, () -> counter.subtractAndGet(1, clockInFuture));
    }

    @Test
    void getCurrentVectorClock_shouldReturnCopyOfClock() {
        PNCounter counter = new PNCounter(1);
        VectorClock currentVectorClock = counter.getCurrentVectorClock();
        Object internalClock = ReflectionTestUtils.getField(counter, "vectorClock");
        assertNotSame(currentVectorClock, internalClock);
    }
}