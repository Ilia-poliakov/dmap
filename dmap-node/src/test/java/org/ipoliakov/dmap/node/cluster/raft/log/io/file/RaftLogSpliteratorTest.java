package org.ipoliakov.dmap.node.cluster.raft.log.io.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.ipoliakov.dmap.protocol.storage.PutReq;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

class RaftLogSpliteratorTest {

    @Test
    void characteristicsTest() {
        RaftLogFileReader.RaftLogSpliterator spliterator = new RaftLogFileReader.RaftLogSpliterator(CodedInputStream.newInstance(new byte[0]));
        assertEquals(Spliterator.ORDERED, spliterator.characteristics(), "RaftLogSpliterator must be ORDERED");
        assertNull(spliterator.trySplit(), "RaftLogSpliterator cannot be split");
        assertEquals(Long.MAX_VALUE, spliterator.estimateSize());
    }

    @Test
    void readOne() {
        PutReq req = PutReq.newBuilder()
                .setKey(ByteString.copyFromUtf8("key"))
                .setValue(ByteString.copyFromUtf8("value"))
                .build();
        Operation operation = Operation.newBuilder()
                .setPayloadType(PayloadType.PUT_REQ)
                .setLogIndex(1)
                .setTerm(1)
                .setMessage(req.toByteString())
                .build();
        RaftLogFileReader.RaftLogSpliterator spliterator = new RaftLogFileReader.RaftLogSpliterator(CodedInputStream.newInstance(operation.toByteArray()));

        final List<Operation> operations = new ArrayList<>(1);
        spliterator.tryAdvance(operations::add);

        assertEquals(operation, operations.get(0));
    }

    @Test
    void readOne_exception() throws IOException {
        CodedInputStream codedInputStream = Mockito.mock(CodedInputStream.class);
        Mockito.when(codedInputStream.readTag()).thenThrow(new IOException());
        RaftLogFileReader.RaftLogSpliterator spliterator = new RaftLogFileReader.RaftLogSpliterator(codedInputStream);
        assertThrows(RuntimeException.class, () -> spliterator.tryAdvance(op -> {}));
    }
}