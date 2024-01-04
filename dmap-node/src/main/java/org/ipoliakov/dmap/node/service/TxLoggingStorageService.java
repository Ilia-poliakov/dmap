package org.ipoliakov.dmap.node.service;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.exception.ReplicationException;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.ipoliakov.dmap.util.concurrent.FutureUtils;
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
    private final RaftCluster raftCluster;
    private final StorageMutationService storageService;

    @Override
    public ByteString put(PutReq req) {
        Operation operation = operation(req.getPayloadType(), req);
        if (replicate(operation)) {
            raftLog.append(operation);
            return storageService.put(req);
        }
        throw new ReplicationException();
    }

    @Override
    public ByteString remove(RemoveReq req) {
        Operation operation = operation(req.getPayloadType(), req);
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

    private boolean replicate(Operation operation) {
        AppendEntriesReq req = AppendEntriesReq.newBuilder()
                .setLeaderId(raftState.getLeaderId())
                .setTerm(raftState.getCurrentTerm())
                .setPrevLogIndex(raftLog.getLastIndex())
                .setPrevLogTerm(raftLog.getLastTerm())
                .addEntries(operation)
                .build();
        try {
            FutureUtils.waitForQuorum(
                    raftCluster.sendToAll(req, AppendEntriesRes.class),
                    AppendEntriesRes::getSuccess,
                    10, TimeUnit.SECONDS
            );
            return true;
        } catch (Exception e) {
            log.error("Replication failed", e);
            return false;
        }
    }
}
