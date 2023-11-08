package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageReadOnlyService;
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

    private final StorageReadOnlyService storageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, GetReq message) {
        ByteString value = storageService.get(message.getKey());
        return GetRes.newBuilder()
                .setValue(value)
                .setPayloadType(PayloadType.GET_RES)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.GET_REQ;
    }
}
