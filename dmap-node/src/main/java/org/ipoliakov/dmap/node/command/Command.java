package org.ipoliakov.dmap.node.command;

import org.ipoliakov.dmap.protocol.PayloadType;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;

public interface Command {

    MessageLite execute(ChannelHandlerContext ctx, MessageLite message);

    PayloadType getPayloadType();
}
