package org.ipoliakov.dmap.common.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class ResponseFuturesTest {

    @Test
    @DisplayName("get - must remove future on getting")
    void get() throws Exception {
        long messageId = 1L;
        var responseFutures = new ResponseFutures();
        responseFutures.add(messageId, CompletableFuture.completedFuture("test"));
        assertEquals("test", responseFutures.get(messageId).get());
        assertNull(responseFutures.get(messageId));
    }

    @Test
    void add() throws Exception {
        long messageId = 1L;
        var responseFutures = new ResponseFutures();
        responseFutures.add(messageId, CompletableFuture.completedFuture("test"));
        assertEquals("test", responseFutures.get(messageId).get());
    }
}