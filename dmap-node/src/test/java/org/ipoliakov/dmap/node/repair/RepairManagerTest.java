package org.ipoliakov.dmap.node.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.TimeUnit;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileWriter;
import org.ipoliakov.dmap.node.txlog.repair.RepairManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

class RepairManagerTest extends IntegrationTest {

    @Autowired
    private RepairManager repairManager;
    @Autowired
    private TxLogFileWriter txLogWriter;

    @Test
    void repairAll() throws Exception {
        int operationCount = 6;
        for (int i = 1; i < operationCount; i++) {
            client.put("key" + i, "value" + i).get(10, TimeUnit.SECONDS);
        }
        client.remove("key1", "value1").get(10, TimeUnit.SECONDS);
        txLogWriter.flush();
        dataStorage.clear();

        repairManager.repairAll();

        assertFalse(dataStorage.containsKey(ByteString.copyFromUtf8("key1")));
        for (int i = 2; i < operationCount; i++) {
            ByteString bytes = dataStorage.get(ByteString.copyFromUtf8("key" + i));
            assertEquals(bytes.toStringUtf8(), "value" + i);
        }
    }
}