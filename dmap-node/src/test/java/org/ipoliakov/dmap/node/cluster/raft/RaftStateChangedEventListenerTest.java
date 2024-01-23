package org.ipoliakov.dmap.node.cluster.raft;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.ipoliakov.dmap.node.cluster.raft.election.ElectionService;
import org.ipoliakov.dmap.node.cluster.raft.heartbeat.HeartbeatService;
import org.ipoliakov.dmap.node.cluster.raft.state.RoleChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RaftStateChangedEventListenerTest {

    @Mock
    private ElectionService electionService;
    @Mock
    private HeartbeatService heartbeatService;

    @InjectMocks
    private RaftStateChangedEventListener raftStateChangedEventListener;

    @Test
    void leader_stopElectionTimer_startHeartbeat() {
        raftStateChangedEventListener.onRoleChanged(new RoleChangedEvent(Role.LEADER));
        verify(electionService).stopElectionTask();
        verify(heartbeatService).startSendingHeartbeats();
    }

    @Test
    void follower_stopHeartbeat_restartElectionTimer() {
        raftStateChangedEventListener.onRoleChanged(new RoleChangedEvent(Role.FOLLOWER));
        verify(heartbeatService).stopSendingHeartbeats();
        verify(electionService).restartElectionTask();
    }

    @Test
    void candidate_stopElectionTimer() {
        raftStateChangedEventListener.onRoleChanged(new RoleChangedEvent(Role.CANDIDATE));
        verify(electionService).stopElectionTask();
        verifyNoInteractions(heartbeatService);
    }
}