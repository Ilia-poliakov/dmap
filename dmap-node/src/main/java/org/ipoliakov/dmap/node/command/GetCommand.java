package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.storage.Storage;
import org.ipoliakov.dmap.protocol.GetReq;
import org.ipoliakov.dmap.protocol.GetRes;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetCommand implements Command<GetReq> {

    private final Storage storage;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, GetReq message) {
        ByteString value = storage.get(message.getKey());
        return GetRes.newBuilder()
                .setValue(value != null ? value : ByteString.EMPTY)
                .setPayloadType(PayloadType.GET_RES)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.GET_REQ;
    }
}
