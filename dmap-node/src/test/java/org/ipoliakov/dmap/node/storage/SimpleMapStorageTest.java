package org.ipoliakov.dmap.node.storage;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class SimpleMapStorageTest {

    private SimpleMapStorage storage;

    @BeforeEach
    void setUp() {
        storage = new SimpleMapStorage();
    }

    @Test
    void putGet() {
        ByteString key = ByteString.copyFrom("key", UTF_8);
        ByteString expected = ByteString.copyFrom("value", UTF_8);

        ByteString prev = storage.put(key, expected);
        ByteString actual = storage.get(ByteString.copyFrom(key.toByteArray()));

        assertNull(prev);
        assertEquals(expected, actual);
    }

    @Test
    void putRewriteValue() {
        ByteString key = ByteString.copyFrom("key", UTF_8);
        ByteString value1 = ByteString.copyFrom("value1", UTF_8);
        storage.put(key, value1);

        key = ByteString.copyFrom("key", UTF_8);
        ByteString value2 = ByteString.copyFrom("value2", UTF_8);
        ByteString prev = storage.put(key, value2);

        assertEquals(value1, prev);
    }
}