package org.ipoliakov.dmap.util;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.client.ValueRes;

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
