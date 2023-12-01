package org.ipoliakov.dmap.node.datastructures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class ConcurrentOptimisticMapSetViewTest {

    @Test
    void iterator() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));

        List<String> actualKeys = new ArrayList<>();
        map.keySet().iterator().forEachRemaining(actualKeys::add);
        Collections.sort(actualKeys);

        assertEquals(List.of("k1", "k2"), actualKeys);
    }

    @Test
    void toArray() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));

        Object[] array1 = map.keySet().toArray();
        Object[] array2 = map.keySet().toArray(new Object[0]);
        Arrays.sort(array1);
        Arrays.sort(array2);

        assertArrayEquals(new Object[] {"k1", "k2"}, array1);
        assertArrayEquals(new Object[] {"k1", "k2"}, array2);
    }

    @Test
    void remove() {
        var map = new ConcurrentOptimisticMap<>(
                new HashMap<>(
                        Map.of(
                                "k1", "v1",
                                "k2", "v2"
                        )));
        Set<String> keySet = map.keySet();

        keySet.remove("k2");

        assertEquals(1, map.size());
        assertEquals(1, keySet.size());
        assertTrue(map.containsKey("k1"));
        assertTrue(keySet.contains("k1"));
    }

    @Test
    void containsAll() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2",
                        "k3", "v3",
                        "k4", "v4"
                ));
        Set<String> keySet = map.keySet();

        assertTrue(keySet.containsAll(Set.of("k2", "k3")));
        assertFalse(keySet.containsAll(Set.of("k2", "k3", "not_existing_key")));
    }

    @Test
    void size() {
        assertEquals(1, new ConcurrentOptimisticMap<>(Map.of("k", "v")).keySet().size());
    }

    @Test
    void size_zero() {
        assertEquals(0, new ConcurrentOptimisticMap<>(Map.of()).keySet().size());
    }

    @Test
    void isEmpty() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of()).keySet().isEmpty());
    }

    @Test
    void isEmpty_false() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).keySet().isEmpty());
    }

    @Test
    void contains() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of("k", "v")).keySet().contains("k"));
    }

    @Test
    void contains_notContains() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).keySet().contains("not_existing_key"));
    }

    @Test
    void clear() {
        var map = new ConcurrentOptimisticMap<>(
                new HashMap<>(
                        Map.of(
                                "k1", "v1",
                                "k2", "v2"
                        )));
        Set<String> keySet = map.keySet();

        keySet.clear();

        assertTrue(map.isEmpty());
        assertTrue(keySet.isEmpty());
    }
}