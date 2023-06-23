package org.ipoliakov.dmap.node.datastructures.offheap.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class EntrySetTest {

    @Test
    void size() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));

        int actualSize = map.entrySet().size();

        assertEquals(1, actualSize);
        assertEquals(map.size(), actualSize);
    }

    @Test
    void size_zeroOnEmptyMap() {
        OffHeapHashMap map = new OffHeapHashMap();

        int actualSize = map.entrySet().size();

        assertEquals(0, actualSize);
        assertEquals(map.size(), actualSize);
    }

    @Test
    void isEmpty() {
        OffHeapHashMap map = new OffHeapHashMap();

        boolean actualIsEmpty = map.entrySet().isEmpty();

        assertTrue(actualIsEmpty);
        assertTrue(map.isEmpty());
    }

    @Test
    void isEmpty_falseAfterPutToMap() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));

        boolean actualIsEmpty = map.entrySet().isEmpty();

        assertFalse(actualIsEmpty);
        assertFalse(map.isEmpty());
    }

    @Test
    void contains() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        assertTrue(map.entrySet().contains(new AbstractMap.SimpleImmutableEntry<>(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"))));
    }

    @Test
    void contains_falseIfNotEntry() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        assertFalse(map.entrySet().contains("key"));
    }

    @Test
    void iterator() {
        OffHeapHashMap map = new OffHeapHashMap();
        Iterator<Map.Entry<ByteString, ByteString>> iterator = map.entrySet().iterator();
        assertNotNull(iterator);
    }

    @Test
    void clear() {
        OffHeapHashMap map = new OffHeapHashMap();
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.entrySet().clear();
        assertTrue(map.isEmpty());
        assertTrue(map.entrySet().isEmpty());
        assertEquals(map.size(), map.entrySet().size());
    }

    @Test
    void add() {
        OffHeapHashMap map = new OffHeapHashMap();
        assertThrows(UnsupportedOperationException.class,
                () -> map.entrySet()
                        .add(new AbstractMap.SimpleImmutableEntry<>(
                                ByteString.copyFromUtf8("key"),
                                ByteString.copyFromUtf8("value")
                        )));
    }

    @Test
    void remove() {
        OffHeapHashMap map = new OffHeapHashMap();
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().remove("key"));
    }

    @Test
    void addAll() {
        OffHeapHashMap map = new OffHeapHashMap();
        assertThrows(UnsupportedOperationException.class,
                () -> map.entrySet()
                        .addAll(List.of(
                                    new AbstractMap.SimpleImmutableEntry<>(
                                            ByteString.copyFromUtf8("key"),
                                            ByteString.copyFromUtf8("value")
                                    )
                                )));
    }

    @Test
    void retainAll() {
        OffHeapHashMap map = new OffHeapHashMap();
        assertThrows(UnsupportedOperationException.class,
                () -> map.entrySet()
                       .retainAll(List.of(
                                    new AbstractMap.SimpleImmutableEntry<>(
                                            ByteString.copyFromUtf8("key"),
                                            ByteString.copyFromUtf8("value")
                                    )
                                )));
    }

    @Test
    void removeAll() {
        OffHeapHashMap map = new OffHeapHashMap();
        assertThrows(UnsupportedOperationException.class,
                () -> map.entrySet()
                      .removeAll(List.of(
                                    new AbstractMap.SimpleImmutableEntry<>(
                                            ByteString.copyFromUtf8("key"),
                                            ByteString.copyFromUtf8("value")
                                    )
                                )));
    }
}