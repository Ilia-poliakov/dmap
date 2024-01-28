package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageReadOnlyService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.storage.GetReq;
import org.ipoliakov.dmap.rpc.command.Command;
import org.ipoliakov.dmap.util.ProtoMessages;
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
        return ProtoMessages.valueRes(value);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.GET_REQ;
    }
}
