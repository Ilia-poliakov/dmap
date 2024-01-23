package org.ipoliakov.dmap.node.cluster.raft.heartbeat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.cluster.Cluster;
import org.ipoliakov.dmap.node.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesRes;
import org.ipoliakov.dmap.util.concurrent.ScheduledTask;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatTask implements ScheduledTask {

    private final Cluster cluster;
    private final RaftState raftState;

    @Override
    public void execute() {
        log.debug("HeartbeatTask - start");
        var req = AppendEntriesReq.newBuilder()
                .setTerm(raftState.getCurrentTerm())
                .setLeaderId(raftState.getLeaderId())
                .build();
        for (CompletableFuture<AppendEntriesRes> cf : cluster.sendToAll(req, AppendEntriesRes.class)) {
            cf.thenAccept(res -> log.debug("Heartbeat response {}", res)).orTimeout(5, TimeUnit.SECONDS);
        }
    }
}