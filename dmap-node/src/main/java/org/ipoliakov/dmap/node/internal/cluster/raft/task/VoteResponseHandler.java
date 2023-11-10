package org.ipoliakov.dmap.node.internal.cluster.raft.task;

import org.ipoliakov.dmap.node.internal.cluster.raft.RaftState;
import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.ipoliakov.dmap.protocol.internal.RequestVoteRes;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteResponseHandler {

    private final RaftState raftState;

    public void handle(RequestVoteRes res, int majorityNodesCount) {
        if (raftState.getRole() != Role.CANDIDATE) {
            log.info("We are not a candidate. Ignore response " + res);
            return;
        }
        if (res.getTerm() > raftState.getCurrentTerm()) {
            log.info("Our term less than others. Become follower. Current term = {}, new term = {}", raftState.getCurrentTerm(), res.getTerm());
            raftState.becomeFollower(res.getTerm());
            return;
        }
        if (res.getTerm() < raftState.getCurrentTerm()) {
            log.info("Stale response {} current term = {}", res, raftState.getCurrentTerm());
            return;
        }
        if (res.getVoteGranted() && raftState.grantVote(res.getVoterId())) {
            log.info("Vote granted from {}. number of votes = {}", res.getVoterId(), raftState.getVoteCount());
        }
        if (raftState.getVoteCount() >= majorityNodesCount) {
            log.info("BECOME THE LEADER");
            raftState.becomeLeader();
        }
    }
}
