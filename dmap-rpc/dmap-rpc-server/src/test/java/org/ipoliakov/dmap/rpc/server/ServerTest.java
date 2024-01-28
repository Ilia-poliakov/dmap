package org.ipoliakov.dmap.rpc.server;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

@Timeout(10)
class ServerTest {

    @Test
    void start() {
        Server server = new Server(
            9090,
            1, 1, 4,
            true, false,
            new ServerPipelineInitializer(Mockito.mock(DispatcherCommandHandler.class))
        );
        Thread serverThread = new Thread(server::start);

        serverThread.start();
        await().untilAsserted(() -> assertReachable("localhost", 9090));

        serverThread.interrupt();
        await().untilAsserted(() -> assertNotReachable("localhost", 9090));
    }

    private void assertNotReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10_000);
            Assertions.fail("Server on " + host + ":" + port + " is still reachable");
        } catch (IOException e) {
            //pass
        }
    }

    private void assertReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10_000);
        } catch (IOException e) {
            Assertions.fail("Server on " + host + ":" + port + " is not reachable");
        }
    }
}