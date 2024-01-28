package org.ipoliakov.dmap.node;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ipoliakov.dmap.client.ClientBuilder;
import org.ipoliakov.dmap.client.CrdtClient;
import org.ipoliakov.dmap.client.KvStorageClient;
import org.ipoliakov.dmap.node.service.StorageServiceImpl;
import org.ipoliakov.dmap.rpc.server.Server;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("test")
@Configuration
@PropertySource({"classpath:dmap.properties", "classpath:dmap-test.properties"})
public class TestConfig implements InitializingBean {

    @Autowired
    private Server server;

    @Override
    public void afterPropertiesSet() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> server.start());
    }

    @Bean
    public StorageServiceImpl replicatedStorageService(StorageServiceImpl storageService) {
        return storageService;
    }

    @Bean
    ClientBuilder.ClientConfigurator clientConfigurator() {
        return await()
                .ignoreExceptions()
                .until(() -> new ClientBuilder()
                                .setPort(9090)
                                .setHost("localhost")
                                .setThreadCount(1)
                                .connect(),
                        client -> true
                );
    }

    @Bean
    KvStorageClient<String, String> storageClient(ClientBuilder.ClientConfigurator clientConfigurator) {
        return clientConfigurator.keyValueStorageBuilder().build();
    }

    @Bean
    CrdtClient crdtClient(ClientBuilder.ClientConfigurator clientConfigurator) {
        return clientConfigurator.crdtClientBuilder().build();
    }
}
