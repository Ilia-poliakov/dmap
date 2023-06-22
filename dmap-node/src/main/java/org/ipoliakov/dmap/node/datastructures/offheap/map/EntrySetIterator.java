package org.ipoliakov.dmap.node.datastructures.offheap.map;

import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.KEY_SIZE_OFFSET;
import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.NEXT_ENTRY_OFFSET;
import static org.ipoliakov.dmap.node.datastructures.offheap.map.OffHeapHashMap.VALUE_SIZE_OFFSET;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntrySetIterator implements Iterator<Map.Entry<ByteString, ByteString>> {

    private final OffHeapHashMap map;

    private int i, currentPtr;

    @Override
    public boolean hasNext() {
        while (i < map.buf.capacity()) {
            byte entryOffset = map.buf.getByte(i * Integer.BYTES);
            if (entryOffset == 0) {
                i++;
                continue;
            }
            break;
        }
        return i < map.buf.capacity();
    }

    @Override
    public Map.Entry<ByteString, ByteString> next() {
        currentPtr = i * 4;
        int entry = map.buf.getIntLE(currentPtr);
        if (entry != 0) {
            int keySize = map.buf.getIntLE(entry + KEY_SIZE_OFFSET);
            int valueSize = map.buf.getIntLE(entry + VALUE_SIZE_OFFSET);

            byte[] key = new byte[keySize];
            byte[] value = new byte[valueSize];

            map.buf.getBytes(entry + VALUE_SIZE_OFFSET + Integer.BYTES, key);
            map.buf.getBytes(entry + VALUE_SIZE_OFFSET + keySize + Integer.BYTES, value);

            currentPtr = entry + NEXT_ENTRY_OFFSET;
            i = entry + VALUE_SIZE_OFFSET + keySize + Integer.BYTES + 1;
            return new AbstractMap.SimpleImmutableEntry<>(
                    ByteString.copyFrom(key),
                    ByteString.copyFrom(value)
            );
        }
        return null;
    }
}
