package org.ipoliakov.dmap.node.cluster.raft.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.cluster.raft.log.io.RaftLogReader;
import org.ipoliakov.dmap.node.cluster.raft.log.io.RaftLogWriter;
import org.ipoliakov.dmap.protocol.raft.Operation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaftLogService {

    private final RaftLogWriter raftLogFileWriter;
    private final RaftLogReader raftLogFileReader;
    private final IntRingBuffer raftLogFileIndex;

    public void append(Operation operation) {
        try {
            int address = raftLogFileWriter.write(operation);
            raftLogFileIndex.add(address);
        } catch (IOException e) {
            log.error("Can't log operation to file", e);
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Operation> readByLogIndex(long logIndex) {
        try {
            int address = raftLogFileIndex.get(logIndex - 1);
            return raftLogFileReader.read(address);
        } catch (IOException e) {
            log.error("Can't read operation from log by index = {}", logIndex, e);
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Operation> readLastEntry() {
        try {
            if (raftLogFileIndex.isEmpty()) {
                return Optional.empty();
            }
            return raftLogFileReader.read(raftLogFileIndex.getLast());
        } catch (IOException e) {
            log.error("Can't read last operation from log", e);
            throw new UncheckedIOException(e);
        }
    }
}
