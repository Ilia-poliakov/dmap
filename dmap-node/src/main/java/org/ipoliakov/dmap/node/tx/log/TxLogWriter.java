package org.ipoliakov.dmap.node.tx.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.ipoliakov.dmap.common.ByteBufferUnmapper;
import org.ipoliakov.dmap.common.OS;
import org.ipoliakov.dmap.protocol.internal.Operation;

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

    @Override
    public void close() throws IOException {
        if (mmap == null) {
            return;
        }
        flush();
        forceUnmapOnWindows();
        mmap = null;
    }

    public void flush() throws IOException {
        ((MappedByteBuffer) mmap).force();
        resize(mmap.position() - mmap.limit());
    }

    private void resize(long deltaSize) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(logFile, "rw")) {
            long newLength = raf.length() + deltaSize;
            forceUnmapOnWindows();
            raf.setLength(newLength);
            int pos = mmap.position();
            mmap = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, newLength);
            mmap.position(pos);
        }
    }

    private void forceUnmapOnWindows() {
        if (OS.isWindows() && mmap.isDirect()) {
            ByteBufferUnmapper.unmap(mmap);
        }
    }
}
