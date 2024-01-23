package org.ipoliakov.dmap.common.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResponseFutures {

    private final ConcurrentMap<Long, CompletableFuture<?>> responseFutures = new ConcurrentHashMap<>();

    public CompletableFuture<?> get(Long messageId) {
        return responseFutures.remove(messageId);
    }

    public void add(Long messageId, CompletableFuture<?> completableFuture) {
        responseFutures.put(messageId, completableFuture);
    }
}
