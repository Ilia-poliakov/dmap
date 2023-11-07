package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.protocol.PutReq;

import com.google.protobuf.ByteString;

public interface StorageMutationService {

    ByteString put(PutReq putReq);
}
