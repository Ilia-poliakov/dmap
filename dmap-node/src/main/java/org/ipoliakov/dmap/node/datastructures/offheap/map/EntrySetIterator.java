package org.ipoliakov.dmap.node.datastructures.offheap.map;

import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.KEY_SIZE_OFFSET;
import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.NEXT_ENTRY_OFFSET;
import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.VALUE_SIZE_OFFSET;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntrySetIterator implements Iterator<Map.Entry<ByteString, ByteString>> {

    private final OffHeapHashMap map;

    private int currentPtr;

    @Override
    public boolean hasNext() {
        while ((currentPtr = map.buf.readIntLE()) == 0) { }
        return currentPtr < map.buf.capacity();
    }

    @Override
    public Map.Entry<ByteString, ByteString> next() {
        if (currentPtr == 0) {
            throw new NoSuchElementException();
        }
        int keySize = map.buf.getIntLE(currentPtr + KEY_SIZE_OFFSET);
        int valueSize = map.buf.getIntLE(currentPtr + VALUE_SIZE_OFFSET);

        byte[] key = new byte[keySize];
        byte[] value = new byte[valueSize];

        map.buf.getBytes(currentPtr + VALUE_SIZE_OFFSET + Integer.BYTES, key);
        map.buf.getBytes(currentPtr + VALUE_SIZE_OFFSET + keySize + Integer.BYTES, value);

        currentPtr += NEXT_ENTRY_OFFSET;
        return new AbstractMap.SimpleImmutableEntry<>(
                ByteString.copyFrom(key),
                ByteString.copyFrom(value)
        );
    }
}
