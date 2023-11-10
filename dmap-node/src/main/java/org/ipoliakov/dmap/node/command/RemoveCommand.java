package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.node.utils.ProtoUtils;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.RemoveReq;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveCommand implements Command<RemoveReq> {

    private final StorageMutationService txLoggingStorageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, RemoveReq req) {
        ByteString removedValue = txLoggingStorageService.remove(req);
        return ProtoUtils.valueRes(removedValue);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.REMOVE_REQ;
    }
}