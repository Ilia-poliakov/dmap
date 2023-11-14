package org.ipoliakov.dmap.node;

import org.ipoliakov.dmap.node.server.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@PropertySource("classpath:dmap.properties")
@ComponentScan(basePackageClasses = Main.class)
public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);

        InitializerRunner initializerRunner = context.getBean(InitializerRunner.class);
        initializerRunner.run();

        Server server = context.getBean(Server.class);
        server.start();
    }
}
