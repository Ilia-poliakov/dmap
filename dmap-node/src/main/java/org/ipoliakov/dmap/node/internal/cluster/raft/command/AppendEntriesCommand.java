package org.ipoliakov.dmap.node.internal.cluster.raft.command;

import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.AppendEntriesReq;
import org.springframework.stereotype.Component;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppendEntriesCommand implements Command<AppendEntriesReq> {

    @Override
    public MessageLite execute(ChannelHandlerContext ctx, AppendEntriesReq req) {
        return null;
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.APPEND_ENTRIES_REQ;
    }
}
