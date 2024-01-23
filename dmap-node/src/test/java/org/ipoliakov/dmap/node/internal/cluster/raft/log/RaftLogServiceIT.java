package org.ipoliakov.dmap.node.internal.cluster.raft.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ipoliakov.dmap.node.IntegrationTest;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;

class RaftLogServiceIT extends IntegrationTest {

    @Autowired
    private RaftLogService service;

    @Test
    void append() {
        for (int i = 1; i < 6; i++) {
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
                            .setTerm(i)
                            .build()
            );
        }
        int index = 4;
        Operation expected = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setMessage(
                        PutReq.newBuilder()
                                .setKey(ByteString.copyFromUtf8("key" + index))
                                .setValue(ByteString.copyFromUtf8("value" + index))
                                .build()
                                .toByteString())
                .setLogIndex(index)
                .setTerm(index)
                .build();

        Operation actual = service.readByLogIndex(index).get();
        assertEquals(expected, actual);
    }

    @Test
    void getLastEntry() {
        for (int i = 1; i < 6; i++) {
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
                            .setTerm(i)
                            .build()
            );
        }
        int index = 5;
        Operation expected = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setMessage(
                        PutReq.newBuilder()
                                .setKey(ByteString.copyFromUtf8("key" + index))
                                .setValue(ByteString.copyFromUtf8("value" + index))
                                .build()
                                .toByteString())
                .setLogIndex(index)
                .setTerm(index)
                .build();

        assertEquals(expected, service.readLastEntry().get());
    }
}