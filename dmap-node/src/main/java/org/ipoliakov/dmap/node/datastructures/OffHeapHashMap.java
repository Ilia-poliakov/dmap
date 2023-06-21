package org.ipoliakov.dmap.node.datastructures;

import static org.ipoliakov.dmap.node.datastructures.Entry.hashCodeOffset;
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
        this.buf = Unpooled.directBuffer(capacity * 4, Integer.MAX_VALUE);
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

        for (int entryOffset; (entryOffset = buf.getIntLE(bucketOffset)) != 0; bucketOffset = entryOffset + nextOffset) {
            if (buf.getIntLE(entryOffset + hashCodeOffset) == hashCode && equals(entryOffset, k)) {
                return getValue(entryOffset);
            }
        }
        return null;
    }

    private ByteString getValue(int entry) {
        int keySize =  buf.getIntLE(entry + keySizeOffset);
        int valueSize =  buf.getIntLE(entry + valueSizeOffset);
        byte[] dst = new byte[valueSize];
        buf.getBytes(entry + valueSizeOffset + keySize + Integer.BYTES, dst);
        return ByteString.copyFrom(dst);
    }

    @Override
    public ByteString put(ByteString key, ByteString value) {
        int hashCode = key.hashCode();
        int bucketOffset = getBucketFor(hashCode);
        int valueSize = value.size();

        for (int entryOffset; (entryOffset = buf.getIntLE(bucketOffset)) != 0; bucketOffset = entryOffset + nextOffset) {
            if (buf.getIntLE(entryOffset + hashCodeOffset) == hashCode && equals(entryOffset, key)) {
                if (canOverwrite(entryOffset, valueSize)) {
                    setValue(entryOffset, value);
                    return value;
                }
                buf.setIntLE(bucketOffset, buf.getInt(entryOffset + nextOffset));
                freeEntry(entryOffset);
                size--;
                break;
            }
        }

        int endOfBuffer = buf.capacity();
        buf.writerIndex(endOfBuffer);
        buf.writeIntLE(hashCode);
        buf.writeIntLE(buf.getIntLE(bucketOffset));
        buf.writeIntLE(key.size());
        buf.writeIntLE(valueSize);
        buf.writeBytes(key.toByteArray());
        buf.writeBytes(value.toByteArray());
        buf.setIntLE(bucketOffset, endOfBuffer);

        size++;
        return value;
    }

    private void freeEntry(int entryOffset) {
        int keySize =  buf.getIntLE(entryOffset + keySizeOffset);
        int valueSize =  buf.getIntLE(entryOffset + valueSizeOffset);
        buf.setZero(entryOffset, valueSizeOffset + keySize + valueSize - Integer.BYTES);
    }

    private boolean canOverwrite(int bucketOffset, int newValueSize) {
        return newValueSize <= sizeOf(bucketOffset);
    }

    private int sizeOf(int bucketOffset) {
        return buf.getIntLE(bucketOffset + valueSizeOffset);
    }

    private void setValue(int entry, ByteString value) {
        int keySize =  buf.getIntLE(entry + keySizeOffset);
        buf.setIntLE(entry + valueSizeOffset, value.size());
        buf.writerIndex(entry + valueSizeOffset + keySize + Integer.BYTES);
        buf.writeBytes(value.toByteArray());
    }

    private boolean equals(int entry, ByteString key) {
        int existingKeySize = buf.getIntLE(entry + keySizeOffset);
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
