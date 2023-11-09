package org.ipoliakov.dmap.node.txlog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

class TxLogServiceIT extends IntegrationTest {

    @Autowired
    private TxLogService service;

    @Test
    void append() {
        for (int i = 0; i < 5; i++) {
            service.append(
                    Operation.newBuilder()
                            .setPayloadType(PayloadType.PUT_REQ)
                            .setMessage(
                                    PutReq.newBuilder()
                                            .setKey(ByteString.copyFromUtf8("key" + i))
                                            .setValue(ByteString.copyFromUtf8("value" + i))
                                            .build()
                                            .toByteString())
                            .setLogIndex(i)
                            .build()
            );
        }
        int index = 3;
        Operation expected = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setMessage(
                        PutReq.newBuilder()
                                .setKey(ByteString.copyFromUtf8("key" + index))
                                .setValue(ByteString.copyFromUtf8("value" + index))
                                .build()
                                .toByteString())
                .setLogIndex(index)
                .build();

        Operation actual = service.readByLogIndex(index);
        assertEquals(expected, actual);
    }
}