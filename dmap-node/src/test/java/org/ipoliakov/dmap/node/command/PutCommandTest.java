package org.ipoliakov.dmap.node.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class PutCommandTest extends IntegrationTest {

    @Test
    void execute() throws Exception {
        String prev = client.put("key", "value").get();

        ByteString actualVal = dataStorage.get(ByteString.copyFromUtf8("key"));
        ByteString expectedVal = ByteString.copyFromUtf8("value");
        assertEquals(expectedVal, actualVal);

        assertTrue(prev.isEmpty());
    }

    @Test
    void execute_returnPrev_WhenRewriteByKey() throws Exception {
        client.put("key", "value1").get();
        String prev = client.put("key", "value2").get();

        ByteString actualVal = dataStorage.get(ByteString.copyFromUtf8("key"));
        assertEquals("value2", actualVal.toStringUtf8());
        assertEquals("value1", prev);
    }
}