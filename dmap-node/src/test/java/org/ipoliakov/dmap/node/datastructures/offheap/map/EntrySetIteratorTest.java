package org.ipoliakov.dmap.node.datastructures.offheap.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class EntrySetIteratorTest {

    @Test
    void next() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));
        map.put(ByteString.copyFromUtf8("key3"), ByteString.copyFromUtf8("value3"));

        Iterator<Map.Entry<ByteString, ByteString>> iterator = map.entrySet().iterator();
        Set<Map.Entry<ByteString, ByteString>> actual = new HashSet<>();
        iterator.forEachRemaining(actual::add);

        var expected = Set.of(
            new AbstractMap.SimpleImmutableEntry<>(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value")),
            new AbstractMap.SimpleImmutableEntry<>(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2")),
            new AbstractMap.SimpleImmutableEntry<>(ByteString.copyFromUtf8("key3"), ByteString.copyFromUtf8("value3"))
        );
        assertEquals(expected, actual);
    }

    @Test
    void next_noSuchElementException_onEmpty() {
        OffHeapHashMap map = new OffHeapHashMap();
        Iterator<Map.Entry<ByteString, ByteString>> iterator = map.entrySet().iterator();
        assertThrows(IllegalStateException.class, iterator::next);
    }
}
