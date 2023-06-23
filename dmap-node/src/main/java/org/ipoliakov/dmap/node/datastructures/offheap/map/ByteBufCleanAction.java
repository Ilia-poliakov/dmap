package org.ipoliakov.dmap.node.datastructures.offheap.map;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ByteBufCleanAction implements Runnable {

    private final ByteBuf buf;

    @Override
    public void run() {
        buf.release(buf.refCnt());
    }
}
