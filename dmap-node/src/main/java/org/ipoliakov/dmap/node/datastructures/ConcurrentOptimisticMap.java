package org.ipoliakov.dmap.node.datastructures;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.ipoliakov.dmap.node.util.concurrent.lock.DmapStampedLock;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcurrentOptimisticMap<K, V> implements ConcurrentMap<K, V> {

    private final Map<K, V> map;
    private final DmapStampedLock lock = new DmapStampedLock();

    @Override
    public int size() {
        return lock.optimisticRead(map::size);
    }

    @Override
    public boolean isEmpty() {
        return lock.optimisticRead(map::isEmpty);
    }

    @Override
    public boolean containsKey(Object key) {
        return lock.optimisticRead(() -> map.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return lock.optimisticRead(() -> map.containsValue(value));
    }

    @Override
    public V get(Object key) {
        return lock.optimisticRead(() -> map.get(key));
    }

    @Override
    public V put(K key, V value) {
        return lock.writeLocked(() -> map.put(key, value));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        long stamp = lock.readLock();
        try {
            V v = map.get(key);
            if (v != null) {
                return v;
            }
            stamp = lock.toWriteLock(stamp);
            return map.put(key, value);
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public V remove(Object key) {
        return lock.writeLocked(() -> map.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        lock.writeLocked(() -> {
            map.putAll(m);
            return null;
        });
    }

    @Override
    public void clear() {
       lock.writeLocked(() -> {
            map.clear();
            return null;
        });
    }

    @Override
    public boolean remove(Object key, Object value) {
        return lock.writeLocked(() -> map.remove(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        long stamp = lock.readLock();
        try {
            Object curValue = get(key);
            if (currentlyIsNotMappedToValue(key, oldValue, curValue)) {
                return false;
            }
            stamp = lock.toWriteLock(stamp);
            map.put(key, newValue);
            return true;
        } finally {
            lock.unlock(stamp);
        }
    }

    private boolean currentlyIsNotMappedToValue(K key, V oldValue, Object curValue) {
        return !(Objects.equals(curValue, oldValue) && (curValue != null || containsKey(key)));
    }

    @Override
    public V replace(K key, V value) {
        long stamp = lock.readLock();
        try {
            if (!containsKey(key)) {
                return null;
            }
            stamp = lock.toWriteLock(stamp);
            return map.put(key, value);
        } finally {
            lock.unlock(stamp);
        }
    }

    @Override
    public Set<K> keySet() {
        return new SetView<>(map.keySet(), lock);
    }

    @Override
    public Collection<V> values() {
        return new CollectionView<>(map.values(), lock);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new SetView<>(map.entrySet(), lock);
    }

    @Override
    public int hashCode() {
        return lock.readLocked(map::hashCode);
    }

    @Override
    public boolean equals(Object other) {
        return lock.readLocked(() -> map.equals(other));
    }

    @Override
    public String toString() {
        return lock.readLocked(map::toString);
    }

    @RequiredArgsConstructor
    private static class SetView<K> extends AbstractSet<K> {

        private final Set<K> sourceSet;
        private final DmapStampedLock lock;

        @Override
        public Iterator<K> iterator() {
            return new OptimisticMapIterator<>(lock, sourceSet.iterator());
        }

        @Override
        public Object[] toArray() {
            return lock.readLocked(sourceSet::toArray);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return lock.readLocked(() -> sourceSet.toArray(a));
        }

        @Override
        public boolean remove(Object o) {
            return lock.writeLocked(() -> sourceSet.remove(o));
        }

        @Override
        public void clear() {
            lock.writeLocked(() -> {
                sourceSet.clear();
                return null;
            });
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return lock.optimisticRead(() -> sourceSet.containsAll(c));
        }

        @Override
        public int size() {
            return lock.optimisticRead(sourceSet::size);
        }

        @Override
        public boolean isEmpty() {
            return lock.optimisticRead(sourceSet::isEmpty);
        }

        @Override
        public boolean contains(Object o) {
            return lock.optimisticRead(() -> sourceSet.contains(o));
        }
    }

    @RequiredArgsConstructor
    private static class CollectionView<V> extends AbstractCollection<V> {

        private final Collection<V> values;
        private final DmapStampedLock lock;

        @Override
        public boolean isEmpty() {
            return lock.optimisticRead(values::isEmpty);
        }

        @Override
        public boolean contains(Object o) {
            return lock.optimisticRead(() -> values.contains(o));
        }

        @Override
        public Object[] toArray() {
            return lock.readLocked(values::toArray);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return lock.readLocked(() -> values.toArray(a));
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return lock.readLocked(() -> values.containsAll(c));
        }

        @Override
        public String toString() {
            return lock.readLocked(values::toString);
        }

        @Override
        public Iterator<V> iterator() {
            return new OptimisticMapIterator<>(lock, values.iterator());
        }

        @Override
        public int size() {
            return lock.optimisticRead(values::size);
        }
    }

    @RequiredArgsConstructor
    private static class OptimisticMapIterator<K> implements Iterator<K> {

        private final DmapStampedLock lock;
        private final Iterator<K> sourceIterator;

        @Override
        public boolean hasNext() {
            return sourceIterator.hasNext();
        }

        @Override
        public K next() {
            return sourceIterator.next();
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            lock.readLocked(() -> {
                sourceIterator.forEachRemaining(action);
                return null;
            });
        }
    }
}
