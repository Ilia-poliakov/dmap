package org.ipoliakov.dmap.rpc.command;

import org.ipoliakov.dmap.protocol.PayloadType;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;

public interface Command<M extends MessageLite>  {

    MessageLite execute(ChannelHandlerContext ctx, M message);

    PayloadType getPayloadType();
}
