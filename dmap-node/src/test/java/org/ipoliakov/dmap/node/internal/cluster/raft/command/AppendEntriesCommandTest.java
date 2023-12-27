package org.ipoliakov.dmap.node.internal.cluster.raft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.raft.AppendEntriesRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class AppendEntriesCommandTest extends IntegrationTest {

    @Test
    @DisplayName("Reply false if term < currentTerm (§5.1)")
    void execute_falseWhenReceivedStaleTerm() throws Exception {
        raftState.setCurrentTerm(10);
        AppendEntriesReq req = AppendEntriesReq.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_REQ)
                .setTerm(1)
                .build();
        AppendEntriesRes expected = AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(raftState.getCurrentTerm())
                .setSuccess(false)
                .build();

        List<CompletableFuture<AppendEntriesRes>> resp = raftCluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower (§5.1)")
    void execute_becomeFollowerWhenReceivedNewerTerm() throws Exception {
        AppendEntriesReq req = AppendEntriesReq.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_REQ)
                .setTerm(2)
                .setLeaderId(10)
                .build();
        AppendEntriesRes expected = AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(req.getTerm())
                .setSuccess(true)
                .build();

        List<CompletableFuture<AppendEntriesRes>> resp = raftCluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
        assertEquals(Role.FOLLOWER, raftState.getRole());
    }

    @Test
    @DisplayName("Another node wins the election of the current term")
    void execute_becomeFollowerWhenLooseElections() throws Exception {
        raftState.becomeCandidate();
        AppendEntriesReq req = AppendEntriesReq.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_REQ)
                .setTerm(raftState.getCurrentTerm())
                .setLeaderId(10)
                .build();
        AppendEntriesRes expected = AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(req.getTerm())
                .setSuccess(true)
                .build();

        List<CompletableFuture<AppendEntriesRes>> resp = raftCluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
        assertEquals(Role.FOLLOWER, raftState.getRole());
        assertEquals(req.getLeaderId(), raftState.getLeaderId());
    }
}