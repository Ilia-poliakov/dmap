package org.ipoliakov.dmap.node.internal.cluster.raft.log;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.internal.cluster.raft.log.io.file.RaftLogFileReader;
import org.ipoliakov.dmap.node.internal.cluster.raft.log.io.file.RaftLogFileWriter;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RaftLogServiceTest {

    @Mock
    private IntRingBuffer index;
    @Mock
    private RaftLogFileWriter raftLogFileWriter;
    @Mock
    private RaftLogFileReader raftLogFileReader;

    @InjectMocks
    private RaftLogService service;

    @Test
    void append_rethrowIOException() throws IOException {
        when(raftLogFileWriter.write(any())).thenThrow(IOException.class);
        assertThrows(UncheckedIOException.class, () -> service.append(Operation.newBuilder().build()));
    }

    @Test
    void readByLogIndex_rethrowIOException() throws IOException {
        when(raftLogFileReader.read(anyInt())).thenThrow(IOException.class);
        assertThrows(UncheckedIOException.class, () -> service.readByLogIndex(1));
    }

    @Test
    void readLastEntry_rethrowIOException() throws IOException {
        when(raftLogFileReader.read(anyInt())).thenThrow(IOException.class);
        assertThrows(UncheckedIOException.class, () -> service.readLastEntry());
    }
}