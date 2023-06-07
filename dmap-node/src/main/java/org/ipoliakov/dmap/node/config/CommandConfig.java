package org.ipoliakov.dmap.node.config;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.ipoliakov.dmap.node.command.Command;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

    @Bean
    public ProtoMessageFactory protoMessageFactory() {
        return new ProtoMessageFactory();
    }

    @Bean
    public EnumMap<PayloadType, Command> commandMap(List<Command> commands) {
        return commands.stream()
                .collect(Collectors.toMap(
                        Command::getPayloadType,
                        command -> command,
                        (k1, k2) -> { throw new IllegalArgumentException("Duplicate commands for payloadType " + k1.getClass() + " and " + k2.getClass()); },
                        () -> new EnumMap<>(PayloadType.class)
                ));
    }
}
