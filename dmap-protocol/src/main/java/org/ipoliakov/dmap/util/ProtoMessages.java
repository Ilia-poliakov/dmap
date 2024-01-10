package org.ipoliakov.dmap.util;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.ipoliakov.dmap.protocol.ValueRes;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;

import com.google.protobuf.ByteString;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtoMessages {

    public static PutReq.Builder putReq() {
        return PutReq.newBuilder().setPayloadType(PayloadType.PUT_REQ);
    }

    public static RemoveReq removeReq(ByteString key) {
        return RemoveReq.newBuilder()
                .setKey(key)
                .setPayloadType(PayloadType.REMOVE_REQ)
                .build();
    }

    public static ValueRes valueRes(ByteString value) {
        return ValueRes.newBuilder()
                .setValue(value)
                .setPayloadType(PayloadType.VALUE_RES)
                .build();
    }

    public static AppendEntriesReq.Builder appendEntriesReq() {
        return AppendEntriesReq.newBuilder().setPayloadType(PayloadType.APPEND_ENTRIES_REQ);
    }
}
