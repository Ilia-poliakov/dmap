package org.ipoliakov.dmap.node.datastructures;

import static org.ipoliakov.dmap.node.datastructures.Entry.keySizeOffset;
import static org.ipoliakov.dmap.node.datastructures.Entry.nextOffset;
import static org.ipoliakov.dmap.node.datastructures.Entry.valueSizeOffset;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.ByteString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OffHeapHashMap implements Map<ByteString, ByteString> {

    private final ByteBuf buf;
    private final int capacity;

    private int size;

    public OffHeapHashMap(int capacity) {
        this.buf = Unpooled.directBuffer(capacity * 16, Integer.MAX_VALUE);
        this.capacity = capacity;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public ByteString get(Object key) {
        ByteString k = (ByteString) key;
        int hashCode = k.hashCode();
        int bucketOffset = getBucketFor(hashCode);

        for (; buf.getInt(bucketOffset) != 0; bucketOffset += nextOffset) {
            if (buf.getInt(bucketOffset) == hashCode && equals(bucketOffset, k)) {
                return getValue(bucketOffset);
            }
        }
        return null;
    }

    private ByteString getValue(int entry) {
        int keySize =  buf.getInt(entry + keySizeOffset);
        int valueSize =  buf.getInt(entry + valueSizeOffset);
        byte[] dst = new byte[valueSize];
        buf.getBytes(entry + valueSizeOffset + keySize + Integer.BYTES, dst);
        return ByteString.copyFrom(dst);
    }

    @Override
    public ByteString put(ByteString key, ByteString value) {
        int hashCode = key.hashCode();
        int bucketOffset = getBucketFor(hashCode);
        int valueSize = value.size();

        for (; buf.getInt(bucketOffset) != 0; bucketOffset += nextOffset) {
            if (buf.getInt(bucketOffset) == hashCode && equals(bucketOffset, key)) {
                setValue(bucketOffset, value);
            }
        }

        buf.writerIndex(bucketOffset);
        buf.writeInt(hashCode);
        buf.writeInt(bucketOffset);
        buf.writeInt(key.size());
        buf.writeInt(valueSize);
        buf.writeBytes(key.toByteArray());
        buf.writeBytes(value.toByteArray());

        return value;
    }

    private void setValue(int entry, ByteString value) {

    }

    private boolean equals(int entry, ByteString key) {
        int existingKeySize = buf.getInt(entry + keySizeOffset);
        if (existingKeySize != key.size()) {
            return false;
        }
        buf.readerIndex(entry + valueSizeOffset + existingKeySize + 1);
        for (int i = 0; i < existingKeySize; i++) {
            if (buf.readByte() != key.byteAt(i)) {
                return false;
            }
        }
        return true;
    }

    private int getBucketFor(int hashCode) {
        return (hashCode & Integer.MAX_VALUE) % capacity * Integer.BYTES;
    }

    @Override
    public ByteString remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends ByteString, ? extends ByteString> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<ByteString> keySet() {
        return null;
    }

    @Override
    public Collection<ByteString> values() {
        return null;
    }

    @Override
    public Set<Entry<ByteString, ByteString>> entrySet() {
        return null;
    }
}
