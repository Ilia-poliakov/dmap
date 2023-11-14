package org.ipoliakov.dmap.node.internal.cluster.raft;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Component
@EqualsAndHashCode
@RequiredArgsConstructor
public class RaftState {

    //TODO: maby possible plain semantic instead of volatile for all fields

    private volatile int leaderId = -1;

    @Value("${node.id}")
    private int selfId;
    private volatile Role role = Role.FOLLOWER;
    private volatile int currentTerm = 1;
    private int votedFor = -1;
    private volatile long index;

    private final Set<Integer> votedServers = new HashSet<>();

    public boolean haveLeader() {
        return leaderId > 0;
    }

    public void becomeCandidate() {
        role = Role.CANDIDATE;
        incrementTerm();
        votedFor = -1;
        votedServers.clear();
        votedServers.add(selfId);
    }

    public void incrementTerm() {
        currentTerm++;
    }

    public long nextIndex() {
        return ++index;
    }

    public void becomeFollower(int term) {
        role = Role.FOLLOWER;
        this.currentTerm = term;
        votedServers.clear();
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
        return votedFor > 0;
    }

    public void becomeLeader() {
        role = Role.LEADER;
    }
}
