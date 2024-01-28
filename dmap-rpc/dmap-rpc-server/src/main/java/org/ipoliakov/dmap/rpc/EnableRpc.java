package org.ipoliakov.dmap.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ipoliakov.dmap.rpc.config.CommandConfig;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CommandConfig.class)
public @interface EnableRpc {
}
