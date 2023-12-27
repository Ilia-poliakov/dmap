package org.ipoliakov.dmap.node.internal.cluster.raft.election;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.internal.RequestVoteRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class VoteResponseHandlerTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void handle_ignore_WeAlreadyNotACandidate() {
        RaftState raftState = followerState(1);
        RaftState expectedState = followerState(1);
        VoteResponseHandler handler = new VoteResponseHandler(raftState);

        handler.handle(
                RequestVoteRes.newBuilder()
                        .setTerm(2)
                        .setVoteGranted(true)
                        .setVoterId(1)
                        .build(), 3
        );
        assertEquals(expectedState, raftState);
    }

    @Test
    void handler_becomeFollower_whenOurTermExpired() {
        RaftState raftState = candidateState(1);
        RaftState expectedState = followerState(Integer.MAX_VALUE);
        VoteResponseHandler handler = new VoteResponseHandler(raftState);

        handler.handle(
                RequestVoteRes.newBuilder()
                        .setTerm(Integer.MAX_VALUE)
                        .setVoteGranted(true)
                        .setVoterId(1)
                        .build(), 3
        );
        assertEquals(expectedState, raftState);
    }

    @Test
    void handler_ignore_whenStaleResponse() {
        RaftState raftState = candidateState(Integer.MAX_VALUE);
        RaftState expectedState = candidateState(Integer.MAX_VALUE);
        VoteResponseHandler handler = new VoteResponseHandler(raftState);

        handler.handle(
                RequestVoteRes.newBuilder()
                        .setTerm(1)
                        .setVoteGranted(true)
                        .setVoterId(1)
                        .build(), 3
        );
        assertEquals(expectedState, raftState);
    }

    @Test
    void handle_becomeLeader() {
        RaftState raftState = candidateState(1);
        RaftState expectedState = leaderState(1);
        VoteResponseHandler handler = new VoteResponseHandler(raftState);

        int majorityNodesCount = 3;
        for (int i = 0; i < majorityNodesCount; i++) {
            expectedState.grantVote(i);
            handler.handle(
                    RequestVoteRes.newBuilder()
                            .setTerm(1)
                            .setVoteGranted(true)
                            .setVoterId(i + 1)
                            .build(), majorityNodesCount
            );
        }
        assertEquals(expectedState, raftState);
    }

    private RaftState followerState(int term) {
        RaftState raftState = new RaftState(0, applicationEventPublisher);
        raftState.becomeFollower(term);
        return raftState;
    }

    private RaftState candidateState(int term) {
        RaftState raftState = new RaftState(0, applicationEventPublisher);
        raftState.becomeCandidate();
        raftState.setCurrentTerm(term);
        return raftState;
    }

    private RaftState leaderState(int term) {
        RaftState raftState = new RaftState(0, applicationEventPublisher);
        raftState.becomeLeader();
        raftState.setCurrentTerm(term);
        return raftState;
    }

}