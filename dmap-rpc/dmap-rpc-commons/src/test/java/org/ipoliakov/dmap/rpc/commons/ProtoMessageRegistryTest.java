package org.ipoliakov.dmap.rpc.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.protocol.storage.GetReq;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

class ProtoMessageRegistryTest {

    private static final ProtoMessageRegistry messageRegistry = new ProtoMessageRegistry();

    @Test
    void parsePayload() {
        GetReq getReq = GetReq.newBuilder()
            .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
            .setPayloadType(messageRegistry.getPayloadType(GetReq.class))
            .build();
        assertEquals(PayloadType.GET_REQ, getReq.getPayloadType());
    }

    @Test
    void getPayloadType() {
        GetReq getReq = GetReq.newBuilder()
            .setKey(ByteString.copyFrom("key", StandardCharsets.UTF_8))
            .setPayloadType(messageRegistry.getPayloadType(GetReq.class))
            .build();
        DMapMessage message = DMapMessage.newBuilder()
            .setMessageId(1)
            .setPayloadType(getReq.getPayloadType())
            .setPayload(getReq.toByteString())
            .build();

        GetReq getReqActual = (GetReq) messageRegistry.parsePayload(message);

        assertEquals(getReq, getReqActual);
    }

    @Test
    void parsePayloadOperation() {
        PutReq putReq = PutReq.newBuilder()
            .setPayloadType(PayloadType.PUT_REQ)
            .setKey(ByteString.copyFromUtf8("key"))
            .setValue(ByteString.copyFromUtf8("value"))
            .build();
        MessageLite parsedOperation = messageRegistry.parsePayload(
            Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setMessage(putReq.toByteString())
                .build()
        );
        assertEquals(putReq.toByteString(), parsedOperation.toByteString());
    }

    @Test
    void parsePayloadOperation_incorrectProto() {
        assertThrows(IllegalArgumentException.class, () ->
            messageRegistry.parsePayload(
                Operation.newBuilder()
                    .setPayloadType(PayloadType.PUT_REQ)
                    .setMessage(ByteString.copyFromUtf8("incorrect proto"))
                    .build()
            ));
    }
}