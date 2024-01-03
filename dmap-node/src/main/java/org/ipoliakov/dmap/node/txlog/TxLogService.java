package org.ipoliakov.dmap.node.txlog;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.ipoliakov.dmap.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.txlog.io.TxLogReader;
import org.ipoliakov.dmap.node.txlog.io.TxLogWriter;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TxLogService {

    private final TxLogWriter txLogFileWriter;
    private final TxLogReader txLogFileReader;
    private final IntRingBuffer txLogFileIndex;

    public void append(Operation operation) {
        try {
            int address = txLogFileWriter.write(operation);
            txLogFileIndex.add(address);
        } catch (IOException e) {
            log.error("Can't log operation to file", e);
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Operation> readByLogIndex(long logIndex) {
        try {
            int address = txLogFileIndex.get(logIndex - 1);
            return txLogFileReader.read(address);
        } catch (IOException e) {
            log.error("Can't read operation from log by index = {}", logIndex, e);
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Operation> readLastEntry() {
        try {
            if (txLogFileIndex.isEmpty()) {
                return Optional.empty();
            }
            return txLogFileReader.read(txLogFileIndex.getLast());
        } catch (IOException e) {
            log.error("Can't read last operation from log", e);
            throw new UncheckedIOException(e);
        }
    }
}
