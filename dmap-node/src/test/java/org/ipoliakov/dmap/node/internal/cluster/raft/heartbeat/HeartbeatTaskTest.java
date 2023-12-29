package org.ipoliakov.dmap.node.internal.cluster.raft.heartbeat;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class HeartbeatTaskTest {

    @Test
    void heartbeatRequest() {
        int term = 10;
        int leaderId = 1;
        RaftCluster raftCluster = Mockito.mock(RaftCluster.class);
        var req = AppendEntriesReq.newBuilder()
            .setTerm(term)
            .setLeaderId(leaderId)
            .build();
        when(raftCluster.sendToAll(req, AppendEntriesRes.class))
            .thenReturn(List.of(CompletableFuture.completedFuture(AppendEntriesRes.getDefaultInstance())));

        RaftState raftState = new RaftState(leaderId, Mockito.mock(ApplicationEventPublisher.class));
        raftState.setCurrentTerm(term);
        raftState.becomeLeader();
        HeartbeatTask heartbeatTask = new HeartbeatTask(raftState, raftCluster);
        heartbeatTask.execute();

        Mockito.verify(raftCluster).sendToAll(req, AppendEntriesRes.class);
    }
}