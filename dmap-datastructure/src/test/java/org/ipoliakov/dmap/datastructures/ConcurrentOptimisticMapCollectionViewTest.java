package org.ipoliakov.dmap.datastructures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
public class ConcurrentOptimisticMapCollectionViewTest {

    @Test
    void isEmpty() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of()).values().isEmpty());
    }

    @Test
    void isEmpty_false() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).values().isEmpty());
    }

    @Test
    void contains() {
        assertTrue(new ConcurrentOptimisticMap<>(Map.of("k", "v")).values().contains("v"));
    }

    @Test
    void contains_notContains() {
        assertFalse(new ConcurrentOptimisticMap<>(Map.of("k", "v")).values().contains("not_existing_value"));
    }

    @Test
    void toArray() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));

        Object[] array1 = map.values().toArray();
        Object[] array2 = map.values().toArray(new Object[0]);
        Arrays.sort(array1);
        Arrays.sort(array2);

        assertArrayEquals(new Object[] {"v1", "v2"}, array1);
        assertArrayEquals(new Object[] {"v1", "v2"}, array2);
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
        Collection<String> values = map.values();

        assertTrue(values.containsAll(Set.of("v2", "v3")));
        assertFalse(values.containsAll(Set.of("v2", "v3", "not_existing_values")));
    }

    @Test
    void size() {
        assertEquals(1, new ConcurrentOptimisticMap<>(Map.of("k", "v")).values().size());
    }

    @Test
    void iterator() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));

        List<String> actualValues = new ArrayList<>();
        map.values().iterator().forEachRemaining(actualValues::add);
        Collections.sort(actualValues);

        assertEquals(List.of("v1", "v2"), actualValues);
    }

    @Test
    void iterator_next_hasNext() {
        var map = new ConcurrentOptimisticMap<>(
                Map.of(
                        "k1", "v1",
                        "k2", "v2"
                ));

        Iterator<String> iterator = map.values().iterator();

        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());

        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void toString_test() {
        Map<String, String> sourceMap = Map.of(
                "k1", "v1",
                "k2", "v2"
        );
        var map = new ConcurrentOptimisticMap<>(sourceMap);
        assertEquals(sourceMap.values().toString(), map.values().toString());
    }
}
