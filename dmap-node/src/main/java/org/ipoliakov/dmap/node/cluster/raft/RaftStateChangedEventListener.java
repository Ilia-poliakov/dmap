package org.ipoliakov.dmap.node.cluster.raft;

import org.ipoliakov.dmap.node.cluster.raft.election.ElectionService;
import org.ipoliakov.dmap.node.cluster.raft.heartbeat.HeartbeatService;
import org.ipoliakov.dmap.node.cluster.raft.state.RoleChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RaftStateChangedEventListener {

    private final ElectionService electionService;
    private final HeartbeatService heartbeatService;

    @EventListener
    public void onRoleChanged(RoleChangedEvent event) {
        switch (event.newRole()) {
            case Role.LEADER -> onBecomeLeader();
            case Role.FOLLOWER -> onBecomeFollower();
            case Role.CANDIDATE -> onBecomeCandidate();
            default -> throw new IllegalStateException("Unknown role: " + event.newRole());
        }
    }

    private void onBecomeFollower() {
        heartbeatService.stopSendingHeartbeats();
        electionService.restartElectionTask();
    }

    private void onBecomeLeader() {
        electionService.stopElectionTask();
        heartbeatService.startSendingHeartbeats();
    }

    private void onBecomeCandidate() {
        electionService.stopElectionTask();
    }
}
