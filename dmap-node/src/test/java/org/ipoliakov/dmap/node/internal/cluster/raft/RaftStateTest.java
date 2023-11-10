package org.ipoliakov.dmap.node.internal.cluster.raft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RaftStateTest {

    private RaftState raftState;

    @BeforeEach
    void setUp() {
        raftState = new RaftState().setSelfId(1);
    }

    @Test
    void incrementTerm() {
        raftState.incrementTerm();
        assertEquals(2, raftState.getCurrentTerm());
    }

    @Test
    void term_shouldStartFromOne() {
        assertEquals(1, raftState.getCurrentTerm());
    }

    @Test
    void getVotedFor_minusOneIfDidNotVote() {
        assertEquals(-1, raftState.getVotedFor());
    }

    @Test
    void haveLeader() {
        raftState.setLeaderId(1);
        assertTrue(raftState.haveLeader());
    }

    @Test
    void doNotHaveLeaderYet() {
        assertFalse(raftState.haveLeader());
    }

    @Test
    void resetState() {
        raftState.becomeLeader();
        raftState.reset();
        assertEquals(new RaftState().setSelfId(1), raftState);
    }

    @Test
    void becomeCandidate() {
        RaftState expected = candidateState();
        raftState.becomeCandidate();
        assertEquals(expected, raftState);
    }

    @Test
    void becomeFollower() {
        RaftState expected = followerState(1);
        raftState.becomeFollower(1);
        assertEquals(expected, raftState);
    }

    @Test
    void alreadyVoted() {
        raftState.setVotedFor(100);
        assertTrue(raftState.alreadyVoted());
    }

    @Test
    void notVotedYet() {
        assertFalse(raftState.alreadyVoted());
    }

    @Test
    void grantVote() {
        raftState.grantVote(100);
        raftState.grantVote(200);
        assertEquals(2, raftState.getVoteCount());
    }

    @Test
    void testHashCode() {
        candidateState().hashCode();
    }

    private static RaftState candidateState() {
        var result = new RaftState()
                .setSelfId(1)
                .setRole(Role.CANDIDATE)
                .setVotedFor(-1);
        result.grantVote(1);
        result.incrementTerm();
        return result;
    }

    private static RaftState followerState(int term) {
        return new RaftState()
                .setSelfId(1)
                .setRole(Role.FOLLOWER)
                .setCurrentTerm(term);
    }
}