package org.ipoliakov.dmap.node.cluster.raft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.cluster.raft.Role;
import org.ipoliakov.dmap.node.cluster.raft.log.RaftLogService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

@Timeout(10)
class AppendEntriesCommandTest extends IntegrationTest {

    @Autowired
    private RaftLogService raftLogService;

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

        List<CompletableFuture<AppendEntriesRes>> resp = cluster.sendToAll(req, AppendEntriesRes.class);
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

        List<CompletableFuture<AppendEntriesRes>> resp = cluster.sendToAll(req, AppendEntriesRes.class);
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

        List<CompletableFuture<AppendEntriesRes>> resp = cluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
        assertEquals(Role.FOLLOWER, raftState.getRole());
        assertEquals(req.getLeaderId(), raftState.getLeaderId());
    }

    @Test
    @DisplayName("Reply false if log does not contain an entry at prevLogIndex whose term matches prevLogTerm (§5.3)")
    void execute_false() throws Exception{
        raftLog.setLastTerm(1);
        raftLog.setLastIndex(1);
        raftState.setCurrentTerm(1);
        raftState.setLeaderId(10);

        AppendEntriesReq req = appendEntriesReq(raftLog.getLastIndex() + 1);
        AppendEntriesRes expected = AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(raftState.getCurrentTerm())
                .setSuccess(false)
                .build();

        List<CompletableFuture<AppendEntriesRes>> resp = cluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
        assertTrue(raftLogService.readLastEntry().isEmpty());
        assertEquals("", storageClient.get("key").get());
    }

    @Test
    void execute_accept() throws Exception {
        raftLog.setLastTerm(1);
        raftLog.setLastIndex(1);
        raftState.setCurrentTerm(1);
        raftState.setLeaderId(10);

        AppendEntriesReq req = appendEntriesReq(raftLog.getLastIndex());
        AppendEntriesRes expected = AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(raftState.getCurrentTerm())
                .setSuccess(true)
                .build();

        List<CompletableFuture<AppendEntriesRes>> resp = cluster.sendToAll(req, AppendEntriesRes.class);
        assertEquals(1, resp.size());
        assertEquals(expected, resp.get(0).get(5, TimeUnit.SECONDS));
        assertEquals(req.getEntries(0), raftLogService.readLastEntry().get());
        assertEquals("value", storageClient.get("key").get());
    }

    private AppendEntriesReq appendEntriesReq(long prevLogIndex) {
        return AppendEntriesReq.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_REQ)
                .setTerm(raftLog.getLastTerm())
                .setLeaderId(10)
                .setPrevLogTerm(raftLog.getLastTerm())
                .setPrevLogIndex(prevLogIndex)
                .addEntries(Operation.newBuilder()
                        .setPayloadType(PayloadType.PUT_REQ)
                        .setTerm(raftLog.getLastTerm())
                        .setLogIndex(raftLog.getLastIndex())
                        .setMessage(PutReq.newBuilder()
                                .setPayloadType(PayloadType.PUT_REQ)
                                .setKey(ByteString.copyFromUtf8("key"))
                                .setValue(ByteString.copyFromUtf8("value"))
                                .build().toByteString())
                        .build())
                .build();
    }
}