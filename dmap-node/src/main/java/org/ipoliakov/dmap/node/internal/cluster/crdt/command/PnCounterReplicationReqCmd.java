package org.ipoliakov.dmap.node.internal.cluster.crdt.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ipoliakov.dmap.datastructures.VectorClock;
import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounter;
import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.node.internal.cluster.crdt.PnCounterService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PnCounterReplicationData;
import org.ipoliakov.dmap.protocol.PnCounterStateData;
import org.ipoliakov.dmap.protocol.internal.PnCounterReplicationReq;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PnCounterReplicationReqCmd implements Command<PnCounterReplicationReq> {

    private final PnCounterService pnCounterService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PnCounterReplicationReq req) {
        log.debug("PnCounter replication request - start. req = {}", req);

        List<PnCounterReplicationData> replicationDataList = req.getReplicationDataList();
        Map<String, PnCounter> counters = toMap(replicationDataList);
        pnCounterService.merge(counters);

        log.debug("PnCounter replication request - end}");
        return ProtoMessages.pnCounterReplicationRes();
    }

    private Map<String, PnCounter> toMap(List<PnCounterReplicationData> replicationDataList) {
        Map<String, PnCounter> result = new HashMap<>();
        for (PnCounterReplicationData replicationData : replicationDataList) {
            String name = replicationData.getName();
            Map<Integer, long[]> states = toStateMap(replicationData);
            VectorClock clock = new VectorClock(replicationData.getTimestamp().getTimestampByNodesMap());
            PnCounter counter = new PnCounter(states, clock);
            result.put(name, counter);
        }
        return result;
    }

    private static Map<Integer, long[]> toStateMap(PnCounterReplicationData replicationData) {
        return replicationData.getStateList()
            .stream().collect(Collectors.toMap(
                PnCounterStateData::getNodeId,
                pnCounterStateData -> new long[]{pnCounterStateData.getPCounter(), pnCounterStateData.getNCounter()}
            ));
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PN_COUNTER_REPLICATION_REQ;
    }
}
