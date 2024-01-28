package org.ipoliakov.dmap.rpc.command;

import org.ipoliakov.dmap.protocol.ErrorCode;
import org.ipoliakov.dmap.protocol.ErrorMessage;
import org.ipoliakov.dmap.protocol.PayloadType;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCommand implements Command<MessageLite> {

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, MessageLite message) {
        String errorMessage = "Unknown command: " + message;
        log.warn(errorMessage);
        return ErrorMessage.newBuilder()
                .setPayloadType(PayloadType.ERROR_RES)
                .setMessage(errorMessage)
                .setErrorCode(ErrorCode.UNKNOWN_COMMAND)
                .build();
    }

    @Override
    public PayloadType getPayloadType() {
        return null;
    }
}
