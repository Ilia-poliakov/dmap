package org.ipoliakov.dmap.node.txlog.io.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

class TxLogSpliteratorTest {

    @Test
    void characteristicsTest() {
        TxLogFileReader.TxLogSpliterator spliterator = new TxLogFileReader.TxLogSpliterator(CodedInputStream.newInstance(new byte[0]));
        assertEquals(Spliterator.ORDERED, spliterator.characteristics(), "TxLogSpliterator must be ORDERED");
        assertNull(spliterator.trySplit(), "TxLogSpliterator cannot be split");
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
                .setTimestamp(System.currentTimeMillis())
                .setMessage(req.toByteString())
                .build();
        TxLogFileReader.TxLogSpliterator spliterator = new TxLogFileReader.TxLogSpliterator(CodedInputStream.newInstance(operation.toByteArray()));

        final List<Operation> operations = new ArrayList<>(1);
        spliterator.tryAdvance(operations::add);

        assertEquals(operation, operations.get(0));
    }

    @Test
    void readOne_exception() throws IOException {
        CodedInputStream codedInputStream = Mockito.mock(CodedInputStream.class);
        Mockito.when(codedInputStream.readTag()).thenThrow(new IOException());
        TxLogFileReader.TxLogSpliterator spliterator = new TxLogFileReader.TxLogSpliterator(codedInputStream);
        assertThrows(RuntimeException.class, () -> spliterator.tryAdvance(op -> {}));
    }
}