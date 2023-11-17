package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.protobuf.ByteString;

class RemoveCommandTest extends IntegrationTest {

    @Test
    @Timeout(10)
    void execute() throws Exception {
        String value = "value";
        dataStorage.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8(value));
        String removedValue = client.remove("key", value).get(10, TimeUnit.SECONDS);
        assertEquals(value, removedValue);
        assertFalse(dataStorage.containsKey(ByteString.copyFromUtf8(value)));
    }

    @Test
    @Timeout(10)
    @DisplayName("execute - return empty when nothing to remove")
    void execute_returnEmptyWhenNothingToRemove() throws Exception {
        String removedValue = client.remove("key", "value").get(10, TimeUnit.SECONDS);
        assertTrue(removedValue.isEmpty());
    }
}