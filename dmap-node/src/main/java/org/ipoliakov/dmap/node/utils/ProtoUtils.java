package org.ipoliakov.dmap.node.utils;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.ValueRes;

import com.google.protobuf.ByteString;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtoUtils {

    public static ValueRes valueRes(ByteString value) {
        return ValueRes.newBuilder()
                .setValue(value)
                .setPayloadType(PayloadType.VALUE_RES)
                .build();
    }
}
