package org.ipoliakov.dmap.it;

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.awaitility.Awaitility;
import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.client.serializer.StringSerializer;
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
    protected List<DMapClient<String, String>> clients = new ArrayList<>();

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

        clients.add(createClient("node1"));
        clients.add(createClient("node2"));
        clients.add(createClient("node3"));

        leaderId = findLeaderId(waitingConsumer);
    }

    public DMapClient<String, String> getLeaderClient() {
        return clients.get(leaderId - 1);
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

    private DMapClient<String, String> createClient(String serviceName) {
        return await()
                .ignoreExceptions()
                .until(() -> DMapClient.builder()
                                .setHost(environment.getServiceHost(serviceName, DEFAULT_PORT))
                                .setPort(environment.getServicePort(serviceName, DEFAULT_PORT))
                                .build(new StringSerializer(), new StringSerializer()),
                        c -> true
                );
    }

    @AfterEach
    void tearDown() {
        environment.stop();
    }
}
