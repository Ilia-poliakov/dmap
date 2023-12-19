package org.ipoliakov.dmap.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

public class PutGetTest extends ClusterIntegrationTest {

    @Test
    void basePutGetCase() throws ExecutionException, InterruptedException, TimeoutException {
        assertTrue(client.put("key", "value").get(10, TimeUnit.SECONDS).isEmpty());
        String actual = client.get("key").get(10, TimeUnit.SECONDS);
        assertEquals("value", actual);
    }
}
