package org.ipoliakov.dmap.node.cluster.raft.heartbeat;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.cluster.Cluster;
import org.ipoliakov.dmap.node.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesRes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class HeartbeatTaskTest {

    @Test
    void heartbeatRequest() {
        int term = 10;
        int leaderId = 1;
        Cluster cluster = Mockito.mock(Cluster.class);
        var req = AppendEntriesReq.newBuilder()
            .setTerm(term)
            .setLeaderId(leaderId)
            .build();
        when(cluster.sendToAll(req, AppendEntriesRes.class))
            .thenReturn(List.of(CompletableFuture.completedFuture(AppendEntriesRes.getDefaultInstance())));

        RaftState raftState = new RaftState(leaderId, Mockito.mock(ApplicationEventPublisher.class));
        raftState.setCurrentTerm(term);
        raftState.becomeLeader();
        HeartbeatTask heartbeatTask = new HeartbeatTask(cluster, raftState);
        heartbeatTask.execute();

        Mockito.verify(cluster).sendToAll(req, AppendEntriesRes.class);
    }
}