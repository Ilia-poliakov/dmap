package org.ipoliakov.dmap.node;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.client.serializer.StringSerializer;
import org.ipoliakov.dmap.node.server.Server;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfig implements InitializingBean {

    @Autowired
    private Server server;

    @Override
    public void afterPropertiesSet() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> server.start());
    }

    @Bean
    DMapClient<String, String> client() {
        return await()
                .ignoreExceptions()
                .until(() -> DMapClient.builder()
                        .setPort(9090)
                        .setHost("localhost")
                        .setThreadCount(1)
                        .build(new StringSerializer(), new StringSerializer()),
                        client -> true
                );
    }
}
