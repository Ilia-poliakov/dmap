package org.ipoliakov.dmap.client;

import java.io.Serializable;

public interface DMapClient<K extends Serializable, V extends Serializable> {

    V get(K key);

    void put(K key, V value);
}
