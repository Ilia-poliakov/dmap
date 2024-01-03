package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.protobuf.ByteString;

@Timeout(10)
class RemoveCommandTest extends IntegrationTest {

    @Test
    void execute() throws Exception {
        String value = "value";
        dataStorage.put(ByteString.copyFromUtf8("key"), ByteString.copyFromUtf8(value));
        String removedValue = client.remove("key", value).get();
        assertEquals(value, removedValue);
        assertFalse(dataStorage.containsKey(ByteString.copyFromUtf8(value)));
    }

    @Test
    @DisplayName("execute - return empty when nothing to remove")
    void execute_returnEmptyWhenNothingToRemove() throws Exception {
        String removedValue = client.remove("key", "value").get();
        assertTrue(removedValue.isEmpty());
    }
}