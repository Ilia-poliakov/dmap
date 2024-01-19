package org.ipoliakov.dmap.it.kvstorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.client.KvStorageClient;
import org.ipoliakov.dmap.it.ClusterIntegrationTest;
import org.junit.jupiter.api.Test;

public class KeyValueStorageTest extends ClusterIntegrationTest {

    @Test
    void basePutGetCase() throws Exception {
        KvStorageClient<String, String> leaderClient = getLeaderClient();
        assertTrue(leaderClient.put("key", "value").get(10, TimeUnit.SECONDS).isEmpty());

        for (int i = 0; i < storageClients.size(); i++) {
            KvStorageClient<String, String> client = storageClients.get(i);
            String actual = client.get("key").get(10, TimeUnit.SECONDS);
            assertEquals("value", actual, "Wrong result for client " + (i + 1));
        }
    }
}
