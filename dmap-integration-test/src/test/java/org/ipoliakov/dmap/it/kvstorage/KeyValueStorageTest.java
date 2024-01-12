package org.ipoliakov.dmap.it.kvstorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.client.DMapClient;
import org.ipoliakov.dmap.it.ClusterIntegrationTest;
import org.junit.jupiter.api.Test;

public class KeyValueStorageTest extends ClusterIntegrationTest {

    @Test
    void basePutGetCase() throws Exception {
        DMapClient<String, String> leaderClient = getLeaderClient();
        assertTrue(leaderClient.put("key", "value").get(10, TimeUnit.SECONDS).isEmpty());

        for (int i = 0; i < clients.size(); i++) {
            DMapClient<String, String> client = clients.get(i);
            String actual = client.get("key").get(10, TimeUnit.SECONDS);
            assertEquals("value", actual, "Wrong result for client " + (i + 1));
        }
    }
}
