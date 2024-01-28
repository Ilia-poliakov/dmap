package org.ipoliakov.dmap.node.cluster.crdt.command;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.node.cluster.crdt.PnCounterService;
import org.ipoliakov.dmap.node.cluster.crdt.mapping.PnCounterMapper;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.crdt.PnCounterAddAndGetReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.crdt.VectorClockSnapshot;
import org.ipoliakov.dmap.rpc.command.Command;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PnCounterAddAndGetReqCmd implements Command<PnCounterAddAndGetReq> {

    private final PnCounterMapper mapper;
    private final PnCounterService pnCounterService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PnCounterAddAndGetReq req) {
        log.debug("Add and get pnCounter - start: request = {}", req);

        VectorClockSnapshot timestamp = req.getTimestamp();
        StampedLong result = pnCounterService.addAndGet(req.getName(), req.getDelta(), new VectorClock(timestamp.getTimestampByNodesMap()));
        PnCounterSnapshot snapshot = mapper.toSnapshot(result);

        log.debug("Add and get pnCounter - end: result = {}", result);
        return ProtoMessages.pnCounterAddAndGetRes(snapshot);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PN_COUNTER_ADD_AND_GET_REQ;
    }
}
