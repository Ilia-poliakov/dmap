package org.ipoliakov.dmap.node.datastructures.offheap.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class EntrySet extends AbstractSet<Map.Entry<ByteString, ByteString>> {

    private final OffHeapHashMap map;

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        ByteString k = ((Map.Entry<ByteString, ByteString>) o).getKey();
        ByteString v = ((Map.Entry<ByteString, ByteString>) o).getValue();
        ByteString mapVal = map.get(k);
        return Objects.equals(v, mapVal);
    }

    @Override
    public Iterator<Map.Entry<ByteString, ByteString>> iterator() {
        return new EntrySetIterator(map);
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<ByteString, ByteString>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Map.Entry<ByteString, ByteString> byteStringByteStringEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
