package org.ipoliakov.dmap.node.tx.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.internal.Operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TxLogReader {

    private final File logFile;

    public Stream<Operation> read() throws FileNotFoundException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile));
        CodedInputStream in = CodedInputStream.newInstance(inputStream);
        return StreamSupport.stream(new TxLogSpliterator(in), false)
                .onClose(() -> close(inputStream));
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
                if (in.readTag() != 0) {
                    int payloadType = in.readEnum();
                    in.readTag();
                    long timestamp = in.readInt64();
                    in.readTag();
                    ByteString message = in.readBytes();
                    action.accept(
                            Operation.newBuilder()
                                    .setPayloadType(PayloadType.forNumber(payloadType))
                                    .setTimestamp(timestamp)
                                    .setMessage(message)
                                    .build()
                    );
                    return true;
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return false;
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
