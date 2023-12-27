package org.ipoliakov.dmap.node.internal.cluster.raft.state;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Component
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RaftState {

    @Value("${MEMBER_ID:${member.id}}")
    private final int selfId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Set<Integer> votedServers = ConcurrentHashMap.newKeySet();

    private volatile int leaderId = -1;
    private volatile Role role = Role.FOLLOWER;
    private volatile int currentTerm = 1;
    private volatile int votedFor = -1;
    private volatile long index;

    public boolean haveLeader() {
        return leaderId > -1;
    }

    public void becomeCandidate() {
        role = Role.CANDIDATE;
        incrementTerm();
        votedFor = selfId;
        leaderId = -1;
        votedServers.clear();
        votedServers.add(selfId);
        applicationEventPublisher.publishEvent(new RoleChangedEvent(role));
    }

    public void incrementTerm() {
        currentTerm++;
    }

    public long nextIndex() {
        return ++index;
    }

    public void becomeFollower(int term) {
        role = Role.FOLLOWER;
        leaderId = -1;
        votedFor = -1;
        this.currentTerm = term;
        votedServers.clear();
        applicationEventPublisher.publishEvent(new RoleChangedEvent(role));
    }

    public void reset() {
        role = Role.FOLLOWER;
        votedFor = -1;
        votedServers.clear();
        currentTerm = 1;
        leaderId = -1;
        index = 0;
    }

    public boolean grantVote(int voterId) {
        return votedServers.add(voterId);
    }

    public int getVoteCount() {
        return votedServers.size();
    }

    public boolean alreadyVoted() {
        return votedFor > -1;
    }

    public void becomeLeader() {
        role = Role.LEADER;
        votedFor = -1;
        leaderId = selfId;
        applicationEventPublisher.publishEvent(new RoleChangedEvent(role));
    }
}
