package org.ipoliakov.dmap.node.cluster.raft.command;

import org.ipoliakov.dmap.node.cluster.raft.RaftLog;
import org.ipoliakov.dmap.node.cluster.raft.state.RaftState;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.RequestVoteReq;
import org.ipoliakov.dmap.protocol.raft.RequestVoteRes;
import org.ipoliakov.dmap.rpc.command.Command;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestVoteCommand implements Command<RequestVoteReq> {

    private final RaftLog raftLog;
    private final RaftState raftState;

    @Override
    @SuppressWarnings("checkstyle:JavaNCSS")
    public MessageLite execute(ChannelHandlerContext ctx, RequestVoteReq req) {
        log.debug("RequestVoteCmd: req = {}", req);

        if (req.getCandidateId() == raftState.getSelfId()) {
            log.info("Vote {} rejected. Request vote from itself", req);
            return response(false);
        }
        if (raftState.getCurrentTerm() > req.getTerm()) {
            log.info("Vote {} rejected. Current term {} is greater", req, raftState.getCurrentTerm());
            return response(false);
        }
        checkTerm(req);
        if (raftState.haveLeader() && req.getCandidateId() != raftState.getLeaderId()) {
            log.info("Vote {} rejected. We have a leader with id = {}", req, raftState.getLeaderId());
            return response(false);
        }
        if (raftState.alreadyVoted()) {
            return response(req.getCandidateId() == raftState.getVotedFor());
        }
        if (raftLog.getLastTerm() > req.getTerm()) {
            log.info("Vote {} rejected. Current last log term {} is greater", req, raftLog.getLastTerm());
            return response(false);
        }
        if (raftLog.getLastTerm() == req.getLastLogTerm() && raftLog.getLastIndex() > req.getLastLogIndex()) {
            log.info("Vote {} rejected. Current last log index {} is greater", req, raftLog.getLastIndex());
            return response(false);
        }

        log.info("Vote granted for " + req);
        return response(true);
    }

    private void checkTerm(RequestVoteReq req) {
        if (raftState.getCurrentTerm() < req.getTerm()) {
            raftState.becomeFollower(req.getTerm());
        }
    }

    private RequestVoteRes response(boolean granted) {
        return RequestVoteRes.newBuilder()
                .setPayloadType(PayloadType.REQUEST_VOTE_RES)
                .setTerm(raftState.getCurrentTerm())
                .setVoterId(raftState.getSelfId())
                .setVoteGranted(granted)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.REQUEST_VOTE_REQ;
    }
}
