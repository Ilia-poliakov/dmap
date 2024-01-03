package org.ipoliakov.dmap.client.internal;

import java.util.concurrent.CompletableFuture;

import org.ipoliakov.dmap.common.network.ProtoMessageRegistry;
import org.ipoliakov.dmap.common.network.ResponseFutures;
import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.GetLeaderRes;

import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClientResponseHandler extends SimpleChannelInboundHandler<DMapMessage> {

    private final ResponseFutures responseFutures;
    private final ClientMessageSender messageSender;
    private final ProtoMessageRegistry messageRegistry;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DMapMessage message) {
        if (message.getPayloadType() == PayloadType.GET_LEADER_RES) {
            refreshLeader(message);
        }
        long messageId = message.getMessageId();
        CompletableFuture future = responseFutures.get(messageId);
        MessageLite payload = messageRegistry.parsePayload(message);
        future.complete(payload);
    }

    private void refreshLeader(DMapMessage message) {
        GetLeaderRes leaderRes = (GetLeaderRes) messageRegistry.parsePayload(message);
        messageSender.refreshLeader(new Endpoint(leaderRes.getHost(), leaderRes.getPort()));
    }
}
