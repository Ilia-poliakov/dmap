package org.ipoliakov.dmap.common.network;

import org.ipoliakov.dmap.common.MonotonicallyIdGenerator;
import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.protocol.storage.ValueRes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.protobuf.ByteString;

import io.netty.channel.Channel;

class MessageSenderTest {

    @Test
    void send() {
        Channel channel = Mockito.mock(Channel.class);
        var sender = new MessageSender(channel, new MonotonicallyIdGenerator(1), new ResponseFutures(), new ProtoMessageRegistry());
        PutReq putReq = PutReq.newBuilder()
            .setPayloadType(PayloadType.PUT_REQ)
            .setKey(ByteString.copyFromUtf8("key"))
            .setValue(ByteString.copyFromUtf8("value"))
            .build();
        sender.send(putReq, ValueRes.class);

        DMapMessage expectedMessage = DMapMessage.newBuilder()
            .setMessageId(1)
            .setPayload(putReq.toByteString())
            .setPayloadType(putReq.getPayloadType())
            .build();
        Mockito.verify(channel).writeAndFlush(expectedMessage, channel.voidPromise());
    }
}