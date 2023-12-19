package org.ipoliakov.dmap.it;

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.time.Duration;

import org.ipoliakov.dmap.client.ClientBuilder;
import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.client.serializer.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

public abstract class ClusterIntegrationTest {

    private static final int DEFAULT_PORT = 9090;
    private static final WaitStrategy WAIT_FOR_CONNECTED = Wait.forListeningPort().forPorts(DEFAULT_PORT).withStartupTimeout(Duration.ofSeconds(60));

    protected DockerComposeContainer environment;
    protected DMapClient<String, String> client;

    @BeforeEach
    void setUp() {
        environment = new DockerComposeContainer(new File("../deploy/docker/docker-compose.yml"))
                .withExposedService("node1", DEFAULT_PORT, WAIT_FOR_CONNECTED)
                .withExposedService("node2", DEFAULT_PORT, WAIT_FOR_CONNECTED)
                .withExposedService("node3", DEFAULT_PORT, WAIT_FOR_CONNECTED)
                .withOptions("--compatibility")
                .withLocalCompose(true);
        environment.start();

        String nodeHost = environment.getServiceHost("node1", DEFAULT_PORT);
        Integer nodePort = environment.getServicePort("node1", DEFAULT_PORT);
        client = await()
                .ignoreExceptions()
                .until(() -> new ClientBuilder()
                                .setHost(nodeHost)
                                .setPort(nodePort)
                                .build(new StringSerializer(), new StringSerializer()),
                        c -> true
                );
    }

    @AfterEach
    void tearDown() {
        environment.stop();
    }
}
