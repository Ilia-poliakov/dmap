package org.ipoliakov.dmap.node.service;

import java.util.Objects;

import org.ipoliakov.dmap.node.storage.Storage;
import org.ipoliakov.dmap.protocol.PutReq;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@Service("storageService")
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageMutationService, StorageReadOnlyService {

    private final Storage storage;

    public ByteString put(PutReq putReq) {
        ByteString prevVal = storage.put(putReq.getKey(), putReq.getValue());
        return Objects.requireNonNullElse(prevVal, ByteString.EMPTY);
    }

    @Override
    public ByteString get(ByteString key) {
        ByteString value = storage.get(key);
        return Objects.requireNonNullElse(value, ByteString.EMPTY);
    }
}
