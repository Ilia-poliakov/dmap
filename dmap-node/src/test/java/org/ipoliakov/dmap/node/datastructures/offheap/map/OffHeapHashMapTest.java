package org.ipoliakov.dmap.node.datastructures.offheap.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
    void get() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("value"), value);
        assertEquals(2, map.size());
    }

    @Test
    void put() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("value"), value);
        assertEquals(1, map.size());
    }

    @Test
    void put_overwriteSameSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueOld"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueNew"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("valueNew"), value);
        assertEquals(1, map.size());
    }

    @Test
    void put_overwriteLessSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("longValue"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueNew"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("valueNew"), value);
        assertEquals(1, map.size());
    }

    @Test
    void put_overwriteBiggerSizedValue() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("valueOld"));
        map.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("longValue"));
        ByteString value = map.get(ByteString.copyFromUtf8("key"));
        assertEquals(ByteString.copyFromUtf8("longValue"), value);
        assertEquals(1, map.size());
    }

    @Test
    void remove() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        ByteString key = ByteString.copyFromUtf8("key");
        map.put(key, ByteString.copyFromUtf8("value"));

        ByteString removed = map.remove(key);

        assertEquals(ByteString.copyFromUtf8("value"), removed);
        assertEquals(0, map.size());
        assertNull(map.get(key));
        assertTrue(map.isEmpty());
    }

    @Test
    void remove_fromEmpty_return_null() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        assertNull(map.remove(ByteString.copyFromUtf8("key")));
    }

    @Test
    void clear() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key1"), ByteString.copyFromUtf8("value1"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));

        map.clear();

        assertNull(map.get(ByteString.copyFromUtf8("key1")));
        assertNull(map.get(ByteString.copyFromUtf8("key2")));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    void clear_addAfterClear() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key1"), ByteString.copyFromUtf8("value1"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));

        map.clear();
        map.put(ByteString.copyFromUtf8("key3"), ByteString.copyFromUtf8("value3"));

        assertEquals(1, map.size());
    }

    @Test
    void clear_doubleClearWithoutErrors() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key1"), ByteString.copyFromUtf8("value1"));

        map.clear();
        map.clear();

        assertTrue(map.isEmpty());
    }

    @Test
    void entrySet() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key1"), ByteString.copyFromUtf8("value1"));
        map.put(ByteString.copyFromUtf8("key2"), ByteString.copyFromUtf8("value2"));
        map.put(ByteString.copyFromUtf8("key3"), ByteString.copyFromUtf8("value3"));

        Set<Map.Entry<ByteString, ByteString>> entrySet = map.entrySet();

        assertEquals(3, entrySet.size());
    }

    @Test
    void entrySet_returnSameSet_ifExist() {
        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(ByteString.copyFromUtf8("key1"), ByteString.copyFromUtf8("value1"));

        Set<Map.Entry<ByteString, ByteString>> entrySet1 = map.entrySet();
        Set<Map.Entry<ByteString, ByteString>> entrySet2 = map.entrySet();

        assertTrue(entrySet1 == entrySet2, "Should be reference equals");
    }

    @Test
    void get_doesNotKeysEqualsWithDifferentSizes() {
        ByteString key = ByteString.copyFromUtf8("key");
        ByteString key2 = ByteString.copyFromUtf8("key2");

        ReflectionTestUtils.setField(key, "hash", 42);
        ReflectionTestUtils.setField(key2, "hash", 42);

        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(key, ByteString.copyFromUtf8("value"));

        ByteString val = map.get(key2);
        assertNull(val);
    }

    @Test
    void get_equalsKeySizes_butDiffByte() {
        ByteString key = ByteString.copyFromUtf8("key");
        ByteString key2 = ByteString.copyFromUtf8("keY");

        ReflectionTestUtils.setField(key, "hash", 42);
        ReflectionTestUtils.setField(key2, "hash", 42);

        Map<ByteString, ByteString> map = new OffHeapHashMap(16);
        map.put(key, ByteString.copyFromUtf8("value"));

        ByteString val = map.get(key2);
        assertNull(val);
    }
}