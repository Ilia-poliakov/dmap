package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.storage.Storage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.PutRes;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PutCommand implements Command {

    private final Storage storage;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, MessageLite message) {
        PutReq putReq = (PutReq) message;
        ByteString prevVal = storage.put(putReq.getKey(), putReq.getValue());
        return PutRes.newBuilder()
            .setValue(prevVal != null ? prevVal : ByteString.EMPTY)
            .setPayloadType(PayloadType.PUT_RES)
            .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
