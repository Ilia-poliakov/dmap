package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.protocol.PnCounterSnapshot;

public interface DMapClient<K extends Serializable, V extends Serializable> {

    static ClientBuilder builder() {
        return new ClientBuilder();
    }

    CompletableFuture<V> get(K key);

    CompletableFuture<V> put(K key, V value);

    CompletableFuture<V> remove(K key, V value);

    CompletableFuture<PnCounterSnapshot> getCounterValue(String name, Map<Integer, Long> lastObservedTimestamp);

    CompletableFuture<PnCounterSnapshot> addAndGetCounter(String name, long delta, Map<Integer, Long> lastObservedTimestamp);
}
