package org.ipoliakov.dmap.node.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.datastructures.ConcurrentOptimisticMap;
import org.ipoliakov.dmap.datastructures.offheap.map.OffHeapHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.protobuf.ByteString;

@Configuration
public class StorageConfig {

    @Value("${storage.inmemory.type}")
    private StorageType storageType;
    @Value("${storage.inmemory.initialCapacity:16}")
    private int initialCapacity;

    @Bean
    public Map<ByteString, ByteString> dataStorage() {
        return switch (storageType) {
            case CHM -> new ConcurrentHashMap<>(initialCapacity);
            case OFF_HEAP -> new OffHeapHashMap(initialCapacity);
            case OFF_HEAP_OPTIMISTIC -> new ConcurrentOptimisticMap<>(new OffHeapHashMap(initialCapacity));
        };
    }

    private enum StorageType {
        CHM,
        OFF_HEAP,
        OFF_HEAP_OPTIMISTIC
    }
}
