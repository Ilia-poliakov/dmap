package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.protocol.storage.RemoveReq;

import com.google.protobuf.ByteString;

public interface StorageMutationService {

    ByteString put(PutReq req);

    ByteString remove(RemoveReq req);
}
