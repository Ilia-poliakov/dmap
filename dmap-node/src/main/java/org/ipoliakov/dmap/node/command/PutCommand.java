package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PutCommand implements Command {

    private final StorageService txLoggingStorageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, MessageLite message) {
        PutReq putReq = (PutReq) message;
        return txLoggingStorageService.put(putReq);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
