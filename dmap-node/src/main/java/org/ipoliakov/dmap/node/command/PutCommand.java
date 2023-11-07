package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageMutationService;
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
public class PutCommand implements Command<PutReq> {

    private final StorageMutationService txLoggingStorageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PutReq message) {
        ByteString prevVal = txLoggingStorageService.put(message);
        return PutRes.newBuilder()
                .setValue(prevVal)
                .setPayloadType(PayloadType.PUT_RES)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
