package org.ipoliakov.dmap.node.tx.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

class TxLogTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("txlog-test", "").toFile();
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void writeRead() throws IOException {
        TxLogWriter txLogWriter = new TxLogWriter(tempFile, 64);
        List<Operation> expected = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PutReq req = PutReq.newBuilder()
                    .setKey(ByteString.copyFromUtf8("key" + i))
                    .setValue(ByteString.copyFromUtf8("value" + i))
                    .build();
            Operation operation = Operation.newBuilder()
                    .setPayloadType(PayloadType.PUT_REQ)
                    .setTimestamp(System.currentTimeMillis())
                    .setMessage(req.toByteString())
                    .build();
            txLogWriter.write(operation);
            expected.add(operation);
        }
        txLogWriter.close();

        TxLogReader txLogReader = new TxLogReader(tempFile);
        List<Operation> actual;
        try (Stream<Operation> operations = txLogReader.read()) {
            actual = operations.collect(Collectors.toList());
        }

        assertEquals(expected, actual);
    }
}