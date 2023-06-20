package org.ipoliakov.dmap.node.datastructures;

class Entry {

    private static int offset = 0;

    public static final int hashCodeOffset = offset += 0;
    public static final int nextOffset = offset += 4;
    public static final int keySizeOffset = offset += 4;
    public static final int valueSizeOffset = offset += 4;
//    public static final int keyDataOffset = offset += 4;
//    public static final int valueDataOffset = offset += 4;

    private static int entryOffset;

    public static void setEntryOffset(int entryOffset) {
        Entry.entryOffset = entryOffset;
    }

    public int getHashCodeOffset() {
        return entryOffset + hashCodeOffset;
    }

    public int getNextOffset() {
        return entryOffset + nextOffset;
    }

    public int getKeySizeOffset() {
        return entryOffset + keySizeOffset;
    }

    public int getValueSizeOffset() {
        return entryOffset + valueSizeOffset;
    }
}
