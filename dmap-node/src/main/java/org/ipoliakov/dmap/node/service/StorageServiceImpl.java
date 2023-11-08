package org.ipoliakov.dmap.node.service;

import java.util.Map;
import java.util.Objects;

import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@Service("storageService")
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageMutationService, StorageReadOnlyService {

    private final Map<ByteString, ByteString> dataStorage;

    @Override
    public ByteString put(PutReq req) {
        ByteString prevVal = dataStorage.put(req.getKey(), req.getValue());
        return Objects.requireNonNullElse(prevVal, ByteString.EMPTY);
    }

    @Override
    public ByteString remove(RemoveReq req) {
        ByteString removed = dataStorage.remove(req.getKey());
        return Objects.requireNonNullElse(removed, ByteString.EMPTY);
    }

    @Override
    public ByteString get(ByteString key) {
        return dataStorage.getOrDefault(key, ByteString.EMPTY);
    }
}
