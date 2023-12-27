package org.ipoliakov.dmap.node.internal.cluster.raft.election;


import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaderElectionTaskTest {

    @Mock
    private RaftLog raftLog;
    @Mock
    private RaftState raftState;
    @Mock
    private RaftCluster raftCluster;
    @Mock
    private VoteResponseHandler voteResponseHandler;

    @InjectMocks
    private LeaderElectionTask leaderElectionTask;

//    @Test
//    @DisplayName("LeaderElectionTaskTest - skip task if we already have a leader")
//    void run_skipIfAlreadyHaveLeader() {
//        when(raftState.haveLeader()).thenReturn(true);
//        leaderElectionTask.run();
//        verify(raftState).haveLeader();
//        verify(raftState).getLeaderId();
//        verifyNoMoreInteractions(raftState);
//    }

    @Test
    void requestVotes() {
        int selfId = 1;
        int lastTerm = 12;
        int currentTerm = 10;
        long lastLogIndex = 11L;
        when(raftState.getCurrentTerm()).thenReturn(currentTerm);
        when(raftState.getSelfId()).thenReturn(selfId);
        when(raftLog.getLastIndex()).thenReturn(lastLogIndex);
        when(raftLog.getLastTerm()).thenReturn(lastTerm);
        when(raftCluster.getMajorityNodesCount()).thenReturn(5);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .setTerm(currentTerm)
                .setCandidateId(selfId)
                .setLastLogIndex(lastLogIndex)
                .setLastLogTerm(lastTerm)
                .build();
        when(raftCluster.sendToAll(req, RequestVoteRes.class))
                .thenReturn(List.of(CompletableFuture.completedFuture(RequestVoteRes.getDefaultInstance())));

        leaderElectionTask.run();

        await().untilAsserted(() -> verify(voteResponseHandler).handle(RequestVoteRes.getDefaultInstance(), 5));
    }
}