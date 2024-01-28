package org.ipoliakov.dmap.node.cluster.crdt.command;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.node.cluster.crdt.PnCounterService;
import org.ipoliakov.dmap.node.cluster.crdt.mapping.PnCounterMapper;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.crdt.PnCounterGetReq;
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
public class PnCounterGetReqCmd implements Command<PnCounterGetReq> {

    private final PnCounterMapper mapper;
    private final PnCounterService pnCounterService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PnCounterGetReq message) {
        log.debug("Get pnCounter value - start: request = {}", message);

        VectorClockSnapshot timestamp = message.getTimestamp();
        StampedLong value = pnCounterService.get(message.getName(), new VectorClock(timestamp.getTimestampByNodesMap()));
        PnCounterSnapshot result = mapper.toSnapshot(value);

        log.debug("Get pnCounter value - end: response = {}", result);
        return ProtoMessages.pnCounterGetRes(result);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PN_COUNTER_GET_REQ;
    }
}
