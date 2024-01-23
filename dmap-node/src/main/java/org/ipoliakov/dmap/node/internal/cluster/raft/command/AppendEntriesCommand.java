package org.ipoliakov.dmap.node.internal.cluster.raft.command;

import java.util.List;

import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.node.internal.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.ipoliakov.dmap.node.internal.cluster.raft.election.ElectionService;
import org.ipoliakov.dmap.node.internal.cluster.raft.log.operation.OperationExecutor;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesRes;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppendEntriesCommand implements Command<AppendEntriesReq> {

    private final RaftLog raftLog;
    private final RaftState raftState;
    private final ElectionService electionService;
    private final OperationExecutor operationExecutor;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, AppendEntriesReq req) {
        log.debug("AppendEntriesCommand: req = {}", req);

        if (req.getTerm() < raftState.getCurrentTerm()) {
            log.warn("Request from old term. Reply false. currentTerm = {}, req = {}", raftState.getCurrentTerm(), req);
            return response(false);
        }
        if (req.getTerm() > raftState.getCurrentTerm() || raftState.getRole() != Role.FOLLOWER) {
            log.info("Becoming follower from {}. term = {}, new term = {}, now leader id = {}",
                    raftState.getRole(), raftState.getCurrentTerm(), req.getTerm(), req.getLeaderId());
            raftState.becomeFollower(req.getTerm());
        }
        if (req.getLeaderId() != raftState.getLeaderId()) {
            log.info("Updating leader id: " + req.getLeaderId());
            raftState.setLeaderId(req.getLeaderId());
        }
        electionService.restartElectionTask();
        if (!lastLogEntryIsCorrect(req)) {
            return response(false);
        }
        applyOperations(req.getEntriesList());
        return response(true);
    }

    private boolean lastLogEntryIsCorrect(AppendEntriesReq req) {
        if (req.getPrevLogIndex() < 1) {
            return true;
        }
        long lastLogIndex = raftLog.getLastIndex();
        int prevLogTerm = req.getPrevLogIndex() == lastLogIndex ? raftLog.getLastTerm() : readTermByIndex(req.getPrevLogIndex());
        return req.getPrevLogTerm() == prevLogTerm;
    }

    private int readTermByIndex(long index) {
        return raftLog.readByIndex(index)
                .map(Operation::getTerm)
                .orElse(-1);
    }

    private void applyOperations(List<Operation> operation) {
        for (Operation op : operation) {
            operationExecutor.execute(op);
            raftLog.append(op);
        }
    }

    private AppendEntriesRes response(boolean success) {
        return AppendEntriesRes.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_RES)
                .setTerm(raftState.getCurrentTerm())
                .setSuccess(success)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.APPEND_ENTRIES_REQ;
    }
}

