package org.ipoliakov.dmap.node.network;

import static io.netty.channel.ChannelHandler.Sharable;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.node.command.DefaultCommand;
import org.ipoliakov.dmap.protocol.DMapMessage;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Sharable
public class DispatcherCommandHandler extends ChannelInboundHandlerAdapter {

    private static final DefaultCommand DEFAULT_COMMAND = new DefaultCommand();

    private final ProtoMessageFactory messageFactory;
    private final EnumMap<PayloadType, Command> commandMap;

    public DispatcherCommandHandler(List<Command> commands, ProtoMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
        commandMap = commands.stream()
            .collect(Collectors.toMap(
                Command::getPayloadType,
                command -> command,
                (k1, k2) -> {throw new IllegalArgumentException("Duplicate commands for payloadType " + k1.getClass() + " and " + k2.getClass());},
                () -> new EnumMap<>(PayloadType.class)
            ));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DMapMessage message = (DMapMessage) msg;
        Command command = commandMap.getOrDefault(message.getPayloadType(), DEFAULT_COMMAND);
        Message response = (Message) command.execute(ctx, messageFactory.parsePayload(message));
        ctx.channel().writeAndFlush(
            DMapMessage.newBuilder()
                .setMessageId(message.getMessageId())
                .setPayloadType(getPayloadType(response))
                .setPayload(response.toByteString())
        );
    }

    private PayloadType getPayloadType(Message message) {
        var payloadTypeDescriptor = (Descriptors.EnumValueDescriptor) message.getField(message.getDescriptorForType().findFieldByNumber(1));
        return PayloadType.forNumber(payloadTypeDescriptor.getNumber());
    }
}
