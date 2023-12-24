package org.ipoliakov.dmap.common;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class ByteBufferUnmapperTest {

    @Test
    void unmap() {
        OS.isWindows();
        ByteBufferUnmapper.unmap(ByteBuffer.allocateDirect(1));
    }
}