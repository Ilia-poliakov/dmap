package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.node.utils.ProtoUtils;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PutCommand implements Command<PutReq> {

    private final StorageMutationService txLoggingStorageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PutReq message) {
        ByteString prevVal = txLoggingStorageService.put(message);
        return ProtoUtils.valueRes(prevVal);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
