package org.ipoliakov.dmap.node;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ipoliakov.dmap.client.ClientBuilder;
import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.client.serializer.StringSerializer;
import org.ipoliakov.dmap.node.storage.SimpleMapStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
public abstract class IntegrationTest {

    @Autowired
    private Server server;
    @Autowired
    protected SimpleMapStorage storage;

    protected DMapClient<String, String> client;

    @BeforeEach
    void setUp() {
        ExecutorService serverThread = Executors.newSingleThreadExecutor();
        serverThread.execute(() -> server.start());

        client = new ClientBuilder()
                .setPort(9090)
                .setHost("localhost")
                .setThreadCount(1)
                .build(new StringSerializer(), new StringSerializer());
    }

    @AfterEach
    void tearDown() {
        storage.clear();
    }
}
