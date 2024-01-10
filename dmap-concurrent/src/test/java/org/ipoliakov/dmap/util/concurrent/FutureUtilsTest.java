package org.ipoliakov.dmap.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class FutureUtilsTest {

    @Test
    void waitForQuorum() throws Exception {
        CompletableFuture<Boolean> f1 = CompletableFuture.completedFuture(true);
        CompletableFuture<Boolean> f2 = CompletableFuture.completedFuture(true);
        CompletableFuture<Boolean> f3 = CompletableFuture.completedFuture(true);

        Set<Throwable> throwables = FutureUtils.waitForQuorum(List.of(f1, f2, f3), Boolean::booleanValue, 10, TimeUnit.SECONDS);
        assertTrue(throwables.isEmpty());
    }

    @Test
    void waitForQuorum_singleFuture() throws Exception {
        CompletableFuture<Boolean> f1 = CompletableFuture.completedFuture(true);
        Set<Throwable> throwables = FutureUtils.waitForQuorum(List.of(f1), Boolean::booleanValue, 10, TimeUnit.SECONDS);
        assertTrue(throwables.isEmpty());
    }

    @Test
    void waitForQuorum_unreachable() {
        CompletableFuture<Boolean> f1 = CompletableFuture.completedFuture(true);
        CompletableFuture<Boolean> f2 = CompletableFuture.completedFuture(false);
        CompletableFuture<Boolean> f3 = CompletableFuture.completedFuture(false);

        assertThrows(QuorumUnreachableException.class,
                () -> FutureUtils.waitForQuorum(List.of(f1, f2, f3), Boolean::booleanValue, 10, TimeUnit.SECONDS));
    }

    @Test
    void waitForQuorum_unreachable_withExceptions() {
        CompletableFuture<Boolean> f1 = CompletableFuture.completedFuture(true);
        CompletableFuture<Boolean> f2 = CompletableFuture.completedFuture(false);
        CompletableFuture<Boolean> f3 = CompletableFuture.failedFuture(new Exception());

        assertThrows(QuorumUnreachableException.class,
                () -> FutureUtils.waitForQuorum(List.of(f1, f2, f3), Boolean::booleanValue, 10, TimeUnit.SECONDS));
    }

    @Test
    void waitForQuorum_unreachable_withTimeout() {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);
        CompletableFuture<Boolean> f1 = CompletableFuture.supplyAsync(() -> true, delayedExecutor);
        CompletableFuture<Boolean> f2 = CompletableFuture.supplyAsync(() -> true, delayedExecutor);
        CompletableFuture<Boolean> f3 = CompletableFuture.supplyAsync(() -> true, delayedExecutor);

        TimeoutException timeoutException = assertThrows(TimeoutException.class,
                () -> FutureUtils.waitForQuorum(List.of(f1, f2, f3), Boolean::booleanValue, 3, TimeUnit.SECONDS));
        assertEquals("Quorum unreachable in time 3 SECONDS", timeoutException.getMessage());
    }

    @Test
    void waitForQuorum_unreachable_withCanceledFuture() {
        CompletableFuture<Boolean> f1 = CompletableFuture.supplyAsync(() -> true, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));
        CompletableFuture<Boolean> f2 = CompletableFuture.completedFuture(false);
        CompletableFuture<Boolean> f3 = CompletableFuture.completedFuture(true);
        f1.cancel(true);

        assertThrows(QuorumUnreachableException.class,
                () -> FutureUtils.waitForQuorum(List.of(f1, f2, f3), Boolean::booleanValue, 10, TimeUnit.SECONDS));
    }
}