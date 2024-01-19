package org.ipoliakov.dmap.node.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileWriter;
import org.ipoliakov.dmap.node.txlog.repair.RepairManager;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.util.ProtoMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

class RepairManagerTest extends IntegrationTest {

    @Autowired
    private RepairManager repairManager;
    @Autowired
    private TxLogFileWriter txLogWriter;

    @BeforeEach
    void setUp() throws Exception {
        resetState();
    }

    @Test
    void repairAll() throws Exception {
        int operationCount = 6;
        for (int i = 1; i < operationCount; i++) {
            logPutOperation(i);
        }
        logRemoveOperation(1, operationCount);

        txLogWriter.flush();
        dataStorage.clear();

        repairManager.repairAll();

        assertFalse(dataStorage.containsKey(ByteString.copyFromUtf8("key1")));
        for (int i = 2; i < operationCount; i++) {
            ByteString bytes = dataStorage.get(ByteString.copyFromUtf8("key" + i));
            assertEquals(bytes.toStringUtf8(), "value" + i);
        }

        assertEquals(1, raftLog.getLastTerm());
        assertEquals(operationCount, raftLog.getLastIndex());
    }

    private void logPutOperation(int index) {
        raftLog.append(
                Operation.newBuilder()
                        .setPayloadType(PayloadType.PUT_REQ)
                        .setLogIndex(index)
                        .setTerm(1)
                        .setMessage(
                                ProtoMessages.putReq()
                                        .setKey(ByteString.copyFromUtf8("key" + index))
                                        .setValue(ByteString.copyFromUtf8("value" + index))
                                        .build()
                                        .toByteString()
                        )
                        .build()
        );
    }

    private void logRemoveOperation(int index, long logIndex) {
        raftLog.append(
                Operation.newBuilder()
                        .setPayloadType(PayloadType.REMOVE_REQ)
                        .setMessage(ProtoMessages.removeReq(ByteString.copyFromUtf8("key" + index)).toByteString())
                        .setLogIndex(logIndex)
                        .setTerm(1)
                        .build()
        );
    }
}