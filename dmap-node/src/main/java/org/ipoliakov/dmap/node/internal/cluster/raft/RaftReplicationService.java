package org.ipoliakov.dmap.node.internal.cluster.raft;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.internal.cluster.Cluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.ipoliakov.dmap.util.concurrent.FutureUtils;
import org.springframework.stereotype.Service;

import com.google.protobuf.MessageLite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaftReplicationService {

    private final Cluster cluster;
    private final RaftLog raftLog;
    private final RaftState raftState;

    public Optional<Operation> replicate(PayloadType payloadType, MessageLite messageLite) {
        var operation = operation(payloadType, messageLite);
        AppendEntriesReq req = ProtoMessages.appendEntriesReq()
                .setLeaderId(raftState.getLeaderId())
                .setTerm(raftState.getCurrentTerm())
                .setPrevLogIndex(raftLog.getLastIndex())
                .setPrevLogTerm(raftLog.getLastTerm())
                .addEntries(operation)
                .build();
        try {
            FutureUtils.waitForQuorum(
                    cluster.sendToAll(req, AppendEntriesRes.class),
                    AppendEntriesRes::getSuccess,
                    10, TimeUnit.SECONDS
            );
            raftLog.append(operation);
            return Optional.of(operation);
        } catch (Exception e) {
            log.error("Replication failed", e);
            return Optional.empty();
        }
    }

    private Operation operation(PayloadType payloadType, MessageLite messageLite) {
        return Operation.newBuilder()
                .setPayloadType(payloadType)
                .setLogIndex(raftState.nextIndex())
                .setTerm(raftState.getCurrentTerm())
                .setMessage(messageLite.toByteString())
                .build();
    }
}
