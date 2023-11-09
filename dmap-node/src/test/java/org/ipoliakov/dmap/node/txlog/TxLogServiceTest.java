package org.ipoliakov.dmap.node.txlog;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.ipoliakov.dmap.node.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileReader;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileWriter;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TxLogServiceTest {

    @Mock
    private IntRingBuffer index;
    @Mock
    private TxLogFileWriter txLogFileWriter;
    @Mock
    private TxLogFileReader txLogFileReader;

    @InjectMocks
    private TxLogService service;

    @Test
    void append_rethrowIOException() throws IOException {
        when(txLogFileWriter.write(any())).thenThrow(IOException.class);
        assertThrows(UncheckedIOException.class, () -> service.append(Operation.newBuilder().build()));
    }

    @Test
    void readByLogIndex_rethrowIOException() throws IOException {
        when(txLogFileReader.read(anyInt())).thenThrow(IOException.class);
        assertThrows(UncheckedIOException.class, () -> service.readByLogIndex(1));
    }
}