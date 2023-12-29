package org.ipoliakov.dmap.node.internal.cluster.raft.command;

import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.node.internal.cluster.raft.Role;
import org.ipoliakov.dmap.node.internal.cluster.raft.election.ElectionService;
import org.ipoliakov.dmap.node.internal.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesRes;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppendEntriesCommand implements Command<AppendEntriesReq> {

    private final RaftState raftState;
    private final ElectionService electionService;

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
        return response(true);
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

