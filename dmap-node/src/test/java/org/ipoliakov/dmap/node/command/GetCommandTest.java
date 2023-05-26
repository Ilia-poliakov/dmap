package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class GetCommandTest extends IntegrationTest {

    @Test
    void execute() throws Exception {
        storage.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8("value"));
        String actualValue = client.get("key").get();
        assertEquals("value", actualValue);
    }

    @Test
    void execute_NotExistingKey() throws Exception {
        String notExistingKey = "not_existing_key";
        storage.get(ByteString.copyFromUtf8(notExistingKey));
        assertTrue(client.get(notExistingKey).get().isEmpty());
    }
}