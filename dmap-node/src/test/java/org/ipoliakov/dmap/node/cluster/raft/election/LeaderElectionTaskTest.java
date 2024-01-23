package org.ipoliakov.dmap.node.cluster.raft.election;


import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.cluster.Cluster;
import org.ipoliakov.dmap.node.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.raft.RequestVoteRes;
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
    private Cluster cluster;
    @Mock
    private VoteResponseHandler voteResponseHandler;

    @InjectMocks
    private LeaderElectionTask leaderElectionTask;

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
        when(cluster.getMajorityNodesCount()).thenReturn(5);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .setTerm(currentTerm)
                .setCandidateId(selfId)
                .setLastLogIndex(lastLogIndex)
                .setLastLogTerm(lastTerm)
                .build();
        when(cluster.sendToAll(req, RequestVoteRes.class))
                .thenReturn(List.of(CompletableFuture.completedFuture(RequestVoteRes.getDefaultInstance())));

        leaderElectionTask.run();

        await().untilAsserted(() -> verify(voteResponseHandler).handle(RequestVoteRes.getDefaultInstance(), 5));
    }
}