package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.node.service.StorageMutationService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.ipoliakov.dmap.rpc.command.Command;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PutCommand implements Command<PutReq> {

    @Qualifier("replicatedStorageService")
    private final StorageMutationService storageService;

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, PutReq message) {
        ByteString prevVal = storageService.put(message);
        return ProtoMessages.valueRes(prevVal);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.PUT_REQ;
    }
}
