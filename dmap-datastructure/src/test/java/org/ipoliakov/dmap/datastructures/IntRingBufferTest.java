package org.ipoliakov.dmap.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntRingBufferTest {

    @Test
    void add() {
        IntRingBuffer buffer = fullBuffer();
        assertEquals(5, buffer.getCapacity());
    }

    @Test
    void add_Overflow() {
        IntRingBuffer buffer = fullBuffer();
        buffer.add(100);
        assertEquals(5, buffer.getCapacity());
        assertEquals(1, buffer.getHeadSequence());
        assertEquals(5, buffer.getTailSequence());
    }

    @Test
    void get_bySequence() {
        IntRingBuffer buffer = fullBuffer();
        buffer.add(200);
        assertEquals(200, buffer.get(buffer.getHeadSequence() - 1));
    }

    @Test
    void getLast() {
        IntRingBuffer buffer = fullBuffer();
        buffer.add(100);
        assertEquals(100, buffer.getLast());
    }

    @Test
    void isEmpty_true() {
        assertTrue(new IntRingBuffer(1).isEmpty());
    }

    @Test
    void isEmpty_false() {
        assertFalse(fullBuffer().isEmpty());
    }

    @Test
    void size() {
        assertEquals(5, fullBuffer().size());
    }

    @Test
    void size_zeroOnEmptyBuffer() {
        assertEquals(0, new IntRingBuffer(1).size());
    }

    @Test
    void clear() {
        IntRingBuffer buffer = fullBuffer();
        buffer.clear();
        assertEquals(0, buffer.size());
        assertTrue(buffer.isEmpty());
    }

    private static IntRingBuffer fullBuffer() {
        IntRingBuffer buffer = new IntRingBuffer(5);
        for (int i = 0; i < buffer.getCapacity(); i++) {
            buffer.add(100);
        }
        return buffer;
    }
}