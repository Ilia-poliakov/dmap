package org.ipoliakov.dmap.node.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static IntRingBuffer fullBuffer() {
        IntRingBuffer buffer = new IntRingBuffer(5);
        for (int i = 0; i < buffer.getCapacity(); i++) {
            buffer.add(100);
        }
        return buffer;
    }
}