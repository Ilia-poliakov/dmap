package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.PutRes;

public interface StorageService {

    PutRes put(PutReq putReq);
}
