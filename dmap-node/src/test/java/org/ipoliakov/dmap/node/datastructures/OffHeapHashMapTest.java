package org.ipoliakov.dmap.node.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class OffHeapHashMapTest {

    @Test
    void size_zeroAfterCreation() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        assertEquals(map.size(), 0);
    }

    @Test
    void size_incrementAfterPut() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        assertEquals(map.size(), 1);
    }

    @Test
    void size_decrementAfterRemove() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.remove(ByteString.copyFromUtf8("key"));
        assertEquals(map.size(), 0);
    }

    @Test
    void isEmpty() {
    }

    @Test
    void containsKey() {
    }

    @Test
    void containsValue() {
    }

    @Test
    void get() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(value, ByteString.copyFromUtf8("value"));
    }

    @Test
    void put() {

    }

    @Test
    void remove() {
    }

    @Test
    void putAll() {
    }

    @Test
    void clear() {
    }

    @Test
    void keySet() {
    }

    @Test
    void values() {
    }

    @Test
    void entrySet() {
    }
}