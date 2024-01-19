package org.ipoliakov.dmap.it;

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.awaitility.Awaitility;
import org.ipoliakov.dmap.client.ClientBuilder;
import org.ipoliakov.dmap.client.CrdtClient;
import org.ipoliakov.dmap.client.KvStorageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ClusterIntegrationTest {

    private static final int DEFAULT_PORT = 9090;

    protected DockerComposeContainer<?> environment;
    protected List<KvStorageClient<String, String>> storageClients;
    protected List<CrdtClient> crdtClients;

    private Integer leaderId;

    @BeforeEach
    void setUp() throws TimeoutException {
        var waitingConsumer = new WaitingConsumer();

        environment = new DockerComposeContainer(new File("../deploy/docker/docker-compose.yml"))
                .withExposedService("node1", DEFAULT_PORT)
                .withExposedService("node2", DEFAULT_PORT)
                .withExposedService("node3", DEFAULT_PORT)
                .withLogConsumer("node1", waitingConsumer)
                .withLogConsumer("node2", waitingConsumer)
                .withLogConsumer("node3", waitingConsumer)
                .withOptions("--compatibility")
                .withLocalCompose(true);
        environment.start();
        waitingConsumer.waitUntil((OutputFrame outputFrame) -> outputFrame.getUtf8String().contains("BECOME THE LEADER"), 5, TimeUnit.MINUTES);

        List<ClientBuilder.ClientConfigurator> configurators = List.of(
                connect("node1"),
                connect("node2"),
                connect("node3")
        );
        storageClients = configurators.stream()
                .map(nodeConfigurator -> nodeConfigurator.keyValueStorageBuilder().build())
                .toList();
        crdtClients = configurators.stream()
                .map(nodeConfigurator -> nodeConfigurator.crdtClientBuilder().build())
                .toList();

        leaderId = findLeaderId(waitingConsumer);
    }

    public KvStorageClient<String, String> getLeaderClient() {
        return storageClients.get(leaderId - 1);
    }

    private static Integer findLeaderId(WaitingConsumer waitingConsumer) {
        return Awaitility.await().ignoreExceptions().until(() -> Integer.valueOf(
                waitingConsumer
                        .getFrames()
                        .stream()
                        .filter(frame -> frame.getUtf8String().contains("leaderId: "))
                        .findFirst().get()
                        .getUtf8String()
                        .split(" ")[1]
                        .strip()), Objects::nonNull);
    }

    private ClientBuilder.ClientConfigurator connect(String serviceName) {
        return await()
                .ignoreExceptions()
                .until(() -> new ClientBuilder()
                                .setHost(environment.getServiceHost(serviceName, DEFAULT_PORT))
                                .setPort(environment.getServicePort(serviceName, DEFAULT_PORT))
                                .connect(),
                        c -> true
                );
    }

    @AfterEach
    void tearDown() {
        environment.stop();
    }
}
