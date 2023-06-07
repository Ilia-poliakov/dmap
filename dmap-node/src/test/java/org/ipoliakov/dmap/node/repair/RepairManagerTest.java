package org.ipoliakov.dmap.node.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.tx.log.TxLogWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

class RepairManagerTest extends IntegrationTest {

    @Autowired
    private RepairManager repairManager;
    @Autowired
    private TxLogWriter txLogWriter;

    @Test
    void repairAll() throws Exception {
        int operationCount = 5;
        for (int i = 0; i < operationCount; i++) {
            client.put("key" + i, "value" + i).get(10, TimeUnit.SECONDS);
        }
        txLogWriter.flush();
        storage.clear();

        repairManager.repairAll();

        for (int i = 0; i < operationCount; i++) {
            ByteString bytes = storage.get(ByteString.copyFromUtf8("key" + i));
            assertEquals(bytes.toStringUtf8(), "value" + i);
        }
    }
}