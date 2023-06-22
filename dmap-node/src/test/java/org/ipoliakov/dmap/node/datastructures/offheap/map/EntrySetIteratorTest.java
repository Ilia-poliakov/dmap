package org.ipoliakov.dmap.node.datastructures.offheap.map;

import java.util.Iterator;
import java.util.Map;

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
        iterator.forEachRemaining(System.out::println);
    }
}