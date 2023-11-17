package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class GetCommandTest extends IntegrationTest {

    @Test
    void execute() throws Exception {
        dataStorage.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        String actualValue = client.get("key").get(10, TimeUnit.SECONDS);
        assertEquals("value", actualValue);
    }

    @Test
    void execute_NotExistingKey() throws Exception {
        String notExistingKey = "not_existing_key";
        dataStorage.get(ByteString.copyFromUtf8(notExistingKey));
        assertTrue(client.get(notExistingKey).get(10, TimeUnit.SECONDS).isEmpty());
    }
}