package org.ipoliakov.dmap.node.internal.cluster.raft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.ipoliakov.dmap.node.txlog.TxLogService;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
class RaftLogTest {

    @Mock
    private TxLogService txLogService;

    @InjectMocks
    private RaftLog raftLog;

    @Test
    void append_shouldSaveIndexAndTerm() {
        var operation = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setTerm(1)
                .setLogIndex(1)
                .setMessage(ByteString.EMPTY)
                .build();
        raftLog.append(operation);
        assertEquals(raftLog.getLastTerm(), operation.getTerm());
        assertEquals(raftLog.getLastIndex(), operation.getLogIndex());
    }

    @Test
    void getLastIndex() {
        raftLog.setLastIndex(1);
        assertEquals(1, raftLog.getLastIndex());
    }

    @Test
    void getLastIndex_readFromFileWhenZero() {
        when(txLogService.readLastEntry()).thenReturn(
                Optional.of(Operation.newBuilder()
                        .setPayloadType(PayloadType.PUT_REQ)
                        .setTerm(2)
                        .setLogIndex(2)
                        .setMessage(ByteString.EMPTY)
                        .build())
        );
        assertEquals(2, raftLog.getLastIndex());
    }

    @Test
    void getLastTerm() {
        var operation = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setTerm(1)
                .setLogIndex(1)
                .setMessage(ByteString.EMPTY)
                .build();
        raftLog.append(operation);
        assertEquals(1, raftLog.getLastTerm());
    }

    @Test
    void getLastTerm_readFromFileWhenZero() {
        when(txLogService.readLastEntry()).thenReturn(
                Optional.of(Operation.newBuilder()
                        .setPayloadType(PayloadType.PUT_REQ)
                        .setTerm(2)
                        .setLogIndex(2)
                        .setMessage(ByteString.EMPTY)
                        .build())
        );
        assertEquals(2, raftLog.getLastTerm());
    }
}