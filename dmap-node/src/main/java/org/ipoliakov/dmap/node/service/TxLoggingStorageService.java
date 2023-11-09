package org.ipoliakov.dmap.node.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicLong;

import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TxLoggingStorageService implements StorageMutationService {

    private static final AtomicLong index = new AtomicLong();

    private final TxLogWriter txLogWriter;
    private final StorageMutationService storageService;

    @Override
    public ByteString put(PutReq req) {
        writeOperation(req.getPayloadType(), req);
        return storageService.put(req);
    }

    @Override
    public ByteString remove(RemoveReq req) {
        writeOperation(req.getPayloadType(), req);
        return storageService.remove(req);
    }

    private void writeOperation(PayloadType payloadType, MessageLite messageLite) {
        try {
            var operation = Operation.newBuilder()
                    .setPayloadType(payloadType)
                    //TODO: User last index when raft implementing. Should start from 1, not 0
                    .setLogIndex(index.incrementAndGet())
                    .setMessage(messageLite.toByteString())
                    .build();
            txLogWriter.write(operation);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
