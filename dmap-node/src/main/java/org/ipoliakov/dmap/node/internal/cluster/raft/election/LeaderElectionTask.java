package org.ipoliakov.dmap.node.internal.cluster.raft.election;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteRes;
import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderElectionTask implements ScheduledTask {

    private final RaftLog raftLog;
    private final RaftState raftState;
    private final RaftCluster raftCluster;
    private final VoteResponseHandler voteResponseHandler;

    @Override
    public void runInternal() {
        log.debug("LeaderElectionTask - start");

        raftState.becomeCandidate();
        var voteReq = RequestVoteReq.newBuilder()
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .setTerm(raftState.getCurrentTerm())
                .setCandidateId(raftState.getSelfId())
                .setLastLogIndex(raftLog.getLastIndex())
                .setLastLogTerm(raftLog.getLastTerm())
                .build();
        log.info("Leader election - start: voteReq = {}", voteReq);

        for (CompletableFuture<RequestVoteRes> cf : raftCluster.sendToAll(voteReq, RequestVoteRes.class)) {
            cf.thenAcceptAsync(res -> voteResponseHandler.handle(res, raftCluster.getMajorityNodesCount()));
        }
    }
}
