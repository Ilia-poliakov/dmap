package org.ipoliakov.dmap.node.tx.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.ipoliakov.dmap.protocol.Operation;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TxLogWriter implements AutoCloseable {

    private final File logFile;
    private final long growSize;

    private ByteBuffer mmap = ByteBuffer.allocate(0);

    public void write(Operation operation) throws IOException {
        ByteString bytes = operation.toByteString();
        if (mmap.remaining() < bytes.size()) {
            resize(growSize);
        }
        bytes.copyTo(mmap);
    }

    private void resize(long deltaSize) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "rw")) {
            long newLength = raf.length() + deltaSize;
            raf.setLength(newLength);
            int pos = mmap.position();
            mmap = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, newLength);
            mmap.position(pos);
        }
    }

    @Override
    public void close() throws IOException {
        resize(mmap.position() - mmap.limit());
    }
}
