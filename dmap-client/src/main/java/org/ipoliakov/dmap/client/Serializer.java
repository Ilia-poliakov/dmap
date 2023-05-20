package org.ipoliakov.dmap.client;

public interface Serializer<T, R> {

    R serialize(T src);

    T dserialize(R dst);
}
