package org.ipoliakov.dmap.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ConcurrentOptimisticMapTest {

    @Test
    void size() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));
        assertEquals(2, map.size());
    }

    @Test
    void isEmpty() {
        assertTrue(new ConcurrentOptimisticMap<>(Collections.emptyMap()).isEmpty());
    }

    @Test
    void isEmpty_false() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).isEmpty());
    }

    @Test
    void containsKey() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of("k", "v")).containsKey("k"));
    }

    @Test
    void containsKey_false() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).containsKey("not_existing_key"));
    }

    @Test
    void containsValue() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of("k", "v")).containsValue("v"));
    }

    @Test
    void containsValue_false() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).containsValue("not_existing_value"));
    }

    @Test
    void get() {
        assertEquals("v", new ConcurrentOptimisticMap<>(Map.of("k", "v")).get("k"));
    }

    @Test
    void put() {
        HashMap<String, String> originalMap = new HashMap<>();
        ConcurrentOptimisticMap<String, String> map = new ConcurrentOptimisticMap<>(originalMap);

        map.put("k", "v");

        assertTrue(map.containsKey("k"));
        assertTrue(originalMap.containsKey("k"));
    }

    @Test
    void putIfAbsent_absent() {
        HashMap<String, String> originalMap = new HashMap<>();
        ConcurrentOptimisticMap<String, String> map = new ConcurrentOptimisticMap<>(originalMap);

        assertNull(map.putIfAbsent("k", "v"));

        assertTrue(map.containsKey("k"));
        assertTrue(originalMap.containsKey("k"));
    }

    @Test
    void putIfAbsent_exists() {
        HashMap<String, String> originalMap = new HashMap<>();
        ConcurrentOptimisticMap<String, String> map = new ConcurrentOptimisticMap<>(originalMap);

        map.put("k", "v");
        assertEquals("v", map.putIfAbsent("k", "v1"));

        assertTrue(map.containsKey("k"));
        assertTrue(originalMap.containsKey("k"));
        assertTrue(originalMap.containsValue("v"));
        assertTrue(originalMap.containsValue("v"));
        assertFalse(map.containsValue("v1"));
        assertFalse(originalMap.containsValue("v1"));
    }

    @Test
    void remove() {
        HashMap<String, String> originalMap = new HashMap<>();
        originalMap.put("k", "v");
        ConcurrentOptimisticMap<String, String> map = new ConcurrentOptimisticMap<>(originalMap);
        assertEquals("v", map.remove("k"));
        assertFalse(map.containsKey("k"));
        assertFalse(map.containsValue("v"));
    }

    @Test
    void remove_KV() {
        HashMap<String, String> originalMap = new HashMap<>();
        originalMap.put("k", "v");
        ConcurrentOptimisticMap<String, String> map = new ConcurrentOptimisticMap<>(originalMap);
        assertTrue(map.remove("k", "v"));
        assertFalse(map.remove("k", "v"));
        assertFalse(map.containsKey("k"));
        assertFalse(map.containsValue("v"));
    }

    @Test
    void putAll() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        Map<String, String> expected = Map.of(
                "k1", "v1",
                "k2", "v2"
        );
        map.putAll(expected);
        assertEquals(expected, map);
    }

    @Test
    void clear() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        map.put("k", "v");

        map.clear();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    void replace() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        map.put("k", "v");

        map.replace("k", "v2");

        assertEquals("v2", map.get("k"));
    }

    @Test
    void replace_NothingToReplace() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        map.put("k", "v");

        map.replace("k1", "v1");

        assertFalse(map.containsKey("k1"));
        assertFalse(map.containsValue("v1"));
    }

    @Test
    void replace_withValue() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        map.put("k", "v");

        map.replace("k", "v", "new_value");

        assertEquals(Map.of("k", "new_value"), map);
    }

    @Test
    void replace_withValue_nothingToReplace() {
        Map<String, String> map = new ConcurrentOptimisticMap<>(new HashMap<>());
        map.put("k", "v");
        map.put("k_null", null);

        map.replace("k", "v1", "new_value");
        map.replace("k_null", "v_null", "new_value");
        map.replace("k_null", null, null);

        var expected = new HashMap<>();
        expected.put("k", "v");
        expected.put("k_null", null);
        assertEquals(expected, map);
    }

    @Test
    void entrySet() {
        var entries = new ConcurrentOptimisticMap<>(Map.of("k", "v")).entrySet();
        assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("k", "v")));
    }

    @Test
    void equals_test() {
        var mapA = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));
        var mapB = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));
        assertEquals(mapA, mapB);
    }

    @Test
    void equals_notEquals() {
        var mapA = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));
        var mapB = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1"
                ));
        assertNotEquals(mapA, mapB);
    }

    @Test
    void hashCode_test() {
        Map<String, String> sourceMap = Map.of(
                "k1", "v1",
                "k2", "v2"
        );
        var map = new ConcurrentOptimisticMap<>(sourceMap);
        assertEquals(sourceMap.hashCode(), map.hashCode());
    }

    @Test
    void toStringTest() {
        Map<String, String> sourceMap = Map.of(
                "k1", "v1",
                "k2", "v2"
        );
        var map = new ConcurrentOptimisticMap<>(sourceMap);
        assertEquals(sourceMap.toString(), map.toString());
    }
}