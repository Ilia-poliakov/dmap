package org.ipoliakov.dmap.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VectorClockTest {

    @Test
    void equals() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(1, 100);
        clock1.set(2, 100);
        clock1.set(3, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 100);
        clock2.set(2, 100);
        clock2.set(3, 100);

        assertEquals(clock1, clock2);
        assertEquals(clock1, clock1);
        assertEquals(clock1, new VectorClock(clock2));
        assertNotEquals(clock1, new Object());

        clock2.set(2, 200);
        assertNotEquals(clock1, clock2);
    }

    @Test
    void merge() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(2, 200);
        clock1.set(3, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 100);
        clock2.set(2, 100);

        VectorClock expected = new VectorClock(1);
        expected.set(1, 100);
        expected.set(2, 200);
        expected.set(3, 100);

        clock1.merge(clock2);
        assertEquals(expected, clock1);
    }

    @Test
    @DisplayName("isAfter - false when clocks are equals")
    void isAfter_falseOnEquals() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(1, 100);
        clock1.set(2, 100);
        clock1.set(3, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 100);
        clock2.set(2, 100);
        clock2.set(3, 100);

        assertFalse(clock1.isAfter(clock2));
    }

    @Test
    @DisplayName("isAfter - false when clock1 less than clock2")
    void isAfter_falseOnLessThan() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(1, 100);
        clock1.set(2, 100);
        clock1.set(3, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 200);
        clock2.set(2, 100);
        clock2.set(3, 100);

        assertFalse(clock1.isAfter(clock2));
    }

    @Test
    @DisplayName("isAfter - true clock1 has additional timestamps")
    void isAfter_trueOnLessThan() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(1, 100);
        clock1.set(2, 100);
        clock1.set(3, 100);
        clock1.set(4, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 100);
        clock2.set(2, 100);
        clock2.set(3, 100);

        assertTrue(clock1.isAfter(clock2));
    }

    @Test
    @DisplayName("isAfter - true when any timestamp is greater")
    void isAfter_trueWhenAnoGreater() {
        VectorClock clock1 = new VectorClock(1);
        clock1.set(1, 200);
        clock1.set(2, 100);
        clock1.set(3, 100);

        VectorClock clock2 = new VectorClock(1);
        clock2.set(1, 100);
        clock2.set(2, 100);
        clock2.set(3, 100);
        assertTrue(clock1.isAfter(clock2));
    }

    @Test
    void getNext() {
        VectorClock clock = new VectorClock(1);
        clock.set(1, 100);
        clock.set(2, 100);
        clock.set(3, 100);

        VectorClock expected = new VectorClock(1);
        expected.set(1, 100);
        expected.set(2, 100);
        expected.set(3, 100);

        long next = clock.getNext(1);
        assertEquals(101, next);
        assertEquals(expected, clock);
    }
}