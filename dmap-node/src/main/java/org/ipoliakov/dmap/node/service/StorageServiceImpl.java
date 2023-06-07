package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.node.storage.Storage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.PutRes;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@Service("storageService")
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final Storage storage;

    public PutRes put(PutReq putReq) {
        ByteString prevVal = storage.put(putReq.getKey(), putReq.getValue());
        return PutRes.newBuilder()
                .setValue(prevVal != null ? prevVal : ByteString.EMPTY)
                .setPayloadType(PayloadType.PUT_RES)
                .build();
    }
}
