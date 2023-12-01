package org.ipoliakov.dmap.datastructures.offheap.map;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntrySetIterator implements Iterator<Map.Entry<ByteString, ByteString>> {

    private final OffHeapHashMap map;

    private int currentPtr;

    @Override
    public boolean hasNext() {
        while ((currentPtr = map.buf.readIntLE()) == 0);
        return currentPtr < map.buf.capacity();
    }

    @Override
    public Map.Entry<ByteString, ByteString> next() {
        if (currentPtr == 0) {
            throw new IllegalStateException("You have to call hasNext() before");
        }
        int keySize = map.buf.getIntLE(currentPtr + OffHeapHashMap.KEY_SIZE_OFFSET);
        int valueSize = map.buf.getIntLE(currentPtr + OffHeapHashMap.VALUE_SIZE_OFFSET);

        byte[] key = new byte[keySize];
        byte[] value = new byte[valueSize];

        map.buf.getBytes(currentPtr + OffHeapHashMap.VALUE_SIZE_OFFSET + Integer.BYTES, key);
        map.buf.getBytes(currentPtr + OffHeapHashMap.VALUE_SIZE_OFFSET + keySize + Integer.BYTES, value);

        currentPtr += OffHeapHashMap.NEXT_ENTRY_OFFSET;
        return new AbstractMap.SimpleImmutableEntry<>(
                ByteString.copyFrom(key),
                ByteString.copyFrom(value)
        );
    }
}
