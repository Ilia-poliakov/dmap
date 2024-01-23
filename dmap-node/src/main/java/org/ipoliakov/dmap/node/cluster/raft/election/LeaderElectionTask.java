package org.ipoliakov.dmap.node.cluster.raft.election;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.cluster.Cluster;
import org.ipoliakov.dmap.node.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.raft.RequestVoteRes;
import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderElectionTask implements ScheduledTask {

    private final Cluster cluster;
    private final RaftLog raftLog;
    private final RaftState raftState;
    private final VoteResponseHandler voteResponseHandler;

    @Override
    public void execute() {
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

        for (CompletableFuture<RequestVoteRes> cf : cluster.sendToAll(voteReq, RequestVoteRes.class)) {
            cf.thenAcceptAsync(res -> voteResponseHandler.handle(res, cluster.getMajorityNodesCount()));
        }
    }
}
