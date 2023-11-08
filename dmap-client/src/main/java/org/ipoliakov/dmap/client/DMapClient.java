package org.ipoliakov.dmap.client;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

public interface DMapClient<K extends Serializable, V extends Serializable> {

    CompletableFuture<V> get(K key);

    CompletableFuture<V> put(K key, V value);

    CompletableFuture<V> remove(K key, V value);
}
