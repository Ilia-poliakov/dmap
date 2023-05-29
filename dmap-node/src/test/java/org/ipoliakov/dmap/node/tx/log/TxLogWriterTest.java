package org.ipoliakov.dmap.node.tx.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.LINUX;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.ipoliakov.dmap.protocol.Operation;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.PutReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

class TxLogWriterTest {

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
    @EnabledOnOs(LINUX)
    void write() throws IOException {
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

        List<Operation> actual = new ArrayList<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile))) {
            CodedInputStream in = CodedInputStream.newInstance(inputStream);
            while (in.readTag() != 0) {
                int payloadType = in.readEnum();
                in.readTag();
                long timestamp = in.readInt64();
                in.readTag();
                ByteString message = in.readBytes();
                actual.add(
                        Operation.newBuilder()
                                .setPayloadType(PayloadType.forNumber(payloadType))
                                .setTimestamp(timestamp)
                                .setMessage(message)
                                .build()
                );
            }
            assertEquals(expected, actual);
        }
    }
}