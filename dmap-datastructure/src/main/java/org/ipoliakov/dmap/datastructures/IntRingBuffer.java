package org.ipoliakov.dmap.datastructures;

import java.util.Arrays;

import lombok.Getter;

public class IntRingBuffer {

    private final int[] elements;
    @Getter
    private volatile long tailSequence = -1, headSequence;

    public IntRingBuffer(int capacity) {
        this.elements = new int[capacity];
    }

    public void add(int elem) {
        tailSequence++;
        if (tailSequence - elements.length == headSequence) {
            headSequence++;
        }
        elements[getIndex(tailSequence)] = elem;
    }

    public int getCapacity() {
        return elements.length;
    }

    public int get(long sequence) {
        return elements[getIndex(sequence)];
    }

    public int getLast() {
        return get(tailSequence);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public long size() {
        return tailSequence - headSequence + 1;
    }

    private int getIndex(long sequence) {
        return (int) (sequence % elements.length);
    }

    public void clear() {
        Arrays.fill(elements, 0);
        tailSequence = -1;
        headSequence = tailSequence + 1;
    }

    @Override
    public String toString() {
        return "IntRingBuffer{"
                + "elements=" + Arrays.toString(elements)
                + '}';
    }
}
