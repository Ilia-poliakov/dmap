package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.client.PutReq;
import org.ipoliakov.dmap.util.ProtoUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PutCommand implements Command<PutReq> {

    @Qualifier("txLoggingStorageService")
    private final StorageMutationService storageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PutReq message) {
        ByteString prevVal = storageService.put(message);
        return ProtoUtils.valueRes(prevVal);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
