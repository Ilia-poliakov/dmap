package org.ipoliakov.dmap.node.internal.cluster.raft.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RaftStateTest {

    private RaftState raftState;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        raftState = new RaftState(1, applicationEventPublisher);
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
        assertEquals(new RaftState(1, applicationEventPublisher), raftState);
    }

    @Test
    void becomeCandidate() {
        RaftState expected = candidateState();
        raftState.setLeaderId(100);
        raftState.becomeCandidate();
        assertEquals(expected, raftState);
        verify(applicationEventPublisher).publishEvent(new RoleChangedEvent(Role.CANDIDATE));
    }

    @Test
    void becomeFollower() {
        RaftState expected = followerState(1);
        raftState.becomeFollower(1);
        assertEquals(expected, raftState);
        verify(applicationEventPublisher).publishEvent(new RoleChangedEvent(Role.FOLLOWER));
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

    @Test
    void testToString() {
        assertFalse(candidateState().toString().contains("applicationEventPublisher"));
    }

    @Test
    void testEquals() {
        assertEquals(candidateState(), candidateState());
        assertEquals(followerState(1), followerState(1));
        assertNotEquals(candidateState().setRole(Role.LEADER), candidateState());
        assertNotEquals(followerState(1), followerState(2));
        assertNotEquals(followerState(1), candidateState());
    }

    private RaftState candidateState() {
        var result = new RaftState(1, applicationEventPublisher)
                .setRole(Role.CANDIDATE)
                .setVotedFor(1);
        result.grantVote(1);
        result.incrementTerm();
        return result;
    }

    private RaftState followerState(int term) {
        return new RaftState(1, applicationEventPublisher)
                .setRole(Role.FOLLOWER)
                .setCurrentTerm(term);
    }
}