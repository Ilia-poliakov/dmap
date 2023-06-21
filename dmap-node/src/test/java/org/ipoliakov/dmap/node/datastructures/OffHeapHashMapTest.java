package org.ipoliakov.dmap.node.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class OffHeapHashMapTest {

    @Test
    void size_zeroAfterCreation() {
        assertEquals(0, new OffHeapHashMap(16).size());
    }

    @Test
    void size_incrementAfterPut() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        assertEquals(1, map.size());
    }

    @Test
    void size_decrementAfterRemove() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.remove(ByteString.copyFromUtf8("key"));
        assertEquals(0, map.size());
    }

    @Test
    void isEmpty() {
        assertTrue(new OffHeapHashMap(16).isEmpty());
    }

    @Test
    void isEmpty_falseOnNotE() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        assertFalse(map.isEmpty());
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
        assertEquals(ByteString.copyFromUtf8("value"), value);
        assertEquals(map.size(), 2);
    }

    @Test
    void put() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("value"), value);
        assertEquals(map.size(), 1);
    }

    @Test
    void put_overwriteSameSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueOld"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueNew"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("valueNew"), value);
        assertEquals(map.size(), 1);
    }

    @Test
    void put_overwriteLessSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("longValue"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueNew"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("valueNew"), value);
        assertEquals(map.size(), 1);
    }

    @Test
    void put_overwriteBiggerSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueOld"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("longValue"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("longValue"), value);
        assertEquals(map.size(), 1);
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