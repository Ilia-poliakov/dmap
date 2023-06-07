package org.ipoliakov.dmap.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.GetReq;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class ProtoMessageFactoryTest {

    private static final ProtoMessageFactory messageFactory = new ProtoMessageFactory();

    @Test
    void parsePayload() {
        GetReq getReq = GetReq.newBuilder()
                .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
                .setPayloadType(messageFactory.getPayloadType(GetReq.class))
                .build();
        assertEquals(PayloadType.GET_REQ, getReq.getPayloadType());
    }

    @Test
    void getPayloadType() {
        GetReq getReq = GetReq.newBuilder()
                .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
                .setPayloadType(messageFactory.getPayloadType(GetReq.class))
                .build();
        DMapMessage message = DMapMessage.newBuilder()
                .setMessageId(1)
                .setPayloadType(getReq.getPayloadType())
                .setPayload(getReq.toByteString())
                .build();

        GetReq getReqActual = (GetReq) messageFactory.parsePayload(message);

        assertEquals(getReq, getReqActual);
    }
}