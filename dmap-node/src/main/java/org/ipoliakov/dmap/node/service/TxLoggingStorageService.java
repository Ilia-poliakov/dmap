package org.ipoliakov.dmap.node.service;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftState;
import org.ipoliakov.dmap.node.internal.cluster.raft.replication.ReplicationService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TxLoggingStorageService implements StorageMutationService {

    private final RaftLog raftLog;
    private final RaftState raftState;
    private final ReplicationService replicationService;
    private final StorageMutationService storageService;

    @Override
    public ByteString put(PutReq req) {
        Operation operation = operation(req.getPayloadType(), req);
        replicate(operation);
        raftLog.append(operation);
        return storageService.put(req);
    }

    @Override
    public ByteString remove(RemoveReq req) {
        Operation operation = operation(req.getPayloadType(), req);
        replicate(operation);
        raftLog.append(operation);
        return storageService.remove(req);
    }

    private Operation operation(PayloadType payloadType, MessageLite messageLite) {
        return Operation.newBuilder()
                .setPayloadType(payloadType)
                .setLogIndex(raftState.nextIndex())
                .setTerm(raftState.getCurrentTerm())
                .setMessage(messageLite.toByteString())
                .build();
    }

    private void replicate(Operation operation) {
        if (!replicationService.replicate(operation)) {
            log.warn("Operation cannot be replicated to quorum nodes. Operation = {}", operation);
            throw new IllegalStateException("Operation " + operation.getPayloadType() + " cannot be replicated to quorum nodes");
        }
    }
}
