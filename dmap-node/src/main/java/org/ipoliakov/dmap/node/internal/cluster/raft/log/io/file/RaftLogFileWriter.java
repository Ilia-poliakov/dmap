package org.ipoliakov.dmap.node.internal.cluster.raft.log.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.ipoliakov.dmap.common.ByteBufferUnmapper;
import org.ipoliakov.dmap.common.OS;
import org.ipoliakov.dmap.node.internal.cluster.raft.log.io.RaftLogWriter;
import org.ipoliakov.dmap.protocol.raft.Operation;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RaftLogFileWriter implements RaftLogWriter {

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final File logFile;
    private final long growSize;

    private ByteBuffer mmap = EMPTY_BUFFER;

    @Override
    public int write(Operation operation) throws IOException {
        ByteString bytes = operation.toByteString();
        if (mmap.remaining() < bytes.size()) {
            resize(growSize);
        }
        int address = mmap.position();
        bytes.copyTo(mmap);
        return address;
    }

    @Override
    public void close() throws IOException {
        if (mmap == null) {
            return;
        }
        flush();
        forceUnmapOnWindows();
        mmap = EMPTY_BUFFER;
    }

    @Override
    public void flush() throws IOException {
        if (mmap.remaining() > 0) {
            ((MappedByteBuffer) mmap).force();
            resize(mmap.position() - mmap.limit());
        }
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
