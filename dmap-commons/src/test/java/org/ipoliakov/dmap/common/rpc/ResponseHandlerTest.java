package org.ipoliakov.dmap.common.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.storage.ValueRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

class ResponseHandlerTest {

    @Test
    @Timeout(10)
    void channelRead0_completeResponseFuture() throws Exception{
        long messageId = 1L;
        var responseFutures = new ResponseFutures();
        CompletableFuture<MessageLite> future = new CompletableFuture<>();
        responseFutures.add(messageId, future);
        ResponseHandler responseHandler = new ResponseHandler(responseFutures, new ProtoMessageRegistry());
        ByteString expectedPayload = ValueRes.newBuilder().setValue(ByteString.copyFromUtf8("value")).build().toByteString();
        DMapMessage message = DMapMessage.newBuilder()
            .setMessageId(messageId)
            .setPayloadType(PayloadType.VALUE_RES)
            .setPayload(expectedPayload)
            .build();
        responseHandler.channelRead0(null, message);
        assertEquals(expectedPayload, future.get().toByteString());
    }
}