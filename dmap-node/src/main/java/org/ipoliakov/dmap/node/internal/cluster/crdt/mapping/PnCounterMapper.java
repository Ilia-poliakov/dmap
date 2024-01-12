package org.ipoliakov.dmap.node.internal.cluster.crdt.mapping;

import org.ipoliakov.dmap.datastructures.crdt.StampedLong;
import org.ipoliakov.dmap.datastructures.crdt.counters.PnCounterState;
import org.ipoliakov.dmap.protocol.PnCounterReplicationData;
import org.ipoliakov.dmap.protocol.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.PnCounterStateData;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface PnCounterMapper {

    @Mapping(target = "timestamp.timestampByNodes", ignore = true)
    @Mapping(source = "src.timestamp.map", target = "timestamp.mutableTimestampByNodes")
    PnCounterSnapshot toSnapshot(StampedLong src);

    @Mapping(source = "src.states", target = "stateList")
    @Mapping(source = "src.clock", target = "timestamp")
    @Mapping(target = "timestamp.timestampByNodes", ignore = true)
    @Mapping(source = "src.clock.map", target = "timestamp.mutableTimestampByNodes")
    PnCounterReplicationData toPnCounterReplicationData(
        org.ipoliakov.dmap.datastructures.crdt.counters.PnCounterSnapshot src, String name);

    @Mapping(source = "pCounter", target = "PCounter")
    @Mapping(source = "nCounter", target = "NCounter")
    PnCounterStateData toCounterStateData(PnCounterState src);
}
