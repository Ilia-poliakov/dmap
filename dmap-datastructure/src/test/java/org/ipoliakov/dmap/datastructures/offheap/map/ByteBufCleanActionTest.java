package org.ipoliakov.dmap.datastructures.offheap.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ByteBufCleanActionTest {

    @Test
    void run_shouldReleaseAllRefs() {
        ByteBuf buf = Unpooled.buffer(1);
        buf.retain();
        buf.retain();
        buf.retain();

        var cleanAction = new ByteBufCleanAction(buf);
        cleanAction.run();

        assertEquals(0, buf.refCnt());
    }
}