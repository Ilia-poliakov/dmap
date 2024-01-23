package org.ipoliakov.dmap.node.cluster.raft.log.io.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ipoliakov.dmap.node.cluster.raft.log.exception.RaftLogReadingException;
import org.ipoliakov.dmap.node.cluster.raft.log.io.RaftLogReader;
import org.ipoliakov.dmap.protocol.PayloadType;
import org.ipoliakov.dmap.protocol.raft.Operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RaftLogFileReader implements RaftLogReader {

    private final File logFile;

    @Override
    public Stream<Operation> readAll() {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(logFile));
            CodedInputStream in = CodedInputStream.newInstance(inputStream);
            return StreamSupport.stream(new RaftLogSpliterator(in), false)
                    .onClose(() -> close(inputStream));
        } catch (FileNotFoundException e) {
            log.error("Could not open log file", e);
            throw new RaftLogReadingException(e);
        }
    }

    @Override
    public Optional<Operation> read(int address) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            raf.seek(address);
            InputStream inputStream = new BufferedInputStream(Channels.newInputStream(raf.getChannel()));
            return Optional.ofNullable(read(CodedInputStream.newInstance(inputStream)));
        }
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    private static Operation read(CodedInputStream in) throws IOException {
        if (in.readTag() != 0) {
            int payloadType = in.readEnum();
            in.readTag();
            int term = in.readInt32();
            in.readTag();
            long logIndex = in.readInt64();
            in.readTag();
            ByteString message = in.readBytes();
            return Operation.newBuilder()
                    .setPayloadType(PayloadType.forNumber(payloadType))
                    .setTerm(term)
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
    static class RaftLogSpliterator implements Spliterator<Operation> {

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