package org.ipoliakov.dmap.node.service;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.PutRes;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TxLoggingStorageService implements StorageService {

    private final TxLogWriter txLogWriter;
    private final StorageService storageService;

    @Override
    public PutRes put(PutReq putReq) {
        try {
            var operation = Operation.newBuilder()
                    .setPayloadType(putReq.getPayloadType())
                    .setTimestamp(System.currentTimeMillis())
                    .setMessage(putReq.toByteString())
                    .build();
            txLogWriter.write(operation);
            return storageService.put(putReq);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
