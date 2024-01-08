package org.ipoliakov.dmap.node.internal.cluster.raft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class RaftReplicationServiceTest {

    @Mock
    private RaftLog raftLog;
    @Mock
    private RaftCluster raftCluster;

    private RaftReplicationService raftReplicationService;

    @BeforeEach
    void setUp() {
        RaftState raftState = leaderState();
        when(raftLog.getLastIndex()).thenReturn(1L);
        when(raftLog.getLastTerm()).thenReturn(1);
        raftReplicationService = new RaftReplicationService(raftLog, raftState, raftCluster);
    }

    @Test
    void replicate() {
        PutReq putReq = ProtoMessages.putReq()
            .setKey(ByteString.copyFromUtf8("key"))
            .setValue(ByteString.copyFromUtf8("value"))
            .build();
        Operation operation = Operation.newBuilder()
            .setPayloadType(putReq.getPayloadType())
            .setLogIndex(2)
            .setTerm(1)
            .setMessage(putReq.toByteString())
            .build();
        AppendEntriesReq appendEntriesReq = ProtoMessages.appendEntriesReq()
            .setLeaderId(1)
            .setTerm(1)
            .setPrevLogIndex(raftLog.getLastIndex())
            .setPrevLogTerm(raftLog.getLastTerm())
            .addEntries(operation)
            .build();
        when(raftCluster.sendToAll(appendEntriesReq, AppendEntriesRes.class))
            .thenReturn(List.of(
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(true).build()),
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(true).build()),
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(true).build())
            ));

        Optional<Operation> replicatedOp = raftReplicationService.replicate(PayloadType.PUT_REQ, putReq);

        assertEquals(operation, replicatedOp.get());
    }

    @Test
    void replicate_quorumUnreachable() {
        when(raftCluster.sendToAll(any(), eq(AppendEntriesRes.class)))
            .thenReturn(List.of(
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(true).build()),
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(false).build()),
                CompletableFuture.completedFuture(AppendEntriesRes.newBuilder().setSuccess(false).build())
            ));
        assertTrue(raftReplicationService.replicate(PayloadType.PUT_REQ, PutReq.getDefaultInstance()).isEmpty());
    }

    private static RaftState leaderState() {
        var raftState = new RaftState(1, Mockito.mock(ApplicationEventPublisher.class))
            .setCurrentTerm(1)
            .setIndex(1);
        raftState.becomeLeader();
        return raftState;
    }
}