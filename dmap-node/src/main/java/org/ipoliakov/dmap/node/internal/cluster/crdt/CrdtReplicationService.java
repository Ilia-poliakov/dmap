package org.ipoliakov.dmap.node.internal.cluster.crdt;

import java.util.Map;

import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounter;
import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounterSnapshot;
import org.ipoliakov.dmap.node.internal.cluster.Cluster;
import org.ipoliakov.dmap.node.internal.cluster.crdt.mapping.PnCounterMapper;
import org.ipoliakov.dmap.protocol.crdt.PnCounterReplicationRes;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrdtReplicationService {

    private final Cluster cluster;
    private final PnCounterMapper mapper;

    public void replicate(Map<String, PnCounter> counters) {
        var req = ProtoMessages.pnCounterReplicationReq();
        for (Map.Entry<String, PnCounter> counterEntry : counters.entrySet()) {
            PnCounterSnapshot snapshot = counterEntry.getValue().snapshot();
            String name = counterEntry.getKey();
            req.addReplicationData(mapper.toPnCounterReplicationData(snapshot, name));
        }
        cluster.sendToAll(req.build(), PnCounterReplicationRes.class);
    }
}
