package org.ipoliakov.dmap.rpc.config;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.rpc.EnableRpc;
import org.ipoliakov.dmap.rpc.command.Command;
import org.ipoliakov.dmap.rpc.commons.ProtoMessageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = EnableRpc.class)
public class CommandConfig {

    @Bean
    public ProtoMessageRegistry protoMessageRegistry() {
        return new ProtoMessageRegistry();
    }

    @Bean
    public EnumMap<PayloadType, Command> commandMap(List<Command> commands) {
        return commands.stream()
            .collect(Collectors.toMap(
                Command::getPayloadType,
                command -> command,
                (k1, k2) -> {
                    throw new IllegalArgumentException("Duplicate commands for payloadType " + k1.getClass() + " and " + k2.getClass());
                },
                () -> new EnumMap<>(PayloadType.class)
            ));
    }
}
