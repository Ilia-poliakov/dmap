package org.ipoliakov.dmap.node.txlog;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.ipoliakov.dmap.node.datastructures.IntRingBuffer;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileReader;
import org.ipoliakov.dmap.node.txlog.io.file.TxLogFileWriter;
import org.ipoliakov.dmap.protocol.internal.Operation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TxLogService {

    private final IntRingBuffer txLogFileIndex;
    private final TxLogFileWriter txLogFileWriter;
    private final TxLogFileReader txLogFileReader;

    public void append(Operation operation) {
        try {
            int address = txLogFileWriter.write(operation);
            txLogFileIndex.add(address);
        } catch (IOException e) {
            log.error("Can't log operation to file", e);
            throw new UncheckedIOException(e);
        }
    }

    public Operation readByLogIndex(long logIndex) {
        try {
            int address = txLogFileIndex.get(logIndex);
            return txLogFileReader.read(address);
        } catch (IOException e) {
            log.error("Can't read operation from log by index = {}", logIndex, e);
            throw new UncheckedIOException(e);
        }
    }
}
