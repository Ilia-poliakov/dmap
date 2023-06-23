package org.ipoliakov.dmap.node.datastructures.offheap.map;

import java.lang.ref.Cleaner;
import java.util.AbstractMap;
import java.util.Set;

import com.google.protobuf.ByteString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class OffHeapHashMap extends AbstractMap<ByteString, ByteString> {

    private static final Cleaner CLEANER = Cleaner.create();

    private static int offset;

    public static final int HASH_CODE_OFFSET = offset += 0;
    public static final int NEXT_ENTRY_OFFSET = offset += Integer.BYTES;
    public static final int KEY_SIZE_OFFSET = offset += Integer.BYTES;
    public static final int VALUE_SIZE_OFFSET = offset += Integer.BYTES;

    final ByteBuf buf;
    final int capacity;

    private int size;
    private EntrySet entrySet;

    public OffHeapHashMap() {
        this(16);
    }

    public OffHeapHashMap(int capacity) {
        this.buf = Unpooled.directBuffer(capacity * Integer.BYTES, Integer.MAX_VALUE);
        this.capacity = capacity;
        CLEANER.register(this, new ByteBufCleanAction(buf));
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
    public ByteString get(Object key) {
        ByteString k = (ByteString) key;
        int hashCode = k.hashCode();
        int bucketOffset = getBucketFor(hashCode);

        for (int entryOffset; (entryOffset = buf.getIntLE(bucketOffset)) != 0; bucketOffset = entryOffset + NEXT_ENTRY_OFFSET) {
            if (buf.getIntLE(entryOffset + HASH_CODE_OFFSET) == hashCode && equals(entryOffset, k)) {
                return getValue(entryOffset);
            }
        }
        return null;
    }

    private ByteString getValue(int entry) {
        int keySize = buf.getIntLE(entry + KEY_SIZE_OFFSET);
        int valueSize = buf.getIntLE(entry + VALUE_SIZE_OFFSET);
        byte[] dst = new byte[valueSize];
        buf.getBytes(entry + VALUE_SIZE_OFFSET + keySize + Integer.BYTES, dst);
        return ByteString.copyFrom(dst);
    }

    @Override
    public ByteString put(ByteString key, ByteString value) {
        int hashCode = key.hashCode();
        int bucketOffset = getBucketFor(hashCode);
        int valueSize = value.size();

        for (int entryOffset; (entryOffset = buf.getIntLE(bucketOffset)) != 0; bucketOffset = entryOffset + NEXT_ENTRY_OFFSET) {
            if (buf.getIntLE(entryOffset + HASH_CODE_OFFSET) == hashCode && equals(entryOffset, key)) {
                if (canOverwrite(entryOffset, valueSize)) {
                    setValue(entryOffset, value);
                    return value;
                }
                buf.setIntLE(bucketOffset, buf.getInt(entryOffset + NEXT_ENTRY_OFFSET));
                freeEntry(entryOffset);
                size--;
                break;
            }
        }
        writeNewEntry(bucketOffset, key, value, hashCode);
        size++;
        return value;
    }

    private boolean equals(int entry, ByteString key) {
        int existingKeySize = buf.getIntLE(entry + KEY_SIZE_OFFSET);
        if (existingKeySize != key.size()) {
            return false;
        }
        buf.readerIndex(entry + VALUE_SIZE_OFFSET + existingKeySize + 1);
        for (int i = 0; i < existingKeySize; i++) {
            if (buf.readByte() != key.byteAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean canOverwrite(int bucketOffset, int newValueSize) {
        return newValueSize <= buf.getIntLE(bucketOffset + VALUE_SIZE_OFFSET);
    }

    private void setValue(int entry, ByteString value) {
        int keySize =  buf.getIntLE(entry + KEY_SIZE_OFFSET);
        buf.setIntLE(entry + VALUE_SIZE_OFFSET, value.size());
        buf.writerIndex(entry + VALUE_SIZE_OFFSET + keySize + Integer.BYTES);
        buf.writeBytes(value.toByteArray());
    }

    private void freeEntry(int entryOffset) {
        int keySize =  buf.getIntLE(entryOffset + KEY_SIZE_OFFSET);
        int valueSize =  buf.getIntLE(entryOffset + VALUE_SIZE_OFFSET);
        buf.setZero(entryOffset, VALUE_SIZE_OFFSET + keySize + valueSize - Integer.BYTES);
    }

    private void writeNewEntry(int bucketOffset, ByteString key, ByteString value, int hashCode) {
        int endOfBuffer = buf.capacity();
        buf.writerIndex(endOfBuffer);
        buf.writeIntLE(hashCode);
        buf.writeIntLE(buf.getIntLE(bucketOffset));
        buf.writeIntLE(key.size());
        buf.writeIntLE(value.size());
        buf.writeBytes(key.toByteArray());
        buf.writeBytes(value.toByteArray());
        buf.setIntLE(bucketOffset, endOfBuffer);
    }

    @Override
    public ByteString remove(Object key) {
        ByteString k = (ByteString) key;
        int hashCode = k.hashCode();
        int bucketOffset = getBucketFor(hashCode);
        int entryOffset;
        ByteString removedValue;
        for (;;) {
            if ((entryOffset = buf.getIntLE(bucketOffset)) == 0) {
                return null;
            }
            if (buf.getIntLE(entryOffset + HASH_CODE_OFFSET) == hashCode && equals(entryOffset, k)) {
                removedValue = getValue(entryOffset);
                buf.setIntLE(entryOffset, buf.getIntLE(entryOffset + NEXT_ENTRY_OFFSET));
                break;
            }
            bucketOffset = entryOffset + NEXT_ENTRY_OFFSET;
        }
        freeEntry(entryOffset);
        size--;
        return removedValue;
    }

    private int getBucketFor(int hashCode) {
        return (hashCode & Integer.MAX_VALUE) % capacity * Integer.BYTES;
    }

    @Override
    public void clear() {
        buf.capacity(capacity * Integer.BYTES);
        buf.setZero(0, buf.capacity());
        size = 0;
    }

    @Override
    public Set<Entry<ByteString, ByteString>> entrySet() {
        Set<Entry<ByteString, ByteString>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet(this)) : es;
    }
}
