package org.ipoliakov.dmap.node.storage;

import com.google.protobuf.ByteString;

public interface Storage {

    ByteString put(ByteString key, ByteString value);

    ByteString get(ByteString key);

    void clear();
}
