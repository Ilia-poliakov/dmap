package org.ipoliakov.dmap.node.internal.cluster.raft.replication;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.node.internal.cluster.RaftCluster;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftState;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplicationService {

    private final RaftLog raftLog;
    private final RaftState raftState;
    private final RaftCluster raftCluster;
    private final AppendEntriesResponseHandler responseHandler;

    public boolean replicate(Operation operation) {
        Operation prevOperation = raftLog.getPrevOperation();
        var req = AppendEntriesReq.newBuilder()
                .setPayloadType(operation.getPayloadType())
                .setTerm(operation.getTerm())
                .setPrevLogTerm(prevOperation.getTerm())
                .setPrevLogIndex(prevOperation.getLogIndex())
                .setLeaderId(raftState.getLeaderId())
                .setLeaderCommit(raftLog.getLastIndex())
                .build();
        for (CompletableFuture<AppendEntriesRes> appendEntriesFuture : raftCluster.sendToAll(req, AppendEntriesRes.class)) {
            appendEntriesFuture.thenAccept(res -> responseHandler.handle(res, raftCluster.getMajorityNodesCount()));
        }
        return false;
    }

//    public static <T> CompletableFuture<List<T>> fromSuccessfulFutures(List<CompletableFuture<T>> futures) {
//        if (futures.isEmpty()) {
//            return CompletableFuture.completedFuture(List.of());
//        }
//        List<T> result = new ArrayList<>();
//        return futures.stream()
//                .reduce((f1, f2) -> f1.handle((s, throwable) -> {
//                            if (throwable != null) {
//                                result.add(null);
//                            } else {
//                                result.add(s);
//                            }
//                            return s;
//                        }).thenCompose(ignored -> f2))
//                .get()
//                .handle((s, throwable) -> {
//                    if (throwable != null) {
//                        result.add(null);
//                    } else {
//                        result.add(s);
//                    }
//                    return s;
//                }).thenApply(aVoid -> result);
//    }
}
