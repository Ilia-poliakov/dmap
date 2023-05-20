package org.ipoliakov.dmap.node.network;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.GetRes;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

@ExtendWith(MockitoExtension.class)
class DispatcherCommandHandlerTest {

    @Test
    void when_duplicateCommand_shouldThrow() {
        Command command = Mockito.mock(Command.class);
        when(command.getPayloadType()).thenReturn(PayloadType.GET_REQ);
        assertThrows(IllegalArgumentException.class, () -> new DispatcherCommandHandler(List.of(command, command), null));
    }

    @Test
    void unknownCommand() throws Exception {
        Command command = Mockito.mock(Command.class);
        ProtoMessageFactory protoMessageFactory = Mockito.mock(ProtoMessageFactory.class);
        ChannelHandlerContext channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        Channel channel = Mockito.mock(Channel.class);
        when(channelHandlerContext.channel()).thenReturn(channel);

        DMapMessage message = DMapMessage.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setMessageId(1)
                .setPayload(PutReq.getDefaultInstance().toByteString())
                .build();
        when(protoMessageFactory.parsePayload(message)).thenReturn(GetRes.getDefaultInstance());

        when(command.getPayloadType()).thenReturn(PayloadType.GET_REQ);
        var dispatcher = new DispatcherCommandHandler(List.of(command), protoMessageFactory);

        dispatcher.channelRead(channelHandlerContext, message);

        verify(command, never()).execute(any(), any());
        verify(channel, only()).writeAndFlush(any(DMapMessage.Builder.class));
    }
}