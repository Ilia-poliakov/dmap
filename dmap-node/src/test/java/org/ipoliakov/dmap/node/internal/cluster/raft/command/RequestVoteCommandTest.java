package org.ipoliakov.dmap.node.internal.cluster.raft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.internal.raft.RequestVoteRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestVoteCommandTest extends IntegrationTest {

    @Test
    @DisplayName("RequestVote RPC. 1.Reply false if term < currentTerm (§5.1)")
    void execute_currentTermGreaterThanRequest() throws Exception {
        raftState.setCurrentTerm(10);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(10)
                .setCandidateId(1)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(false)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Rules for all Servers. If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower (§5.1)")
    void execute_toFollowerWhenCurrentTermLessThanRequest() throws Exception {
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(Integer.MAX_VALUE)
                .setLastLogTerm(10)
                .setCandidateId(Integer.MAX_VALUE)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        resp.get(0).get(5, TimeUnit.SECONDS);
        assertEquals(Integer.MAX_VALUE, raftState.getCurrentTerm());
        assertEquals(Role.FOLLOWER, raftState.getRole());
    }

    @Test
    void execute_rejectWhenWeAlreadyHaveLeader() throws Exception {
        raftState.setLeaderId(1);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(10)
                .setCandidateId(2)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(false)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    void execute_rejectWhenAlreadyVoted() throws Exception {
        raftState.setVotedFor(2);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(10)
                .setCandidateId(1)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(false)
                .setTerm(raftState.getCurrentTerm())
                .setVoterId(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    void execute_grantWhenAlreadyVotedForCandidate() throws Exception {
        raftState.setVotedFor(2);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(10)
                .setCandidateId(2)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(true)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    void execute_rejectWhenCurrentLogTermGreaterThanRequest() throws Exception {
        raftLog.setLastTerm(100);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(10)
                .setCandidateId(2)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(false)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    void execute_rejectWhenCurrentLogIndexGreaterThanRequest() throws Exception {
        raftLog.setLastTerm(1);
        raftLog.setLastIndex(100);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(1)
                .setCandidateId(2)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(false)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    void execute_voteGranted() throws Exception {
        raftLog.setLastTerm(1);
        raftLog.setLastIndex(1);
        RequestVoteReq req = RequestVoteReq.newBuilder()
                .setTerm(1)
                .setLastLogTerm(1)
                .setCandidateId(2)
                .setLastLogIndex(1)
                .setPayloadType(PayloadType.REQUEST_VOTE_REQ)
                .build();

        RequestVoteRes expected = RequestVoteRes.newBuilder()
                .setVoteGranted(true)
                .setVoterId(1)
                .setTerm(raftState.getCurrentTerm())
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .build();
        List<CompletableFuture<RequestVoteRes>> resp = raftCluster.sendToAll(req, RequestVoteRes.class);

        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }
}