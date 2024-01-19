package org.ipoliakov.dmap.util;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.crdt.PnCounterAddAndGetReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterAddAndGetRes;
import org.ipoliakov.dmap.protocol.crdt.PnCounterGetReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterGetRes;
import org.ipoliakov.dmap.protocol.crdt.PnCounterReplicationReq;
import org.ipoliakov.dmap.protocol.crdt.PnCounterReplicationRes;
import org.ipoliakov.dmap.protocol.crdt.PnCounterSnapshot;
import org.ipoliakov.dmap.protocol.raft.AppendEntriesReq;
import org.ipoliakov.dmap.protocol.storage.GetReq;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.protocol.storage.RemoveReq;
import org.ipoliakov.dmap.protocol.storage.ValueRes;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtoMessages {

    public static final PnCounterReplicationRes PN_COUNTER_REPLICATION_RES = PnCounterReplicationRes.newBuilder()
            .setPayloadType(PayloadType.PN_COUNTER_REPLICATION_REQ)
            .build();

    public static GetReq getReq(ByteString key) {
        return GetReq.newBuilder()
                .setPayloadType(PayloadType.GET_REQ)
                .setKey(key)
                .build();
    }

    public static PutReq.Builder putReq() {
        return PutReq.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ);
    }

    public static RemoveReq removeReq(ByteString key) {
        return RemoveReq.newBuilder()
                .setPayloadType(PayloadType.REMOVE_REQ)
                .setKey(key)
                .build();
    }

    public static ValueRes valueRes(ByteString value) {
        return ValueRes.newBuilder()
                .setPayloadType(PayloadType.VALUE_RES)
                .setValue(value)
                .build();
    }

    public static AppendEntriesReq.Builder appendEntriesReq() {
        return AppendEntriesReq.newBuilder()
                .setPayloadType(PayloadType.APPEND_ENTRIES_REQ);
    }

    public static PnCounterReplicationReq.Builder pnCounterReplicationReq() {
        return PnCounterReplicationReq.newBuilder()
                .setPayloadType(PayloadType.PN_COUNTER_REPLICATION_REQ);
    }

    public static PnCounterReplicationRes pnCounterReplicationRes() {
        return PN_COUNTER_REPLICATION_RES;
    }

    public static PnCounterGetReq.Builder pnCounterGetReq(String name) {
        return PnCounterGetReq.newBuilder()
                .setPayloadType(PayloadType.PN_COUNTER_GET_REQ)
                .setName(name);
    }

    public static MessageLite pnCounterGetRes(PnCounterSnapshot value) {
        return PnCounterGetRes.newBuilder()
                .setPayloadType(PayloadType.PN_COUNTER_GET_RES)
                .setValue(value)
                .build();
    }

    public static PnCounterAddAndGetReq.Builder pnCounterAddAndGetReq(String name, long delta) {
        return PnCounterAddAndGetReq.newBuilder()
                .setPayloadType(PayloadType.PN_COUNTER_ADD_AND_GET_REQ)
                .setName(name)
                .setDelta(delta);
    }

    public static PnCounterAddAndGetRes pnCounterAddAndGetRes(PnCounterSnapshot value) {
        return PnCounterAddAndGetRes.newBuilder()
                .setPayloadType(PayloadType.PN_COUNTER_ADD_AND_GET_RES)
                .setValue(value)
                .build();
    }
}
