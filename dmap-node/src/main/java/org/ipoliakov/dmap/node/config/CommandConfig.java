package org.ipoliakov.dmap.node.config;

import org.ipoliakov.dmap.common.ProtoMessageFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

    @Bean
    public ProtoMessageFactory protoMessageFactory() {
        return new ProtoMessageFactory();
    }
}
