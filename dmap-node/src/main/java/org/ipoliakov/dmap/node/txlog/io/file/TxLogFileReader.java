package org.ipoliakov.dmap.node.txlog.io.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ipoliakov.dmap.node.txlog.exception.TxLogReadingException;
import org.ipoliakov.dmap.node.txlog.io.TxLogReader;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.Operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TxLogFileReader implements TxLogReader {

    private final File logFile;

    @Override
    public Stream<Operation> readAll() {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile));
            CodedInputStream in = CodedInputStream.newInstance(inputStream);
            return StreamSupport.stream(new TxLogSpliterator(in), false)
                    .onClose(() -> close(inputStream));
        } catch (FileNotFoundException e) {
            log.error("Could not open log file", e);
            throw new TxLogReadingException(e);
        }
    }

    @Override
    public Operation read(int address) throws IOException {
        byte[] buf = new byte[8 * 1024];
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            raf.seek(address);
            raf.read(buf);
            return read(CodedInputStream.newInstance(new ByteArrayInputStream(buf)));
        }
    }

    private static Operation read(CodedInputStream in) throws IOException {
        if (in.readTag() != 0) {
            int payloadType = in.readEnum();
            in.readTag();
            long logIndex = in.readInt64();
            in.readTag();
            ByteString message = in.readBytes();
            return Operation.newBuilder()
                    .setPayloadType(PayloadType.forNumber(payloadType))
                    .setLogIndex(logIndex)
                    .setMessage(message)
                    .build();
        }
        return null;
    }

    private void close(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            log.error("Could not close input stream for file = {}", logFile.getAbsolutePath(), e);
        }
    }

    @RequiredArgsConstructor
    static class TxLogSpliterator implements Spliterator<Operation> {

        private final CodedInputStream in;

        @Override
        public boolean tryAdvance(Consumer<? super Operation> action) {
            try {
                Operation operation = read(in);
                if (operation != null) {
                    action.accept(operation);
                    return true;
                }
                return false;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Spliterator<Operation> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED;
        }
    }
}
