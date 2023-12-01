package org.ipoliakov.dmap.node.util.concurrent.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

@Timeout(10)
class DmapStampedLockTest {

    private static final String VALUE = "VALUE";

    private DmapStampedLock lock;

    @BeforeEach
    void setUp() {
        lock = new DmapStampedLock();
    }

    @Test
    void optimisticRead() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Supplier<String> operation = Mockito.mock(Supplier.class);
        when(operation.get()).thenReturn(VALUE);
        assertEquals(VALUE, lock.optimisticRead(operation));
        verify(operation, only()).get();
        assertFalse(lock.isReadLocked());
        assertFalse(lock.isWriteLocked());
    }

    @Test
    void optimisticRead_failOptimistic() {
        AtomicBoolean firstComing = new AtomicBoolean(true);
        AtomicInteger invocationCount = new AtomicInteger();
        Supplier<String> operation = () -> {
            invocationCount.incrementAndGet();
            if (firstComing.get()) {
                long stamp = lock.writeLock();
                lock.unlockWrite(stamp);
                firstComing.set(false);
            }
            return VALUE;
        };
        assertEquals(VALUE, lock.optimisticRead(operation));
        assertEquals(2, invocationCount.get());
        assertFalse(lock.isReadLocked());
        assertFalse(lock.isWriteLocked());
    }

    @Test
    void writeLocked() {
        Supplier<String> operation = Mockito.mock(Supplier.class);
        when(operation.get()).thenReturn(VALUE);
        assertEquals(VALUE, lock.writeLocked(operation));
        verify(operation, only()).get();
        assertFalse(lock.isReadLocked());
        assertFalse(lock.isWriteLocked());
    }

    @Test
    void toWriteLock() {
        long stamp = lock.readLock();
        long writeStamp = lock.toWriteLock(stamp);
        assertTrue(lock.isWriteLocked());
        assertNotEquals(stamp, writeStamp);
        lock.unlock(writeStamp);
        assertFalse(lock.isReadLocked());
        assertFalse(lock.isWriteLocked());
    }
}