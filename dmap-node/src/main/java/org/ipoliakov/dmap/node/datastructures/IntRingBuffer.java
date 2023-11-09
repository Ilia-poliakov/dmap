package org.ipoliakov.dmap.node.datastructures;

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

    private int getIndex(long sequence) {
        return (int) (sequence % elements.length);
    }

    @Override
    public String toString() {
        return "IntRingBuffer{"
                + "elements=" + Arrays.toString(elements)
                + '}';
    }
}
