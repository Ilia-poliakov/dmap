package org.ipoliakov.dmap.node.service;

import java.util.Map;
import java.util.Objects;

import org.ipoliakov.dmap.protocol.PutReq;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@Service("storageService")
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageMutationService, StorageReadOnlyService {

    private final Map<ByteString, ByteString> dataStorage;

    public ByteString put(PutReq putReq) {
        ByteString prevVal = dataStorage.put(putReq.getKey(), putReq.getValue());
        return Objects.requireNonNullElse(prevVal, ByteString.EMPTY);
    }

    @Override
    public ByteString get(ByteString key) {
        return dataStorage.getOrDefault(key, ByteString.EMPTY);
    }
}
