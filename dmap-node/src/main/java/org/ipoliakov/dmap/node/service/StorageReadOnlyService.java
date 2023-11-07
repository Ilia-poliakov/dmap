package org.ipoliakov.dmap.node.service;

import com.google.protobuf.ByteString;

public interface StorageReadOnlyService {

    ByteString get(ByteString key);
}
