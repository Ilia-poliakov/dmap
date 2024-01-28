package org.ipoliakov.dmap.util.concurrent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class FutureUtils {

    /**
     * Wait for results from the majority of futures by condition.
     * When specified condition is true then result of future is correct
     *
     * @param futures   futures of which a quorum is required
     * @param condition condition for successful completion
     * @param timeout   the maximum time to wait
     * @param unit      time unit of the timeout argument
     * @return Exception from unsuccessful futures
     * @throws QuorumUnreachableException when quorum if not reachable
     * @throws TimeoutException           if the wait timed out
     */
    @SuppressWarnings("checkstyle:JavaNCSS")
    public static <R> Set<Throwable> waitForQuorum(List<CompletableFuture<R>> futures, Predicate<R> condition, long timeout, TimeUnit unit) throws TimeoutException {
        int completedCount = 0;
        int incompleteCount = 0;
        int quorum = futures.size() / 2 + 1;
        Set<Throwable> throwables = new HashSet<>();
        long until = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < until) {
            CompletableFuture<?> aggregatedFuture = null;
            for (CompletableFuture<R> future : futures) {
                if (!future.isDone()) {
                    incompleteCount++;
                    aggregatedFuture = aggregatedFuture == null ? future : CompletableFuture.anyOf(aggregatedFuture, future);
                } else if (future.isCompletedExceptionally() && !future.isCancelled()) {
                    throwables.add(future.exceptionNow());
                } else if (!future.isCancelled()) {
                    R r = future.getNow(null);
                    if (condition.test(r)) {
                        completedCount++;
                    }
                }
            }
            if (completedCount >= quorum) {
                return throwables;
            }
            if (incompleteCount + completedCount < quorum) {
                throw new QuorumUnreachableException(completedCount, quorum);
            }
        }
        throw new TimeoutException("Quorum unreachable in time " + timeout + " " + unit);
    }
}
